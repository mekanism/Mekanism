package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityFluidTank extends TileEntityMekanism implements IActiveState, IConfigurable, IFluidHandlerWrapper, ISustainedTank, IFluidContainerManager,
      ITankManager, ITierUpgradeable, IComparatorSupport {

    public FluidTank fluidTank;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier;

    public int prevAmount;

    public int valve;
    public FluidStack valveFluid;

    public float prevScale;

    public boolean needsPacket;

    public int currentRedstoneLevel;

    public TileEntityFluidTank(IBlockProvider blockProvider) {
        super(blockProvider);
        this.tier = ((BlockFluidTank) blockProvider.getBlock()).getTier();
        fluidTank = new FluidTank(this.tier.getStorage());
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }
        tier = FluidTankTier.values()[upgradeTier.ordinal()];
        fluidTank.setCapacity(tier.getStorage());
        Mekanism.packetHandler.sendUpdatePacket(this);
        markDirty();
        return true;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            float targetScale = (float) (fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0) / fluidTank.getCapacity();
            if (Math.abs(prevScale - targetScale) > 0.01) {
                prevScale = (9 * prevScale + targetScale) / 10;
            }
        } else {
            if (valve > 0) {
                valve--;
                if (valve == 0) {
                    valveFluid = null;
                    needsPacket = true;
                }
            }

            if (fluidTank.getFluidAmount() != prevAmount) {
                MekanismUtils.saveChunk(this);
                needsPacket = true;
            }

            prevAmount = fluidTank.getFluidAmount();
            if (!getInventory().get(0).isEmpty()) {
                manageInventory();
            }
            if (getActive()) {
                activeEmit();
            }

            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                markDirty();
                currentRedstoneLevel = newRedstoneLevel;
            }
            if (needsPacket) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
            needsPacket = false;
        }
    }

    private void activeEmit() {
        if (fluidTank.getFluid() != null) {
            TileEntity tileEntity = Coord4D.get(this).offset(Direction.DOWN).getTileEntity(world);
            CapabilityUtils.getCapabilityHelper(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).ifPresent(handler -> {
                FluidStack toDrain = new FluidStack(fluidTank.getFluid(), Math.min(tier.getOutput(), fluidTank.getFluidAmount()));
                fluidTank.drain(handler.fill(toDrain, true), tier != FluidTankTier.CREATIVE);
            });
        }
    }

    private void manageInventory() {
        if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
            FluidStack ret = FluidContainerUtils.handleContainerItem(this, getInventory(), editMode, fluidTank.getFluid(), getCurrentNeeded(), 0, 1, null);

            if (ret != null) {
                fluidTank.setFluid(PipeUtils.copy(ret, Math.min(fluidTank.getCapacity(), ret.amount)));
                if (tier == FluidTankTier.CREATIVE) {
                    FluidStack fluid = fluidTank.getFluid();
                    if (fluid != null) {
                        fluid.amount = Integer.MAX_VALUE;
                    }
                } else {
                    int rejects = Math.max(0, ret.amount - fluidTank.getCapacity());
                    if (rejects > 0) {
                        pushUp(PipeUtils.copy(ret, rejects), true);
                    }
                }
            } else if (tier != FluidTankTier.CREATIVE) {
                fluidTank.setFluid(null);
            }
        }
    }

    public int pushUp(FluidStack fluid, boolean doFill) {
        Coord4D up = Coord4D.get(this).offset(Direction.UP);
        TileEntity tileEntity = up.getTileEntity(world);
        if (tileEntity instanceof TileEntityFluidTank) {
            return CapabilityUtils.getCapabilityHelper(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN).getIfPresentElse(
                  handler -> PipeUtils.canFill(handler, fluid) ? handler.fill(fluid, doFill) : 0,
                  0
            );
        }
        return 0;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        return slotID == 1;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return FluidContainerUtils.isFluidContainer(itemstack);
        }
        return false;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{1};
        } else if (side == Direction.UP) {
            return new int[]{0};
        }
        return InventoryUtils.EMPTY;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("tier", tier.ordinal());
        nbtTags.putInt("editMode", editMode.ordinal());
        if (fluidTank.getFluid() != null) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        tier = FluidTankTier.values()[nbtTags.getInt("tier")];
        editMode = ContainerEditMode.values()[nbtTags.getInt("editMode")];
        //Needs to be outside the contains check because this is just based on the tier which is known information
        fluidTank.setCapacity(tier.getStorage());
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
            FluidTankTier prevTier = tier;
            tier = FluidTankTier.values()[dataStream.readInt()];
            fluidTank.setCapacity(tier.getStorage());

            valve = dataStream.readInt();
            editMode = ContainerEditMode.values()[dataStream.readInt()];
            if (valve > 0) {
                valveFluid = TileUtils.readFluidStack(dataStream);
            } else {
                valveFluid = null;
            }

            TileUtils.readTankData(dataStream, fluidTank);
            if (prevTier != tier) {
                //TODO: Is this still needed given the block actually will change once we setup upgrading
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    public int getCurrentNeeded() {
        int needed = fluidTank.getCapacity() - fluidTank.getFluidAmount();
        if (tier == FluidTankTier.CREATIVE) {
            return Integer.MAX_VALUE;
        }
        Coord4D top = Coord4D.get(this).offset(Direction.UP);
        TileEntity topTile = top.getTileEntity(world);
        if (topTile instanceof TileEntityFluidTank) {
            TileEntityFluidTank topTank = (TileEntityFluidTank) topTile;
            if (fluidTank.getFluid() != null && topTank.fluidTank.getFluid() != null) {
                if (fluidTank.getFluid().getFluid() != topTank.fluidTank.getFluid().getFluid()) {
                    return needed;
                }
            }
            needed += topTank.getCurrentNeeded();
        }
        return needed;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(tier.ordinal());
        data.add(valve);
        data.add(editMode.ordinal());
        if (valve > 0) {
            TileUtils.addFluidStack(data, valveFluid);
        }
        TileUtils.addTankData(data, fluidTank);
        return data;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        if (!world.isRemote) {
            setActive(!getActive());
            world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return side != null && side != Direction.DOWN && side != Direction.UP;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        if (tier == FluidTankTier.CREATIVE) {
            return resource.amount;
        }
        int filled = fluidTank.fill(resource, doFill);
        if (filled < resource.amount && !getActive()) {
            filled += pushUp(PipeUtils.copy(resource, resource.amount - filled), doFill);
        }
        if (filled > 0 && from == Direction.UP) {
            if (valve == 0) {
                needsPacket = true;
            }
            valve = 20;
            valveFluid = new FluidStack(resource, 1);
        }
        return filled;
    }

    @Override
    @Nullable
    public FluidStack drain(Direction from, int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, tier != FluidTankTier.CREATIVE && doDrain);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        TileEntity tile = MekanismUtils.getTileEntity(world, getPos().offset(Direction.DOWN));
        if (from == Direction.DOWN && getActive() && !(tile instanceof TileEntityFluidTank)) {
            return false;
        }
        if (tier == FluidTankTier.CREATIVE) {
            return true;
        }
        if (getActive() && tile instanceof TileEntityFluidTank) { // Only fill if tanks underneath have same fluid.
            return fluidTank.getFluid() == null ? ((TileEntityFluidTank) tile).canFill(Direction.UP, fluid) : fluidTank.getFluid().isFluidEqual(fluid);
        }
        return FluidContainerUtils.canFill(fluidTank.getFluid(), fluid);
    }

    @Override
    public boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        return fluidTank != null && FluidContainerUtils.canDrain(fluidTank.getFluid(), fluid) && !getActive() || from != Direction.DOWN;
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        return new FluidTankInfo[]{fluidTank.getInfo()};
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public void setFluidStack(FluidStack fluidStack, Object... data) {
        fluidTank.setFluid(fluidStack);
    }

    @Override
    public FluidStack getFluidStack(Object... data) {
        return fluidTank.getFluid();
    }

    @Override
    public boolean hasTank(Object... data) {
        return true;
    }

    @Override
    public ContainerEditMode getContainerEditMode() {
        return editMode;
    }

    @Override
    public void setContainerEditMode(ContainerEditMode mode) {
        editMode = mode;
    }

    @Override
    public Object[] getTanks() {
        return new Object[]{fluidTank};
    }
}