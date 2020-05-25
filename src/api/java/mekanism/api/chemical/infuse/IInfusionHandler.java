package mekanism.api.chemical.infuse;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalHandler;

public interface IInfusionHandler extends IChemicalHandler<InfuseType, InfusionStack> {

    @Nonnull
    @Override
    default InfusionStack getEmptyStack() {
        return InfusionStack.EMPTY;
    }
}