package mekanism.common.recipe.compat;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

public class MekAe2Datagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider apply(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        return new AE2RecipeProvider(recipeOutput, advancementOutput, modid());
    }

    @Override
    public String modid() {
        return "ae2";
    }
}