package mekanism.common.base;

import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

public abstract class MultiblockFluidTank<MULTIBLOCK extends TileEntityMultiblock> implements IFluidTank {

    protected final MULTIBLOCK multiblock;

    protected MultiblockFluidTank(MULTIBLOCK multiblock) {
        this.multiblock = multiblock;
    }

    public abstract void setFluid(FluidStack stack);

    protected abstract void updateValveData();

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (multiblock.structure != null && !multiblock.getWorld().isRemote) {
            if (resource == null || resource.getFluid() == null) {
                return 0;
            }
            FluidStack fluidStack = getFluid();
            if (fluidStack != null && !fluidStack.isFluidEqual(resource)) {
                return 0;
            }
            if (fluidStack == null || fluidStack.getFluid() == null) {
                if (resource.amount <= getCapacity()) {
                    if (doFill) {
                        setFluid(fluidStack = resource.copy());
                        if (resource.amount > 0) {
                            MekanismUtils.saveChunk(multiblock);
                            updateValveData();
                        }
                    }
                    return resource.amount;
                }
                if (doFill) {
                    setFluid(fluidStack = resource.copy());
                    fluidStack.amount = getCapacity();
                    if (getCapacity() > 0) {
                        MekanismUtils.saveChunk(multiblock);
                        updateValveData();
                    }
                }
                return getCapacity();
            }
            int needed = getCapacity() - fluidStack.amount;
            if (resource.amount <= needed) {
                if (doFill) {
                    fluidStack.amount += resource.amount;
                    if (resource.amount > 0) {
                        MekanismUtils.saveChunk(multiblock);
                        updateValveData();
                    }
                }
                return resource.amount;
            }
            if (doFill) {
                fluidStack.amount = getCapacity();
                if (needed > 0) {
                    MekanismUtils.saveChunk(multiblock);
                    updateValveData();
                }
            }
            return needed;
        }
        return 0;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (multiblock.structure != null && !multiblock.getWorld().isRemote) {
            FluidStack fluidStack = getFluid();
            if (fluidStack == null || fluidStack.getFluid() == null) {
                return null;
            }
            if (fluidStack.amount <= 0) {
                return null;
            }
            int used = maxDrain;
            if (fluidStack.amount < used) {
                used = fluidStack.amount;
            }
            if (doDrain) {
                fluidStack.amount -= used;
            }
            FluidStack drained = new FluidStack(fluidStack, used);
            if (fluidStack.amount <= 0) {
                setFluid(null);
            }
            if (drained.amount > 0 && doDrain) {
                MekanismUtils.saveChunk(multiblock);
                multiblock.sendPacketToRenderer();
            }
            return drained;
        }
        return null;
    }

    @Override
    public int getFluidAmount() {
        if (multiblock.structure != null) {
            FluidStack fluid = getFluid();
            return fluid == null ? 0 : fluid.amount;
        }
        return 0;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(this);
    }
}