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
     * @deprecated Use {@link #fromHolder(Holder, int)} instead
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    INGREDIENT from(TYPE instance, int amount);

    /**
     * Creates an Ingredient that matches any of the provided types.
     *
     * @param amount Amount needed.
     * @param items  Types to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one; or if no types are passed.
     * @since 10.6.0
     * @deprecated Use {@link #fromHolders(int, Holder[])} instead
     */
    @SuppressWarnings("unchecked")
    @Deprecated(forRemoval = true, since = "10.7.11")
    INGREDIENT from(int amount, TYPE... items);

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
     * Creates an Item Stack Ingredient that matches a provided items and amount.
     *
     * @param amount  Amount needed.
     * @param holders Types to match.
     *
     * @since 10.7.11
     */
    @SuppressWarnings("unchecked")
    default INGREDIENT fromHolders(int amount, Holder<TYPE>... holders) {
        //TODO - 1.22: Make this not be defaulted, we only default it on the off chance someone is implementing their own ingredient creator using this interface
        throw new UnsupportedOperationException("Not implemented yet");
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