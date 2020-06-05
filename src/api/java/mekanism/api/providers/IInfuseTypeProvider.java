package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;

public interface IInfuseTypeProvider extends IChemicalProvider<InfuseType> {

    @Nonnull
    @Override
    default InfusionStack getStack(long size) {
        return new InfusionStack(getChemical(), size);
    }
}