package mekanism.api.chemical.infuse;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IEmptyStackProvider;

public interface IEmptyInfusionProvider extends IEmptyStackProvider<InfuseType, InfusionStack> {

    @Nonnull
    @Override
    default InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }
}