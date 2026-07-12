package mekanism.common.recipe.compat;

import net.minecraft.core.HolderLookup.Provider;

public class MekBWGDatagen implements IMekBWGDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(Provider registries, String modid) {
        return new BWGRecipeProvider(registries, modid);
    }
}