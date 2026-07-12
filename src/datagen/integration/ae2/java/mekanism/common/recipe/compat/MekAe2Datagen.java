package mekanism.common.recipe.compat;

import net.minecraft.core.HolderLookup.Provider;

public class MekAe2Datagen implements IMekAe2Datagen {

    @Override
    public CompatRecipeProvider recipeProvider(Provider registries, String modid) {
        return new AE2RecipeProvider(registries, modid);
    }
}