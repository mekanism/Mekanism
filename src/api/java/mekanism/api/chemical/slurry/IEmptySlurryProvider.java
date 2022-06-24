package mekanism.api.chemical.slurry;

import mekanism.api.chemical.IEmptyStackProvider;
import org.jetbrains.annotations.NotNull;

public interface IEmptySlurryProvider extends IEmptyStackProvider<Slurry, SlurryStack> {

    @NotNull
    @Override
    default SlurryStack getEmptyStack() {
        return SlurryStack.EMPTY;
    }
}