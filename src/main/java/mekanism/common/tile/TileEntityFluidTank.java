package mekanism.common.tile;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.base.CreativeFluidTank;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFluidContainerManager;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ITankManager;
import mekanism.common.base.ITileComponent;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.upgrade.FluidTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityFluidTank extends TileEntityMekanism implements IActiveState, IConfigurable, IFluidHandlerWrapper, ISustainedTank, IFluidContainerManager,
      ITankManager {

    public FluidTank fluidTank;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    public FluidTankTier tier;

    public int prevAmount;

    public int valve;
    @Nonnull
    public FluidStack valveFluid = FluidStack.EMPTY;

    public float prevScale;

    public boolean needsPacket;

    private FluidInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;

    private boolean updateClientLight = false;

    public TileEntityFluidTank(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockFluidTank) getBlockType()).getTier();
        fluidTank = tier == FluidTankTier.CREATIVE ? new CreativeFluidTank() : new FluidTank(tier.getStorage());
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(inputSlot = FluidInventorySlot.input(new StackedFluidHandler(), fluid -> true, this, 146, 19), RelativeSide.TOP);
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 146, 51), RelativeSide.BOTTOM);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (isRemote()) {
            //TODO: Unify scale code into its own object/helper class so that we can use the same calculations everywhere
            // and also make sure we include the override for rendering contents even when there is less than 0.01 for the
            // scale they would be
            float targetScale = (float) fluidTank.getFluidAmount() / fluidTank.getCapacity();
            if (Math.abs(prevScale - targetScale) > 0.01) {
                prevScale = (9 * prevScale + targetScale) / 10;
            } else if (!fluidTank.isEmpty() && prevScale == 0) {
                //If we have any fluid in the tank make sure we end up rendering it
                prevScale = targetScale;
            }
            if (updateClientLight) {
                MekanismUtils.recheckLighting(world, pos);
                updateClientLight = false;
            }
        } else {
            if (valve > 0) {
                valve--;
                if (valve == 0) {
                    valveFluid = FluidStack.EMPTY;
                    needsPacket = true;
                }
            }

            if (fluidTank.getFluidAmount() != prevAmount) {
                markDirty();
                needsPacket = true;
                if (prevAmount == 0 || fluidTank.getFluidAmount() == 0) {
                    //If it was empty and no longer is, or wasn't empty and now is empty we want to recheck the block lighting
                    // as the fluid may have changed and have a light value
                    //TODO: Do we want to only bother doing this if the fluid *does* have a light value attached?
                    //TODO: Do we even need this on the sever side of things
                    MekanismUtils.recheckLighting(world, pos);
                }
            }

            prevAmount = fluidTank.getFluidAmount();
            inputSlot.handleTank(outputSlot, editMode);
            if (getActive()) {
                activeEmit();
            }
            if (needsPacket) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
            needsPacket = false;
        }
    }

    private void activeEmit() {
        if (!fluidTank.isEmpty()) {
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), pos.down());
            CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).ifPresent(handler -> {
                FluidStack toDrain = new FluidStack(fluidTank.getFluid(), Math.min(tier.getOutput(), fluidTank.getFluidAmount()));
                fluidTank.drain(handler.fill(toDrain, FluidAction.EXECUTE), tier == FluidTankTier.CREATIVE ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            });
        }
    }

    public int pushUp(@Nonnull FluidStack fluid, FluidAction fluidAction) {
        TileEntityFluidTank tile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, getWorld(), pos.up());
        if (tile != null) {
            Optional<IFluidHandler> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.DOWN));
            if (capability.isPresent()) {
                IFluidHandler handler = capability.get();
                if (PipeUtils.canFill(handler, fluid)) {
                    return handler.fill(fluid, fluidAction);
                }
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("editMode", editMode.ordinal());
        if (!fluidTank.isEmpty()) {
            nbtTags.put("fluidTank", fluidTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        editMode = ContainerEditMode.byIndexStatic(nbtTags.getInt("editMode"));
        //Needs to be outside the contains check because this is just based on the tier which is known information
        fluidTank.setCapacity(tier.getStorage());
        if (nbtTags.contains("fluidTank")) {
            fluidTank.readFromNBT(nbtTags.getCompound("fluidTank"));
        }
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            valve = dataStream.readInt();
            if (valve > 0) {
                valveFluid = dataStream.readFluidStack();
            } else {
                valveFluid = FluidStack.EMPTY;
            }
            fluidTank.setFluid(dataStream.readFluidStack());
            //Set the client's light to update just in case the value changed
            //TODO: Do we want to only bother doing this if the fluid *does* have a light value attached?
            updateClientLight = true;
        }
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    public int getCurrentNeeded() {
        if (tier == FluidTankTier.CREATIVE) {
            return Integer.MAX_VALUE;
        }
        int needed = fluidTank.getSpace();
        TileEntityFluidTank topTank = MekanismUtils.getTileEntity(TileEntityFluidTank.class, getWorld(), pos.up());
        if (topTank != null) {
            if (!fluidTank.isEmpty() && !topTank.fluidTank.isEmpty()) {
                if (!fluidTank.getFluid().isFluidEqual(topTank.fluidTank.getFluid())) {
                    return needed;
                }
            }
            if (topTank.tier == FluidTankTier.CREATIVE) {
                //Don't allow creative tanks to be taken into stacked amount as it causes weird things to occur
                return needed;
            }
            int aboveNeeded = topTank.getCurrentNeeded();
            if ((long) needed + aboveNeeded > Integer.MAX_VALUE) {
                //If we would overflow, just return we need max value
                return Integer.MAX_VALUE;
            }
            needed += aboveNeeded;
        }
        return needed;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(valve);
        if (valve > 0) {
            data.add(valveFluid);
        }
        data.add(fluidTank.getFluid());
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
        if (!isRemote()) {
            setActive(!getActive());
            World world = getWorld();
            if (world != null) {
                world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 1);
            }
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
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        if (tier == FluidTankTier.CREATIVE) {
            return resource.getAmount();
        }
        int filled = fluidTank.fill(resource, fluidAction);
        if (filled < resource.getAmount() && !getActive()) {
            filled += pushUp(new FluidStack(resource, resource.getAmount() - filled), fluidAction);
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

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return fluidTank.drain(maxDrain, tier == FluidTankTier.CREATIVE ? FluidAction.SIMULATE : fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        TileEntity tile = MekanismUtils.getTileEntity(getWorld(), getPos().down());
        if (from == Direction.DOWN && getActive() && !(tile instanceof TileEntityFluidTank)) {
            return false;
        }
        if (tier == FluidTankTier.CREATIVE) {
            return true;
        }
        if (getActive() && tile instanceof TileEntityFluidTank) { // Only fill if tanks underneath have same fluid.
            return fluidTank.isEmpty() ? ((TileEntityFluidTank) tile).canFill(Direction.UP, fluid) : fluidTank.getFluid().isFluidEqual(fluid);
        }
        return FluidContainerUtils.canFill(fluidTank.getFluid(), fluid);
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return fluidTank != null && FluidContainerUtils.canDrain(fluidTank.getFluid(), fluid) && !getActive() || from != Direction.DOWN;
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return new IFluidTank[]{fluidTank};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public void setFluidStack(@Nonnull FluidStack fluidStack, Object... data) {
        fluidTank.setFluid(fluidStack);
    }

    @Nonnull
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
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof FluidTankUpgradeData) {
            FluidTankUpgradeData data = (FluidTankUpgradeData) upgradeData;
            redstone = data.redstone;
            inputSlot.setStack(data.inputSlot.getStack());
            outputSlot.setStack(data.outputSlot.getStack());
            setContainerEditMode(data.editMode);
            fluidTank.setFluid(data.stored);
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    @Nonnull
    @Override
    public FluidTankUpgradeData getUpgradeData() {
        return new FluidTankUpgradeData(redstone, inputSlot, outputSlot, editMode, fluidTank.getFluid(), getComponents());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(ContainerEditMode::byIndexStatic, ContainerEditMode.BOTH, () -> editMode, value -> editMode = value));
        container.track(SyncableFluidStack.create(fluidTank));
    }

    private class StackedFluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return fluidTank.getTanks();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluidTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return fluidTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return fluidTank.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = fluidTank.fill(resource, action);
            //Push the fluid upwards
            if (filled < resource.getAmount() && !getActive()) {
                TileEntityFluidTank tile = MekanismUtils.getTileEntity(TileEntityFluidTank.class, getWorld(), pos.up());
                //Except if the above tank is creative as then weird things happen
                if (tile != null && tile.tier != FluidTankTier.CREATIVE) {
                    filled += pushUp(new FluidStack(resource, resource.getAmount() - filled), action);
                }
            }
            return filled;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return fluidTank.drain(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return fluidTank.drain(maxDrain, action);
        }
    }
}