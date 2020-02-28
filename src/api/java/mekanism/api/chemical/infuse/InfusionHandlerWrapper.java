package mekanism.api.chemical.infuse;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.IChemicalHandlerWrapper;

/**
 * Helper interface for wrapping a {@link IInfusionHandler} into a generic chemical handler.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfusionHandlerWrapper implements IChemicalHandlerWrapper<InfuseType, InfusionStack> {

    @Nonnull
    private final IInfusionHandler handler;

    public InfusionHandlerWrapper(IInfusionHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getTanks() {
        return handler.getInfusionTankCount();
    }

    @Override
    public InfusionStack getChemicalInTank(int tank) {
        return handler.getInfusionInTank(tank);
    }

    @Override
    public void setChemicalInTank(int tank, InfusionStack stack) {
        handler.setInfusionInTank(tank, stack);
    }

    @Override
    public int getTankCapacity(int tank) {
        return handler.getInfusionTankCapacity(tank);
    }

    @Override
    public boolean isChemicalValid(int tank, InfusionStack stack) {
        return handler.isInfusionValid(tank, stack);
    }

    @Override
    public InfusionStack insertChemical(int tank, InfusionStack stack, Action action) {
        return handler.insertInfusion(tank, stack, action);
    }

    @Override
    public InfusionStack extractChemical(int tank, int amount, Action action) {
        return handler.extractInfusion(tank, amount, action);
    }

    @Override
    public InfusionStack insertChemical(InfusionStack stack, Action action) {
        return handler.insertInfusion(stack, action);
    }

    @Override
    public InfusionStack extractChemical(int amount, Action action) {
        return handler.extractInfusion(amount, action);
    }

    @Override
    public InfusionStack extractChemical(InfusionStack stack, Action action) {
        return handler.extractInfusion(stack, action);
    }
}