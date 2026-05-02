package mekanism.api.recipes.ingredients.creator;

import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.InputIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public interface IIngredientCreator<TYPE, STACK, INGREDIENT extends InputIngredient<@NotNull STACK>> {//TODO - 26.1: Add helpers that take TypedInstance?

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
     * @since 10.5.0
     */
    INGREDIENT fromHolder(Holder<TYPE> instance, int amount);

    /**
     * Creates an Item Stack Ingredient that matches a provided items and amount.
     *
     * @param amount  Amount needed.
     * @param holders Types to match.
     *
     * @since 10.7.11
     */
    @SuppressWarnings("unchecked")
    INGREDIENT fromHolders(int amount, Holder<TYPE>... holders);

    /**
     * Creates an Ingredient that matches a given tag and amount.
     *
     * @param tag    Tag to match.
     * @param amount Amount needed.
     *
     * @throws NullPointerException     if the given tag is null.
     * @throws IllegalArgumentException if the given amount smaller than one.
     */
    INGREDIENT from(HolderGetter<TYPE> lookup, TagKey<TYPE> tag, int amount);//TODO - 26.1: Add docs for lookup param

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