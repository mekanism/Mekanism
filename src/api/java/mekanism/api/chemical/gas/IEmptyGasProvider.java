package mekanism.api.chemical.gas;

import mekanism.api.chemical.IEmptyStackProvider;
import org.jetbrains.annotations.NotNull;

public interface IEmptyGasProvider extends IEmptyStackProvider<Gas, GasStack> {

    @NotNull
    @Override
    default GasStack getEmptyStack() {
        return GasStack.EMPTY;
    }
}