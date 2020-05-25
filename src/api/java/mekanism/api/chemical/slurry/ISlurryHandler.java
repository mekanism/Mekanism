package mekanism.api.chemical.slurry;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IChemicalHandler;

public interface ISlurryHandler extends IChemicalHandler<Slurry, SlurryStack> {

    @Nonnull
    @Override
    default SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }
}