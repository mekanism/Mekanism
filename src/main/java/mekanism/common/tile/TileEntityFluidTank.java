package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedTank;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITierUpgradeable;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityFluidTank extends TileEntityContainerBlock implements IActiveState, IConfigurable,
      IFluidHandlerWrapper, ISustainedTank, IFluidContainerManager, ITankManager, ISecurityTile, ITierUpgradeable {

    public boolean isActive;

    public boolean clientActive;

    public FluidTank fluidTank;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier = FluidTankTier.BASIC;

    public int updateDelay;

    public int prevAmount;

    public int valve;
    public Fluid valveFluid;

    public float prevScale;

    public boolean needsPacket;

    public int currentRedstoneLevel;

    public TileComponentSecurity securityComponent = new TileComponentSecurity(this);

    public TileEntityFluidTank() {
        super("FluidTank");

        fluidTank = new FluidTank(tier.storage);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        if (upgradeTier.ordinal() != tier.ordinal() + 1) {
            return false;
        }

        tier = FluidTankTier.values()[upgradeTier.ordinal()];
        fluidTank.setCapacity(tier.storage);

        Mekanism.packetHandler
              .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                    new Range4D(Coord4D.get(this)));
        markDirty();

        return true;
    }

    @Override
    public boolean canSetFacing(int facing) {
        return false;
    }

    @Override
    public void onUpdate() {
        if (world.isRemote) {
            if (updateDelay > 0) {
                updateDelay--;

                if (updateDelay == 0 && clientActive != isActive) {
                    isActive = clientActive;
                    MekanismUtils.updateBlock(world, getPos());
                }
            }

            float targetScale =
                  (float) (fluidTank.getFluid() != null ? fluidTank.getFluid().amount : 0) / fluidTank.getCapacity();

            if (Math.abs(prevScale - targetScale) > 0.01) {
                prevScale = (9 * prevScale + targetScale) / 10;
            }
        } else {
            if (updateDelay > 0) {
                updateDelay--;

                if (updateDelay == 0 && clientActive != isActive) {
                    needsPacket = true;
                }
            }

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

            if (!inventory.get(0).isEmpty()) {
                manageInventory();
            }

            if (isActive) {
                activeEmit();
            }

            int newRedstoneLevel = getRedstoneLevel();

            if (newRedstoneLevel != currentRedstoneLevel) {
                markDirty();
                currentRedstoneLevel = newRedstoneLevel;
            }

            if (needsPacket) {
                Mekanism.packetHandler.sendToAllAround(
                      new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                      Coord4D.get(this).getTargetPoint(50));
            }

            needsPacket = false;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize("tile.FluidTank" + tier.getBaseTier().getSimpleName() + ".name");
    }

    private void activeEmit() {
        if (fluidTank.getFluid() != null) {
            TileEntity tileEntity = Coord4D.get(this).offset(EnumFacing.DOWN).getTileEntity(world);

            if (CapabilityUtils
                  .hasCapability(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP)) {
                IFluidHandler handler = CapabilityUtils
                      .getCapability(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.UP);
                FluidStack toDrain = new FluidStack(fluidTank.getFluid(),
                      Math.min(tier.output, fluidTank.getFluidAmount()));
                fluidTank.drain(handler.fill(toDrain, true), tier != FluidTankTier.CREATIVE);
            }
        }
    }

    private void manageInventory() {
        if (FluidContainerUtils.isFluidContainer(inventory.get(0))) {
            FluidStack ret = FluidContainerUtils
                  .handleContainerItem(this, inventory, editMode, fluidTank.getFluid(), getCurrentNeeded(), 0, 1, null);

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
            } else {
                if (tier != FluidTankTier.CREATIVE) {
                    fluidTank.setFluid(null);
                }
            }
        }
    }

    public int pushUp(FluidStack fluid, boolean doFill) {
        Coord4D up = Coord4D.get(this).offset(EnumFacing.UP);
        TileEntity tileEntity = up.getTileEntity(world);
        if (tileEntity instanceof TileEntityFluidTank) {
            IFluidHandler handler = CapabilityUtils
                  .getCapability(tileEntity, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.DOWN);

            if (PipeUtils.canFill(handler, fluid)) {
                return handler.fill(fluid, doFill);
            }
        }

        return 0;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
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
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return new int[]{1};
        } else if (side == EnumFacing.UP) {
            return new int[]{0};
        }

        return InventoryUtils.EMPTY;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("tier", tier.ordinal());
        nbtTags.setBoolean("isActive", isActive);
        nbtTags.setInteger("editMode", editMode.ordinal());

        if (fluidTank.getFluid() != null) {
            nbtTags.setTag("fluidTank", fluidTank.writeToNBT(new NBTTagCompound()));
        }

        return nbtTags;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        tier = FluidTankTier.values()[nbtTags.getInteger("tier")];
        clientActive = isActive = nbtTags.getBoolean("isActive");
        editMode = ContainerEditMode.values()[nbtTags.getInteger("editMode")];

        if (nbtTags.hasKey("fluidTank")) {
            fluidTank.setCapacity(tier.storage);
            fluidTank.readFromNBT(nbtTags.getCompoundTag("fluidTank"));
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            FluidTankTier prevTier = tier;

            tier = FluidTankTier.values()[dataStream.readInt()];
            fluidTank.setCapacity(tier.storage);

            clientActive = dataStream.readBoolean();
            valve = dataStream.readInt();
            editMode = ContainerEditMode.values()[dataStream.readInt()];

            if (valve > 0) {
                valveFluid = FluidRegistry.getFluid(PacketHandler.readString(dataStream));
            } else {
                valveFluid = null;
            }

            TileUtils.readTankData(dataStream, fluidTank);

            if (prevTier != tier || (updateDelay == 0 && clientActive != isActive)) {
                updateDelay = general.UPDATE_DELAY;
                isActive = clientActive;
                MekanismUtils.updateBlock(world, getPos());
            }
        }
    }

    public int getRedstoneLevel() {
        double fractionFull = (float) fluidTank.getFluidAmount() / (float) fluidTank.getCapacity();
        return MathHelper.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    public int getCurrentNeeded() {
        int needed = fluidTank.getCapacity() - fluidTank.getFluidAmount();

        if (tier == FluidTankTier.CREATIVE) {
            return Integer.MAX_VALUE;
        }

        Coord4D top = Coord4D.get(this).offset(EnumFacing.UP);
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
        data.add(isActive);
        data.add(valve);
        data.add(editMode.ordinal());

        if (valve > 0) {
            data.add(FluidRegistry.getFluidName(valveFluid));
        }

        TileUtils.addTankData(data, fluidTank);

        return data;
    }

    @Override
    public boolean getActive() {
        return isActive;
    }

    @Override
    public void setActive(boolean active) {
        isActive = active;

        if (clientActive != active && updateDelay == 0) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));

            updateDelay = 10;
            clientActive = active;
        }
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
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side) {
        if (!world.isRemote) {
            setActive(!getActive());
            world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK,
                  SoundCategory.BLOCKS, 0.3F, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side) {
        return EnumActionResult.PASS;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.CONFIGURABLE_CAPABILITY
              || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
              || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.CONFIGURABLE_CAPABILITY) {
            return Capabilities.CONFIGURABLE_CAPABILITY.cast(this);
        }

        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }

        return super.getCapability(capability, side);
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (tier == FluidTankTier.CREATIVE) {
            return resource != null ? resource.amount : 0;
        }

        if (resource != null && canFill(from, resource)) {
            int filled = fluidTank.fill(resource, doFill);

            if (filled < resource.amount && !isActive) {
                filled += pushUp(PipeUtils.copy(resource, resource.amount - filled), doFill);
            }

            if (filled > 0 && from == EnumFacing.UP) {
                if (valve == 0) {
                    needsPacket = true;
                }

                valve = 20;
                valveFluid = resource.getFluid();
            }

            return filled;
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        if (resource != null && canDrain(from, resource)) {
            return fluidTank.drain(resource.amount, tier != FluidTankTier.CREATIVE && doDrain);
        }

        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        if (canDrain(from, null)) {
            return fluidTank.drain(maxDrain, tier != FluidTankTier.CREATIVE && doDrain);
        }

        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        TileEntity tile = world.getTileEntity(getPos().offset(EnumFacing.DOWN));
        if (from == EnumFacing.DOWN && world != null) {
            if (isActive && !(tile instanceof TileEntityFluidTank)) {
                return false;
            }
        }

        if (tier == FluidTankTier.CREATIVE) {
            return true;
        }

        if (isActive && tile instanceof TileEntityFluidTank) // Only fill if tanks underneath have same fluid.
        {
            return (fluidTank.getFluid() == null && ((TileEntityFluidTank) tile).canFill(EnumFacing.UP, fluid)) ||
                  (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(fluid));
        }

        return fluidTank.getFluid() == null || fluidTank.getFluid().isFluidEqual(fluid);
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        if (fluidTank != null) {
            if (fluid == null || fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(fluid)) {
                return !(isActive && from == EnumFacing.DOWN);

            }
        }

        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
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

    @Override
    public TileComponentSecurity getSecurity() {
        return securityComponent;
    }
}
