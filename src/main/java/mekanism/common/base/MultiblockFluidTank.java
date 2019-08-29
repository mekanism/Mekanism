package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public abstract class MultiblockFluidTank<MULTIBLOCK extends TileEntityMultiblock> implements IFluidTank {

    protected final MULTIBLOCK multiblock;

    protected MultiblockFluidTank(MULTIBLOCK multiblock) {
        this.multiblock = multiblock;
    }

    public abstract void setFluid(@Nonnull FluidStack stack);

    protected abstract void updateValveData();

    @Override
    public int fill(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (multiblock.structure != null && !multiblock.getWorld().isRemote) {
            if (resource.isEmpty()) {
                return 0;
            }
            FluidStack fluidStack = getFluid();
            if (!fluidStack.isEmpty() && !fluidStack.isFluidEqual(resource)) {
                return 0;
            }
            if (fluidStack.isEmpty()) {
                if (resource.getAmount() <= getCapacity()) {
                    if (fluidAction.execute()) {
                        setFluid(fluidStack = resource.copy());
                        if (resource.getAmount() > 0) {
                            MekanismUtils.saveChunk(multiblock);
                            updateValveData();
                        }
                    }
                    return resource.getAmount();
                }
                if (fluidAction.execute()) {
                    setFluid(fluidStack = resource.copy());
                    fluidStack.setAmount(getCapacity());
                    if (getCapacity() > 0) {
                        MekanismUtils.saveChunk(multiblock);
                        updateValveData();
                    }
                }
                return getCapacity();
            }
            int needed = getCapacity() - fluidStack.getAmount();
            if (resource.getAmount() <= needed) {
                if (fluidAction.execute()) {
                    fluidStack.setAmount(fluidStack.getAmount() + resource.getAmount());
                    if (resource.getAmount() > 0) {
                        MekanismUtils.saveChunk(multiblock);
                        updateValveData();
                    }
                }
                return resource.getAmount();
            }
            if (fluidAction.execute()) {
                fluidStack.setAmount(fluidStack.getAmount() + getCapacity());
                if (needed > 0) {
                    MekanismUtils.saveChunk(multiblock);
                    updateValveData();
                }
            }
            return needed;
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction fluidAction) {
        FluidStack fluid = getFluid();
        if (fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }
        FluidStack copy = fluid.copy();
        copy.setAmount(maxDrain);
        return drain(copy, fluidAction);
    }

    @Nonnull
    @Override
    public FluidStack drain(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (multiblock.structure != null && !multiblock.getWorld().isRemote) {
            FluidStack fluidStack = getFluid();
            if (fluidStack.isEmpty()) {
                return FluidStack.EMPTY;
            }
            int used = resource.getAmount();
            if (fluidStack.getAmount() < used) {
                used = fluidStack.getAmount();
            }
            if (fluidAction.execute()) {
                fluidStack.setAmount(fluidStack.getAmount() - used);
            }
            FluidStack drained = new FluidStack(fluidStack, used);
            if (fluidStack.isEmpty()) {
                setFluid(FluidStack.EMPTY);
            }
            if (drained.getAmount() > 0 && fluidAction.execute()) {
                MekanismUtils.saveChunk(multiblock);
                multiblock.sendPacketToRenderer();
            }
            return drained;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getFluidAmount() {
        if (multiblock.structure != null) {
            return getFluid().getAmount();
        }
        return 0;
    }
}