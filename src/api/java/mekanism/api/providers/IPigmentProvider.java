package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;

public interface IPigmentProvider extends IChemicalProvider<Pigment> {

    @Nonnull
    @Override
    default PigmentStack getStack(long size) {
        return new PigmentStack(getChemical(), size);
    }
}