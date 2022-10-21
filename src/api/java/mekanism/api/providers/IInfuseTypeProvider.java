package mekanism.api.providers;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import org.jetbrains.annotations.NotNull;

public interface IInfuseTypeProvider extends IChemicalProvider<InfuseType> {

    @NotNull
    @Override
    default InfusionStack getStack(long size) {
        return new InfusionStack(getChemical(), size);
    }
}