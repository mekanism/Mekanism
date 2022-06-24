package mekanism.api.providers;

import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import org.jetbrains.annotations.NotNull;

public interface IPigmentProvider extends IChemicalProvider<Pigment> {

    @NotNull
    @Override
    default PigmentStack getStack(long size) {
        return new PigmentStack(getChemical(), size);
    }
}