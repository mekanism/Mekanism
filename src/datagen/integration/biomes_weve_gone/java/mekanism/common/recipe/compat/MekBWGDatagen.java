package mekanism.common.recipe.compat;

import net.minecraft.data.worldgen.BootstrapContextAccess;

public class MekBWGDatagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(BootstrapContextAccess contextAccess) {
        return new BWGRecipeProvider(contextAccess, modid());
    }

    @Override
    public String modid() {
        return "biomeswevegone";
    }
}