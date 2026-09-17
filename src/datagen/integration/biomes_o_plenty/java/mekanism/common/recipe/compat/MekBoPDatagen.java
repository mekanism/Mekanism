package mekanism.common.recipe.compat;

import net.minecraft.data.worldgen.BootstrapContextAccess;

public class MekBoPDatagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(BootstrapContextAccess contextAccess) {
        return new BiomesOPlentyRecipeProvider(contextAccess, modid());
    }

    @Override
    public String modid() {
        return "biomesoplenty";
    }
}