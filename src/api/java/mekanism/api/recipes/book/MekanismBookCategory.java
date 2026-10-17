package mekanism.api.recipes.book;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus.Internal;

/// Interface for managing common methods for mekanism book categories.
///
/// @since 10.8.0
public interface MekanismBookCategory extends StringRepresentable {

    /// Converts this category to the corresponding vanilla Recipe Book Category object.
    RecipeBookCategory toRecipeBookCategory();

    /// Creates a holder pointing to a vanilla recipe book category
    @Internal
    static Holder<RecipeBookCategory> createHolder(Identifier recipeType, String category) {
        return DeferredHolder.create(Registries.RECIPE_BOOK_CATEGORY, recipeType.withSuffix("_" + category));
    }
}