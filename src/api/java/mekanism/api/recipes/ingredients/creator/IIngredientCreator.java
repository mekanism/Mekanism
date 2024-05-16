package mekanism.api.recipes.ingredients.creator;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

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
     * Retrieve a codec which can (de)encode a single or multi ingredient of this type.
     *
     * @return a codec for this ingredient type
     *
     * @since 10.5.0
     */
    Codec<INGREDIENT> codec();

    /**
     * Retrieve a stream codec which can be used to encode and decode ingredients of this type over the network.
     *
     * @return a stream codec for this ingredient type
     *
     * @since 10.6.0
     */
    StreamCodec<RegistryFriendlyByteBuf, INGREDIENT> streamCodec();
}