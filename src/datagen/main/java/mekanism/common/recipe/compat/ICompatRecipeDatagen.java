package mekanism.common.recipe.compat;

import net.minecraft.core.HolderLookup;

@FunctionalInterface
public interface ICompatRecipeDatagen {

    CompatRecipeProvider recipeProvider(HolderLookup.Provider registries, String modid);
}