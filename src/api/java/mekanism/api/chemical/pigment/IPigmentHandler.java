package mekanism.api.chemical.pigment;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalHandler;

public interface IPigmentHandler extends IChemicalHandler<Pigment, PigmentStack> {

    @Nonnull
    @Override
    default PigmentStack getEmptyStack() {
        return PigmentStack.EMPTY;
    }
}