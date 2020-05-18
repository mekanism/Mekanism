package mekanism.api.chemical.pigment;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandlerWrapper;

/**
 * Helper interface for wrapping a {@link IPigmentHandler} into a generic chemical handler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentHandlerWrapper implements IChemicalHandlerWrapper<Pigment, PigmentStack> {

    @Nonnull
    private final IPigmentHandler handler;

    public PigmentHandlerWrapper(IPigmentHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.getPigmentTankCount();
    }

    @Override
    public PigmentStack getChemicalInTank(int tank) {
        return handler.getPigmentInTank(tank);
    }

    @Override
    public void setChemicalInTank(int tank, PigmentStack stack) {
        handler.setPigmentInTank(tank, stack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getPigmentTankCapacity(tank);
    }

    @Override
    public boolean isChemicalValid(int tank, PigmentStack stack) {
        return handler.isPigmentValid(tank, stack);
    }

    @Override
    public PigmentStack insertChemical(int tank, PigmentStack stack, Action action) {
        return handler.insertPigment(tank, stack, action);
    }

    @Override
    public PigmentStack extractChemical(int tank, long amount, Action action) {
        return handler.extractPigment(tank, amount, action);
    }

    @Override
    public PigmentStack insertChemical(PigmentStack stack, Action action) {
        return handler.insertPigment(stack, action);
    }

    @Override
    public PigmentStack extractChemical(long amount, Action action) {
        return handler.extractPigment(amount, action);
    }

    @Override
    public PigmentStack extractChemical(PigmentStack stack, Action action) {
        return handler.extractPigment(stack, action);
    }
}