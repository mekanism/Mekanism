package mekanism.api.providers;

import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import org.jetbrains.annotations.NotNull;

public interface IGasProvider extends IChemicalProvider<Gas> {

    @NotNull
    @Override
    default GasStack getStack(long size) {
        return new GasStack(getChemical(), size);
    }
}