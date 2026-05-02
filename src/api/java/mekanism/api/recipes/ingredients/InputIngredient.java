package mekanism.api.recipes.ingredients;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import net.minecraft.core.TypedInstance;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.NotNull;

/**
 * Interface describing the base methods common to all inputs of our recipes.
 */
@MethodsAreNotNullByDefault
public interface InputIngredient<HOLDERTYPE, STACK extends TypedInstance<HOLDERTYPE>> extends Predicate<STACK> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param type Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@NotNull TypedInstance<HOLDERTYPE> type);

    /**
     * Gets a copy of the internal instance that matches the given argument.
     *
     * @param type Input argument.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    STACK getMatchingInstance(STACK type);

    /**
     * Gets the amount of the given argument that is needed, or zero if the given argument doesn't match.
     *
     * @param type Input argument.
     *
     * @return Amount of the given argument that is needed.
     */
    long getNeededAmount(TypedInstance<HOLDERTYPE> type);

    /**
     * Checks if this ingredient has any matching instances, in most cases this should be {@code false}, but for cases like tags this may not always be the case.
     *
     * @return {@code true} for no matching instances, {@code false} if there are any matching instances.
     */
    boolean hasNoMatchingInstances();

    void logMissingTags();

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     *
     * @apiNote Do not modify any of the values returned by the representations
     */
    List<STACK> getRepresentations(ContextMap context);
}