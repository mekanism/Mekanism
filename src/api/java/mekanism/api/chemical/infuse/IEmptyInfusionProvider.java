package mekanism.api.chemical.infuse;

import mekanism.api.chemical.IEmptyStackProvider;
import org.jetbrains.annotations.NotNull;

public interface IEmptyInfusionProvider extends IEmptyStackProvider<InfuseType, InfusionStack> {

    @NotNull
    @Override
    default InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }
}