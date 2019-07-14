package mekanism.api.recipes;

import java.util.Collection;
import mekanism.api.annotations.NonNull;

/**
 * Recipe with a defined output definition (for JEI)
 */
public interface OutputDefinition<T> {
    @NonNull
    Collection<@NonNull T> getOutputDefinition();
}
