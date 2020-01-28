package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock implements IFluidHandlerWrapper, IHeatTransfer {

    public boolean prevMaster = false;

    public TileEntityThermalEvaporationValve() {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            if ((master == null) == prevMaster) {
                for (Direction side : EnumUtils.DIRECTIONS) {
                    BlockPos offset = pos.offset(side);
                    if (!world.isAirBlock(offset) && MekanismUtils.getTileEntity(TileEntityThermalEvaporationBlock.class, world, offset) == null) {
                        MekanismUtils.notifyNeighborofChange(world, offset, pos);
                    }
                }
            }
            prevMaster = master != null;
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        TileEntityThermalEvaporationController controller = getController();
        return controller == null ? 0 : controller.inputTank.fill(resource, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(Direction from, int maxDrain, FluidAction fluidAction) {
        TileEntityThermalEvaporationController controller = getController();
        return controller == null ? FluidStack.EMPTY : controller.outputTank.drain(maxDrain, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        TileEntityThermalEvaporationController controller = getController();
        return controller != null && controller.hasRecipe(fluid);
    }

    @Override
    public boolean canDrain(Direction from, @Nonnull FluidStack fluid) {
        TileEntityThermalEvaporationController controller = getController();
        return controller != null && controller.outputTank.getFluidAmount() > 0 && FluidContainerUtils.canDrain(controller.outputTank.getFluid(), fluid);
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        TileEntityThermalEvaporationController controller = getController();
        if (controller == null) {
            return PipeUtils.EMPTY;
        }
        return new IFluidTank[]{controller.inputTank, controller.outputTank};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public double getTemp() {
        return 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 1;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return 0;
    }

    @Override
    public void transferHeatTo(double heat) {
        TileEntityThermalEvaporationController controller = getController();
        if (controller != null) {
            controller.heatToAbsorb += heat;
        }
    }

    @Override
    public double[] simulateHeat() {
        return new double[]{0, 0};
    }

    @Override
    public double applyTemperatureChange() {
        return 0;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return getController() == null;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        TileEntityThermalEvaporationController controller = getController();
        if (controller != null) {
            return MekanismUtils.redstoneLevelFromContents(controller.inputTank.getFluidAmount(), controller.inputTank.getCapacity());
        }
        return 0;
    }
}