package mekanism.common.content.tank;

import mekanism.api.Coord4D;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public class DynamicFluidTank implements IFluidTank {

    public TileEntityDynamicTank dynamicTank;

    public DynamicFluidTank(TileEntityDynamicTank tileEntity) {
        dynamicTank = tileEntity;
    }

    @Override
    public FluidStack getFluid() {
        return dynamicTank.structure != null ? dynamicTank.structure.fluidStored : null;
    }

    @Override
    public int getCapacity() {
        return dynamicTank.structure != null ? dynamicTank.structure.volume * TankUpdateProtocol.FLUID_PER_TANK : 0;
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (dynamicTank.structure != null && !dynamicTank.getWorld().isRemote) {
            if (resource == null || resource.getFluid() == null) {
                return 0;
            }

            if (dynamicTank.structure.fluidStored != null && !dynamicTank.structure.fluidStored
                  .isFluidEqual(resource)) {
                return 0;
            }

            if (dynamicTank.structure.fluidStored == null || dynamicTank.structure.fluidStored.getFluid() == null) {
                if (resource.amount <= getCapacity()) {
                    if (doFill) {
                        dynamicTank.structure.fluidStored = resource.copy();

                        if (resource.amount > 0) {
                            MekanismUtils.saveChunk(dynamicTank);
                            updateValveData();
                        }
                    }

                    return resource.amount;
                } else {
                    if (doFill) {
                        dynamicTank.structure.fluidStored = resource.copy();
                        dynamicTank.structure.fluidStored.amount = getCapacity();

                        if (getCapacity() > 0) {
                            MekanismUtils.saveChunk(dynamicTank);
                            updateValveData();
                        }
                    }

                    return getCapacity();
                }
            } else if (resource.amount <= getNeeded()) {
                if (doFill) {
                    dynamicTank.structure.fluidStored.amount += resource.amount;

                    if (resource.amount > 0) {
                        MekanismUtils.saveChunk(dynamicTank);
                        updateValveData();
                    }
                }

                return resource.amount;
            } else {
                int prevNeeded = getNeeded();

                if (doFill) {
                    dynamicTank.structure.fluidStored.amount = getCapacity();

                    if (prevNeeded > 0) {
                        MekanismUtils.saveChunk(dynamicTank);
                        updateValveData();
                    }
                }

                return prevNeeded;
            }
        }

        return 0;
    }

    public void updateValveData() {
        if (dynamicTank.structure != null) {
            for (ValveData data : dynamicTank.structure.valves) {
                if (data.location.equals(Coord4D.get(dynamicTank))) {
                    data.onTransfer();
                }
            }
        }
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (dynamicTank.structure != null && !dynamicTank.getWorld().isRemote) {
            if (dynamicTank.structure.fluidStored == null || dynamicTank.structure.fluidStored.getFluid() == null) {
                return null;
            }

            if (dynamicTank.structure.fluidStored.amount <= 0) {
                return null;
            }

            int used = maxDrain;

            if (dynamicTank.structure.fluidStored.amount < used) {
                used = dynamicTank.structure.fluidStored.amount;
            }

            if (doDrain) {
                dynamicTank.structure.fluidStored.amount -= used;
            }

            FluidStack drained = new FluidStack(dynamicTank.structure.fluidStored.getFluid(), used);

            if (dynamicTank.structure.fluidStored.amount <= 0) {
                dynamicTank.structure.fluidStored = null;
            }

            if (drained.amount > 0 && doDrain) {
                MekanismUtils.saveChunk(dynamicTank);
                dynamicTank.sendPacketToRenderer();
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
        if (dynamicTank.structure != null) {
            return dynamicTank.structure.fluidStored.amount;
        }

        return 0;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }
}
