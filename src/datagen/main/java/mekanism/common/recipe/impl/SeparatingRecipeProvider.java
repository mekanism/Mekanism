package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;

class SeparatingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(MekanismTags.Fluids.BRINE, 10),
              MekanismGases.SODIUM.getStack(1),
              MekanismGases.CHLORINE.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              IngredientCreatorAccess.fluid().from(FluidTags.WATER, 2),
              MekanismGases.HYDROGEN.getStack(2),
              MekanismGases.OXYGEN.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "water"));
    }
}