package mekanism.api.chemical.gas;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IEmptyStackProvider;

public interface IEmptyGasProvider extends IEmptyStackProvider<Gas, GasStack> {

    @Nonnull
    @Override
    default GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }
}