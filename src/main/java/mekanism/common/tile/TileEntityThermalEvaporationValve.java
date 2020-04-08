package mekanism.common.tile;

import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityThermalEvaporationValve extends TileEntityThermalEvaporationBlock implements IHeatTransfer {

    private boolean prevMaster = false;

    public TileEntityThermalEvaporationValve() {
        super(MekanismBlocks.THERMAL_EVAPORATION_VALVE);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        return side -> getController() == null ? Collections.emptyList() : getController().getFluidTanks(side);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
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

    @Override
    public boolean persists(SubstanceType type) {
        //But that we do not handle fluid when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.FLUID) {
            return false;
        }
        return super.persists(type);
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
        if (capability == Capabilities.HEAT_HANDLER_CAPABILITY && getController() == null) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_HANDLER_CAPABILITY) {
            return Capabilities.HEAT_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        TileEntityThermalEvaporationController controller = getController();
        if (controller == null) {
            return 0;
        }
        return MekanismUtils.redstoneLevelFromContents(controller.inputTank.getFluidAmount(), controller.inputTank.getCapacity());
    }
}