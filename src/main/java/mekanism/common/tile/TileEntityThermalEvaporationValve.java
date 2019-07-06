package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock implements IFluidHandlerWrapper, IHeatTransfer, IComparatorSupport {

    public boolean prevMaster = false;
    private int currentRedstoneLevel;

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            if ((master == null) == prevMaster) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    Coord4D obj = Coord4D.get(this).offset(side);
                    if (obj.exists(world) && !obj.isAirBlock(world) && !(obj.getTileEntity(world) instanceof TileEntityThermalEvaporationBlock)) {
                        MekanismUtils.notifyNeighborofChange(world, obj, this.pos);
                    }
                }
            }
            prevMaster = master != null;
            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    @Override
    public int fill(EnumFacing from, @Nonnull FluidStack resource, boolean doFill) {
        TileEntityThermalEvaporationController controller = getController();
        return controller == null ? 0 : controller.inputTank.fill(resource, doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        TileEntityThermalEvaporationController controller = getController();
        return controller == null ? null : controller.outputTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, @Nonnull FluidStack fluid) {
        TileEntityThermalEvaporationController controller = getController();
        return controller != null && controller.hasRecipe(fluid.getFluid());
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        TileEntityThermalEvaporationController controller = getController();
        return controller != null && controller.outputTank.getFluidAmount() > 0 && FluidContainerUtils.canDrain(controller.outputTank.getFluid(), fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        TileEntityThermalEvaporationController controller = getController();
        if (controller == null) {
            return PipeUtils.EMPTY;
        }
        return new FluidTankInfo[]{new FluidTankInfo(controller.inputTank), new FluidTankInfo(controller.outputTank)};
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
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
    public double getInsulationCoefficient(EnumFacing side) {
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
    public boolean canConnectHeat(EnumFacing side) {
        return getController() != null;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getController() != null) ||
               super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.cast(this);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && getController() != null) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
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