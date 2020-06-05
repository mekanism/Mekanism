package mekanism.api.providers;

import javax.annotation.Nonnull;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;

public interface ISlurryProvider extends IChemicalProvider<Slurry> {

    @Nonnull
    @Override
    default SlurryStack getStack(long size) {
        return new SlurryStack(getChemical(), size);
    }
}