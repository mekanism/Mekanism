package mekanism.api.recipes.book;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.RecipeBookCategory;

/// Interface for managing common methods for mekanism book categories.
///
/// @since 10.8.0
public interface MekanismBookCategory extends StringRepresentable {

    RecipeBookCategory toRecipeBookCategory();
}