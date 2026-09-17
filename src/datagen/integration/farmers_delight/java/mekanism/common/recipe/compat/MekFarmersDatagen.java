package mekanism.common.recipe.compat;

import net.minecraft.data.worldgen.BootstrapContextAccess;

public class MekFarmersDatagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider recipeProvider(BootstrapContextAccess contextAccess) {
        return new FarmersDelightRecipeProvider(contextAccess, modid());
    }

    @Override
    public String modid() {
        return "farmersdelight";
    }
}