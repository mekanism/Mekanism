package mekanism.api.recipes.book;

import java.util.Optional;
import mekanism.api.MekanismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// Interface for managing common methods for mekanism book categories.
///
/// @since 10.8.0
public interface MekanismBookCategory extends StringRepresentable {

    /// Converts this category to the corresponding vanilla Recipe Book Category object.
    RecipeBookCategory toRecipeBookCategory();

    /// {@return the primary icon to be displayed for this category in the recipe book} Either an [net.minecraft.world.item.ItemStack],
    /// [net.neoforged.neoforge.transfer.fluid.FluidResource], or [mekanism.api.chemical.ChemicalResource]
    Object getPrimaryIcon(Provider holderLookup);

    /// {@return the secondary icon to be displayed for this category in the recipe book} Either an [net.minecraft.world.item.ItemStack],
    /// [net.neoforged.neoforge.transfer.fluid.FluidResource], or [mekanism.api.chemical.ChemicalResource]
    default Optional<Object> getSecondaryIcon(Provider holderLookup) {
        return Optional.empty();
    }

    @Nullable
    default String getAdvancementFolder() {
        return null;
    }

    /// Creates a holder pointing to a vanilla recipe book category
    @Internal
    static DeferredHolder<RecipeBookCategory, RecipeBookCategory> createHolder(Identifier recipeType, String category) {
        return DeferredHolder.create(Registries.RECIPE_BOOK_CATEGORY, recipeType.withSuffix("_" + category));
    }

    /// Creates a resource key pointing to a mekanism item
    @Nullable
    @Internal
    @Contract("!null -> !null")
    static ResourceKey<Item> createMekanismKey(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, name));
    }

    /// Creates a holder pointing to an item
    @Nullable
    @Internal
    @Contract("!null -> !null")
    static Holder<Item> createItem(@Nullable ResourceKey<Item> key) {
        if (key == null) {
            return null;
        }
        return DeferredHolder.create(key);
    }
}