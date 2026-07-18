package mekanism.api.recipes.ingredients;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializerHelper;
import mekanism.api.recipes.display.slot.WithAmountSlotDisplay;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

/// Base implementation for how Mekanism handle's ItemStack Ingredients.
///
/// Create instances of this using [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()].
///
/// @implNote This is a wrapper around [SizedIngredient]
public final class ItemStackIngredient implements InputIngredient<Item, ItemStack> {

    /// A codec which can (de)encode item stack ingredients.
    ///
    /// @since 10.6.0
    public static final Codec<ItemStackIngredient> CODEC = SizedIngredient.NESTED_CODEC.xmap(ItemStackIngredient::new, ItemStackIngredient::ingredient);
    /// A stream codec which can be used to encode and decode item stack ingredients over the network.
    ///
    /// @since 10.6.0
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackIngredient> STREAM_CODEC = SizedIngredient.STREAM_CODEC
          .map(ItemStackIngredient::new, ItemStackIngredient::ingredient);

    /// Creates an Item Stack Ingredient that matches a given ingredient and amount. Prefer calling via
    /// [mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess#item()] and
    /// [mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator#from(SizedIngredient)].
    ///
    /// @param ingredient Sized ingredient to match.
    ///
    /// @throws NullPointerException     if the given instance is null.
    /// @throws IllegalArgumentException if the given instance is empty.
    /// @since 10.6.0
    public static ItemStackIngredient of(SizedIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
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
    public boolean testType(TypedInstance<Item> instance) {
        Objects.requireNonNull(instance);
        Ingredient vanillaIngredient = ingredient.ingredient();
        //Component data might be necessary, make it into a stack and test it
        return vanillaIngredient.test(IngredientCreatorAccess.item().createStack(instance));
    }

    @Override
    public ItemStack getMatchingInstance(ItemStack stack) {
        return test(stack) ? stack.copyWithCount(ingredient.count()) : ItemStack.EMPTY;
    }

    @Override
    public int getNeededAmount(TypedInstance<Item> instance) {
        return testType(instance) ? ingredient.count() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return ingredient.ingredient().isEmpty();
    }

    @Override
    public void logMissingTags() {
        //TODO - 26.2: Re-evaluate this implementation
        if (hasNoMatchingInstances()) {
            Ingredient unsized = ingredient.ingredient();
            if (unsized.isCustom()) {
                MekanismAPI.logger.error("Empty ItemStackIngredient: {}", SerializerHelper.stringify(Ingredient.CODEC, unsized));
            } else {
                Optional<TagKey<Item>> fluidTagKey = unsized.getValues().unwrapKey();
                if (fluidTagKey.isPresent()) {
                    MekanismAPI.logger.error("Empty tag: {}", fluidTagKey.get());
                } else {
                    MekanismAPI.logger.error("Empty ItemStackIngredient: {}", SerializerHelper.stringify(Ingredient.CODEC, unsized));
                }
            }
        }
    }

    @Override
    public List<ItemStack> getRepresentations(ContextMap context) {
        if (this.representations == null) {
            this.representations = display().resolve(context, SlotDisplay.ItemStackContentsFactory.INSTANCE).toList();
        }
        return representations;
    }

    @Override
    public SlotDisplay display() {
        return new WithAmountSlotDisplay(ingredient.ingredient().display(), ingredient.count());
    }

    /// For use in recipe input caching. Gets the internal Neo Sized Ingredient.
    ///
    /// @since 10.6.0
    @Internal
    public SizedIngredient ingredient() {
        return ingredient;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
