package mekanism.generators.common.tile.turbine;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityTurbineVent extends TileEntityTurbineCasing implements IFluidHandlerWrapper {

    public IFluidTank fakeInfo = new FluidTank(1000);

    public TileEntityTurbineVent() {
        super(GeneratorsBlocks.TURBINE_VENT);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote() && structure != null && structure.flowRemaining > 0) {
            FluidStack fluidStack = new FluidStack(Fluids.WATER, structure.flowRemaining);
            EmitUtils.forEachSide(getWorld(), getPos(), EnumSet.allOf(Direction.class),
                  (tile, side) -> CapabilityUtils.getCapability(tile, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()).ifPresent(handler -> {
                      if (PipeUtils.canFill(handler, fluidStack)) {
                          structure.flowRemaining -= handler.fill(fluidStack, FluidAction.EXECUTE);
                      }
                  }));
        }
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        return ((!isRemote() && structure != null) || (isRemote() && clientHasStructure)) ? new IFluidTank[]{fakeInfo} : PipeUtils.EMPTY;
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        int amount = Math.min(maxDrain, structure.flowRemaining);
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = new FluidStack(Fluids.WATER, amount);
        if (fluidAction.execute()) {
            structure.flowRemaining -= amount;
        }
        return fluidStack;
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        return structure != null && (fluid.isEmpty() || fluid.getFluid() == Fluids.WATER);
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
}