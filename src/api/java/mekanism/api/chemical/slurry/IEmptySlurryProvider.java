package mekanism.api.chemical.slurry;

import javax.annotation.Nonnull;
import mekanism.api.chemical.IEmptyStackProvider;

public interface IEmptySlurryProvider extends IEmptyStackProvider<Slurry, SlurryStack> {

    @Nonnull
    @Override
    default SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }
}