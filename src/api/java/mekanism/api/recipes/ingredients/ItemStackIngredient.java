package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base implementation for how Mekanism handle's ItemStack Ingredients.
 * <p>
 * Create instances of this using {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()}.
 *
 * @implNote This is a wrapper around {@link SizedIngredient}
 */
@NothingNullByDefault
public final class ItemStackIngredient implements InputIngredient<@NotNull ItemStack> {

    /**
     * A codec which can (de)encode item stack ingredients.
     *
     * @since 10.6.0
     */
    public static final Codec<ItemStackIngredient> CODEC = SizedIngredient.FLAT_CODEC.xmap(ItemStackIngredient::new, ItemStackIngredient::ingredient);
    /**
     * A stream codec which can be used to encode and decode item stack ingredients over the network.
     *
     * @since 10.6.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackIngredient> STREAM_CODEC = SizedIngredient.STREAM_CODEC
          .map(ItemStackIngredient::new, ItemStackIngredient::ingredient);

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient and amount. Prefer calling via
     * {@link mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()} and
     * {@link mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator#from(SizedIngredient)}.
     *
     * @param ingredient Sized ingredient to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    public static ItemStackIngredient of(SizedIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
        if (ingredient.ingredient().isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty ingredient.");
        }
        return new ItemStackIngredient(ingredient);
    }

    private final SizedIngredient ingredient;
    @Nullable
    private List<ItemStack> representations;

    private ItemStackIngredient(SizedIngredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean test(ItemStack stack) {
        Objects.requireNonNull(stack);
        return ingredient.test(stack);
    }

    @Override
    public boolean testType(ItemStack stack) {
        Objects.requireNonNull(stack);
        return ingredient.ingredient().test(stack);
    }

    @Override
    public ItemStack getMatchingInstance(ItemStack stack) {
        return test(stack) ? stack.copyWithCount(ingredient.count()) : ItemStack.EMPTY;
    }

    @Override
    public long getNeededAmount(ItemStack stack) {
        return testType(stack) ? ingredient.count() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.ingredient().hasNoItems();
    }

    @Override
    public void logMissingTags() {
        if (hasNoMatchingInstances()) {
            Ingredient unsized = ingredient.ingredient();
            if (unsized.isSimple()) {
                if (unsized.isEmpty()) {
                    MekanismAPI.logger.error("Empty ingredient: {}", unsized);
                } else {
                    for (Value ingredientValue : unsized.getValues()) {
                        if (ingredientValue instanceof TagValue tagValue) {
                            MekanismAPI.logger.error("Empty tag: {}", tagValue);
                        } else {
                            MekanismAPI.logger.warn("Unknown value: {}", ingredientValue);
                        }
                    }
                }
            } else {
                MekanismAPI.logger.error("Empty ItemStackIngredient: {}", SerializerHelper.stringify(Ingredient.CODEC, unsized));
            }
        }
    }

    @Override
    public List<@NotNull ItemStack> getRepresentations() {
        if (this.representations == null) {
            //TODO: See if quark or whatever mods used to occasionally have empty stacks in their ingredients still do
            // if so we probably should filter them out of this
            this.representations = List.of(ingredient.getItems());
        }
        return representations;
    }

    /**
     * For use in recipe input caching. Gets the internal Neo Sized Ingredient.
     *
     * @since 10.6.0
     */
    @Internal
    public SizedIngredient ingredient() {
        return ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return ingredient.equals(((ItemStackIngredient) o).ingredient);
    }

    @Override
    public int hashCode() {
        return ingredient.hashCode();
    }

    @Override
    public String toString() {
        return ingredient.toString();
    }
}