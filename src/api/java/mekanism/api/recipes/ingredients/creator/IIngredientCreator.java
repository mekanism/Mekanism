package mekanism.api.recipes.ingredients.creator;

import com.google.gson.JsonElement;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IIngredientCreator<TYPE, STACK, INGREDIENT extends InputIngredient<@NonNull STACK>> {

    /**
     * Creates an Ingredient that matches a given stack.
     *
     * @param instance Stack to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     */
    INGREDIENT from(STACK instance);

    /**
     * Creates an Ingredient that matches a provided type and amount.
     *
     * @param instance Type to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     */
    INGREDIENT from(TYPE instance, int amount);

    /**
     * Creates an Ingredient that matches a given tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     *
     * @throws NullPointerException     if the given tag is null.
     * @throws IllegalArgumentException if the given amount smaller than one.
     */
    INGREDIENT from(TagKey<TYPE> tag, int amount);

    /**
     * Reads an Ingredient from a Packet Buffer.
     *
     * @param buffer Buffer to read from.
     *
     * @throws NullPointerException if the given buffer is null.
     */
    INGREDIENT read(FriendlyByteBuf buffer);

    /**
     * Helper to deserialize a Json Object into an Ingredient.
     *
     * @param json Json object to deserialize.
     *
     * @throws com.google.gson.JsonSyntaxException if the ingredient failed to deserialize or was invalid.
     */
    INGREDIENT deserialize(@Nullable JsonElement json);

    /**
     * Combines multiple Ingredients into a single Ingredient.
     *
     * @param ingredients Ingredients to combine.
     *
     * @return Combined Ingredient.
     *
     * @throws NullPointerException     if the given array is null.
     * @throws IllegalArgumentException if the given array is empty.
     */
    INGREDIENT createMulti(INGREDIENT... ingredients);

    /**
     * Creates an Ingredient out of a stream of Ingredients.
     *
     * @param ingredients Ingredient(s) to combine.
     *
     * @return Given Ingredient or Combined Ingredient if multiple were in the stream.
     *
     * @throws NullPointerException     if the given stream is null.
     * @throws IllegalArgumentException if the given stream is empty.
     */
    INGREDIENT from(Stream<INGREDIENT> ingredients);
}