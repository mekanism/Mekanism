package mekanism.api.recipes.inputs;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;

public interface InputIngredient<TYPE> extends Predicate<TYPE> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param t the input argument
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@NonNull TYPE t);

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     */
    //TODO: Make a note after checking some stuff but this should either allow them to be mutable or specifically say
    // not to attempt to mutate them
    @NonNull List<TYPE> getRepresentations();
}