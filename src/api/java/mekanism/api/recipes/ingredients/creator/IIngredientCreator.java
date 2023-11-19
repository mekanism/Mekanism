package mekanism.api.recipes.ingredients.creator;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.stream.Stream;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IIngredientCreator<TYPE, STACK, INGREDIENT extends InputIngredient<@NotNull STACK>> {

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
     * Creates an Ingredient that matches a provided type and amount.
     *
     * @param instance Type to match.
     * @param amount   Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     *
     * @since 10.5.0
     */
    default INGREDIENT fromHolder(Holder<TYPE> instance, int amount) {
        return from(instance.value(), amount);
    }

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
    @Deprecated(forRemoval = true)
    default INGREDIENT deserialize(@Nullable JsonElement json) {
        return codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, e->{});
    }

    /**
     * Helper to serialize into a Json object
     *
     * @param ingredient the ingredient to serialize
     * @return the serialized ingredient
     * @throws RuntimeException if encoding failed
     */
    default JsonElement serialize(INGREDIENT ingredient) {
        return codec().encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow(false, unused->{});
    }

    /**
     * Retrieve a codec which can (de)encode a single or multi ingredient of this type.
     *
     * @return a codec for this ingredient type
     */
    Codec<INGREDIENT> codec();

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
    @SuppressWarnings("unchecked")
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