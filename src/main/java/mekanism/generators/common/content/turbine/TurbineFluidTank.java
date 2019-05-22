package mekanism.generators.common.content.turbine;

import javax.annotation.Nullable;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class TurbineFluidTank implements IFluidTank {

    public TileEntityTurbineCasing turbine;

    public TurbineFluidTank(TileEntityTurbineCasing tileEntity) {
        turbine = tileEntity;
    }

    @Override
    @Nullable
    public FluidStack getFluid() {
        return turbine.structure != null ? turbine.structure.fluidStored : null;
    }

    @Override
    public int getCapacity() {
        return turbine.structure != null ? turbine.structure.getFluidCapacity() : 0;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (turbine.structure != null && !turbine.getWorld().isRemote) {
            if (resource == null || resource.getFluid() == null) {
                return 0;
            }
            if (turbine.structure.fluidStored != null && !turbine.structure.fluidStored.isFluidEqual(resource)) {
                return 0;
            }
            if (turbine.structure.fluidStored == null || turbine.structure.fluidStored.getFluid() == null) {
                if (resource.amount <= getCapacity()) {
                    if (doFill) {
                        turbine.structure.fluidStored = resource.copy();
                        if (resource.amount > 0) {
                            MekanismUtils.saveChunk(turbine);
                        }
                    }
                    return resource.amount;
                } else {
                    if (doFill) {
                        turbine.structure.fluidStored = resource.copy();
                        turbine.structure.fluidStored.amount = getCapacity();
                        if (getCapacity() > 0) {
                            MekanismUtils.saveChunk(turbine);
                        }
                    }
                    return getCapacity();
                }
            } else if (resource.amount <= getNeeded()) {
                if (doFill) {
                    turbine.structure.fluidStored.amount += resource.amount;
                    if (resource.amount > 0) {
                        MekanismUtils.saveChunk(turbine);
                    }
                }
                return resource.amount;
            } else {
                int prevNeeded = getNeeded();
                if (doFill) {
                    turbine.structure.fluidStored.amount = getCapacity();
                    if (prevNeeded > 0) {
                        MekanismUtils.saveChunk(turbine);
                    }
                }
                return prevNeeded;
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (turbine.structure != null && !turbine.getWorld().isRemote) {
            if (turbine.structure.fluidStored == null || turbine.structure.fluidStored.getFluid() == null) {
                return null;
            }
            if (turbine.structure.fluidStored.amount <= 0) {
                return null;
            }
            int used = maxDrain;
            if (turbine.structure.fluidStored.amount < used) {
                used = turbine.structure.fluidStored.amount;
            }
            if (doDrain) {
                turbine.structure.fluidStored.amount -= used;
            }
            FluidStack drained = new FluidStack(turbine.structure.fluidStored.getFluid(), used);
            if (turbine.structure.fluidStored.amount <= 0) {
                turbine.structure.fluidStored = null;
            }
            if (drained.amount > 0 && doDrain) {
                MekanismUtils.saveChunk(turbine);
                turbine.sendPacketToRenderer();
            }
            return drained;
        }
        return null;
    }

    public int getNeeded() {
        return getCapacity() - getFluidAmount();
    }

    @Override
    public int getFluidAmount() {
        if (turbine.structure != null && turbine.structure.fluidStored != null) {
            return turbine.structure.fluidStored.amount;
        }
        return 0;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }
}