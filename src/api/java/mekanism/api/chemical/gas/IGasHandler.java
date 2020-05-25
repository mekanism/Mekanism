package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalHandler;

public interface IGasHandler extends IChemicalHandler<Gas, GasStack> {

    @Nonnull
    @Override
    default GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }
}