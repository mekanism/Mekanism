package mekanism.common.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

/**
 * Interface for helping split the recipe provider over multiple classes to make it a bit easier to interact with
 */
public interface ISubRecipeProvider {

    void addRecipes(RecipeOutput output, HolderLookup.Provider registries);
}