package mekanism.common.recipe.compat;

import net.minecraft.core.HolderLookup.Provider;

public class MekBoPDatagen implements IMekBoPDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(Provider registries, String modid) {
        return new BiomesOPlentyRecipeProvider(registries, modid);
    }
}