package mekanism.common.base;

import javax.annotation.Nonnull;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public abstract class MultiblockFluidTank<MULTIBLOCK extends TileEntityMultiblock<?>> extends FluidTank {

    protected final MULTIBLOCK multiblock;

    protected MultiblockFluidTank(MULTIBLOCK multiblock) {
        super(0);
        this.multiblock = multiblock;
    }

    //Note: We allow getFluid, setFluid, getFluidAmount, and isEmpty to check against the actual stored fluid even if we don't
    // have the structure set yet, to ensure we are able to properly load data

    @Override
    public boolean isFluidValid(@Nonnull FluidStack stack) {
        if (multiblock.structure == null) {
            return false;
        }
        return isEmpty() || getFluid().isFluidEqual(stack);
    }

    protected abstract void updateValveData();

    @Override
    public int fill(@Nonnull FluidStack resource, FluidAction fluidAction) {
        if (multiblock.structure == null || resource.isEmpty()) {
            return 0;
        }
        FluidStack fluidStack = getFluid();
        if (!fluidStack.isEmpty() && !fluidStack.isFluidEqual(resource)) {
            return 0;
        }
        if (fluidStack.isEmpty()) {
            if (resource.getAmount() <= getCapacity()) {
                if (fluidAction.execute() && !multiblock.getWorld().isRemote()) {
                    setFluid(fluidStack = resource.copy());
                    if (!resource.isEmpty()) {
                        MekanismUtils.saveChunk(multiblock);
                        updateValveData();
                    }
                }
                return resource.getAmount();
            }
            if (fluidAction.execute() && !multiblock.getWorld().isRemote()) {
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
            if (fluidAction.execute() && !multiblock.getWorld().isRemote()) {
                fluidStack.setAmount(fluidStack.getAmount() + resource.getAmount());
                if (!resource.isEmpty()) {
                    MekanismUtils.saveChunk(multiblock);
                    updateValveData();
                }
            }
            return resource.getAmount();
        }
        if (fluidAction.execute() && !multiblock.getWorld().isRemote()) {
            fluidStack.setAmount(fluidStack.getAmount() + getCapacity());
            if (needed > 0) {
                MekanismUtils.saveChunk(multiblock);
                updateValveData();
            }
        }
        return needed;
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
        if (multiblock.structure == null) {
            return FluidStack.EMPTY;
        }
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
        if (!drained.isEmpty() && fluidAction.execute() && !multiblock.getWorld().isRemote()) {
            MekanismUtils.saveChunk(multiblock);
            multiblock.sendPacketToRenderer();
        }
        return drained;
    }

    @Override
    public abstract int getCapacity();
}