package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;

public interface IGasProvider extends IChemicalProvider<Gas> {

    @Nonnull
    @Override
    default GasStack getStack(long size) {
        return new GasStack(getChemical(), size);
    }
}