package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandlerWrapper;

/**
 * Helper interface for wrapping a {@link IGasHandler} into a generic chemical handler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasHandlerWrapper implements IChemicalHandlerWrapper<Gas, GasStack> {

    @Nonnull
    private final IGasHandler handler;

    public GasHandlerWrapper(IGasHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.getGasTankCount();
    }

    @Override
    public GasStack getChemicalInTank(int tank) {
        return handler.getGasInTank(tank);
    }

    @Override
    public void setChemicalInTank(int tank, GasStack stack) {
        handler.setGasInTank(tank, stack);
    }

    @Override
    public long getTankCapacity(int tank) {
        return handler.getGasTankCapacity(tank);
    }

    @Override
    public boolean isChemicalValid(int tank, GasStack stack) {
        return handler.isGasValid(tank, stack);
    }

    @Override
    public GasStack insertChemical(int tank, GasStack stack, Action action) {
        return handler.insertGas(tank, stack, action);
    }

    @Override
    public GasStack extractChemical(int tank, long amount, Action action) {
        return handler.extractGas(tank, amount, action);
    }

    @Override
    public GasStack insertChemical(GasStack stack, Action action) {
        return handler.insertGas(stack, action);
    }

    @Override
    public GasStack extractChemical(long amount, Action action) {
        return handler.extractGas(amount, action);
    }

    @Override
    public GasStack extractChemical(GasStack stack, Action action) {
        return handler.extractGas(stack, action);
    }
}