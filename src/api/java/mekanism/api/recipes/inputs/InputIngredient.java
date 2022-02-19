package mekanism.api.recipes.inputs;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import net.minecraft.network.PacketBuffer;

/**
 * Interface describing the base methods common to all inputs of our recipes.
 */
public interface InputIngredient<TYPE> extends Predicate<TYPE> {
    //TODO - 1.18: Evaluate moving most of our InputIngredient implementation details (item, fluid, chemicals) out of the API
    // and then have helpers to create the actual objects, as the API doesn't really have a need to know about the difference
    // between single, tagged, and multi

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
    default long getNeededAmount(TYPE type) {
        //TODO - 1.18: Remove default implementation, we only default this as to not be "breaking" if some
        // addon somehow has custom input ingredients
        return testType(type) ? 1 : 0;
    }

    /**
     * Primarily for JEI, a list of valid instances of the type
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     *
     * @apiNote Do not modify any of the values returned by the representations
     */
    @Nonnull
    List<TYPE> getRepresentations();

    /**
     * Writes this ingredient to a PacketBuffer.
     *
     * @param buffer The buffer to write to.
     */
    void write(PacketBuffer buffer);

    /**
     * Serializes this ingredient to a JsonElement
     *
     * @return JsonElement representation of this ingredient.
     */
    @Nonnull
    JsonElement serialize();
}