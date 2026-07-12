package mekanism.common.recipe.compat;

import net.minecraft.core.HolderLookup.Provider;

public class MekFarmersDatagen implements IMekFarmersDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(Provider registries, String modid) {
        return new FarmersDelightRecipeProvider(registries, modid);
    }
}