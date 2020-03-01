package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityDynamicValve extends TileEntityDynamicTank implements IFluidHandlerWrapper {

    public TileEntityDynamicValve() {
        super(MekanismBlocks.DYNAMIC_VALVE);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) ? new IFluidTank[]{structure.fluidTank} : PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        if (structure == null) {
            return 0;
        }
        return structure.fluidTank.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        if (structure == null) {
            return FluidStack.EMPTY;
        }
        return structure.fluidTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return (!isRemote() && structure != null) || (isRemote() && clientHasStructure);
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) && !structure.fluidTank.isEmpty() &&
               (fluid.isEmpty() || structure.fluidTank.getFluid().isFluidEqual(fluid));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return structure == null ? 0 : MekanismUtils.redstoneLevelFromContents(structure.fluidTank.getFluidAmount(), structure.fluidTank.getCapacity());
    }
}