package mekanism.api.recipes.inputs;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NonNull;

public interface InputPredicate<T> extends Predicate<T> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param t the input argument
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(T t);//TODO: Use this instead of test in some spots we currently use test

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     */
    @NonNull List<T> getRepresentations();
}