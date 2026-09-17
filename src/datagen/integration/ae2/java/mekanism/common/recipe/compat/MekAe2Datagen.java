package mekanism.common.recipe.compat;

import net.minecraft.data.worldgen.BootstrapContextAccess;

public class MekAe2Datagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(BootstrapContextAccess contextAccess) {
        return new AE2RecipeProvider(contextAccess, modid());
    }

    @Override
    public String modid() {
        return "ae2";
    }
}