package mekanism.api.chemical.slurry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandlerWrapper;

/**
 * Helper interface for wrapping a {@link ISlurryHandler} into a generic chemical handler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SlurryHandlerWrapper implements IChemicalHandlerWrapper<Slurry, SlurryStack> {

    @Nonnull
    private final ISlurryHandler handler;

    public SlurryHandlerWrapper(ISlurryHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.getSlurryTankCount();
    }

    @Override
    public SlurryStack getChemicalInTank(int tank) {
        return handler.getSlurryInTank(tank);
    }

    @Override
    public void setChemicalInTank(int tank, SlurryStack stack) {
        handler.setSlurryInTank(tank, stack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getSlurryTankCapacity(tank);
    }

    @Override
    public boolean isChemicalValid(int tank, SlurryStack stack) {
        return handler.isSlurryValid(tank, stack);
    }

    @Override
    public SlurryStack insertChemical(int tank, SlurryStack stack, Action action) {
        return handler.insertSlurry(tank, stack, action);
    }

    @Override
    public SlurryStack extractChemical(int tank, long amount, Action action) {
        return handler.extractSlurry(tank, amount, action);
    }

    @Override
    public SlurryStack insertChemical(SlurryStack stack, Action action) {
        return handler.insertSlurry(stack, action);
    }

    @Override
    public SlurryStack extractChemical(long amount, Action action) {
        return handler.extractSlurry(amount, action);
    }

    @Override
    public SlurryStack extractChemical(SlurryStack stack, Action action) {
        return handler.extractSlurry(stack, action);
    }
}