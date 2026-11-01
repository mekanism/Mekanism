package mekanism.common.recipe.compat;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

public class MekBWGDatagen implements ICompatRecipeDatagen {

    @Override
    public CompatRecipeProvider apply(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        return new BWGRecipeProvider(recipeOutput, advancementOutput, modid());
    }

    @Override
    public String modid() {
        return "biomeswevegone";
    }
}