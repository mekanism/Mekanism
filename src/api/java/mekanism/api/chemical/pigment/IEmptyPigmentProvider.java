package mekanism.api.chemical.pigment;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IEmptyStackProvider;

public interface IEmptyPigmentProvider extends IEmptyStackProvider<Pigment, PigmentStack> {

    @Nonnull
    @Override
    default PigmentStack getEmptyStack() {
        return PigmentStack.EMPTY;
    }
}