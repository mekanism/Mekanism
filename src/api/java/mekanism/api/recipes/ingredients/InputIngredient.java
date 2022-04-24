package mekanism.api.recipes.ingredients;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Interface describing the base methods common to all inputs of our recipes.
 */
@MethodsReturnNonnullByDefault
public interface InputIngredient<TYPE> extends Predicate<TYPE> {

    /**
     * Evaluates this predicate on the given argument, ignoring any size data.
     *
     * @param type Input argument.
     *
     * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
     */
    boolean testType(@Nonnull TYPE type);

    /**
     * Gets a copy of the internal instance that matches the given argument.
     *
     * @param type Input argument.
     *
     * @return Matching instance. The returned value can be safely modified after.
     */
    TYPE getMatchingInstance(TYPE type);

    /**
     * Gets the amount of the given argument that is needed, or zero if the given argument doesn't match.
     *
     * @param type Input argument.
     *
     * @return Amount of the given argument that is needed.
     */
    long getNeededAmount(TYPE type);

    /**
     * Checks if this ingredient has any matching instances, in most cases this should be {@code false}, but for cases like tags this may not always be the case.
     *
     * @return {@code true} for no matching instances, {@code false} if there are any matching instances.
     *
     * @implNote Defaults to checking that {@link #getRepresentations()} is empty, but usually this will be overridden with a more efficient implementation.
     */
    default boolean hasNoMatchingInstances() {
        return getRepresentations().isEmpty();
    }

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     *
     * @apiNote Do not modify any of the values returned by the representations
     */
    List<TYPE> getRepresentations();

    /**
     * Writes this ingredient to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    void write(FriendlyByteBuf buffer);

    /**
     * Serializes this ingredient to a JsonElement
     *
     * @return JsonElement representation of this ingredient.
     */
    JsonElement serialize();
}