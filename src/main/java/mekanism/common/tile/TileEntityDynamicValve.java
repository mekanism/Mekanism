package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.MekanismBlock;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.content.tank.DynamicFluidTank;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements IFluidHandlerWrapper, IComparatorSupport {

    public DynamicFluidTank fluidTank;
    private int currentRedstoneLevel;

    public TileEntityDynamicValve() {
        super(MekanismBlock.DYNAMIC_VALVE);
        fluidTank = new DynamicFluidTank(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) ? new IFluidTank[]{fluidTank} : PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return fluidTank.fill(resource, fluidAction);
    }

    @Override
    @Nullable
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        return fluidTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return (!world.isRemote && structure != null) || (world.isRemote && clientHasStructure);
    }

    @Override
    public boolean canDrain(Direction from, @Nullable FluidStack fluid) {
        return ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) && FluidContainerUtils.canDrain(structure.fluidStored, fluid);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if ((!world.isRemote && structure != null) || (world.isRemote && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return !world.isRemote ? structure == null : !clientHasStructure;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return (!world.isRemote && structure != null) || (world.isRemote && clientHasStructure) ? SLOTS : InventoryUtils.EMPTY;
    }

    @Override
    public boolean isItemValidForSlot(int slot, @Nonnull ItemStack stack) {
        //can be filled/emptied
        return slot == 0 && FluidContainerUtils.isFluidContainer(stack);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }
}