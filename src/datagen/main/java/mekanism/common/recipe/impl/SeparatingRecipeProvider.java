package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.tags.FluidTags;

class SeparatingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(MekanismTags.Fluids.BRINE, 10),
              MekanismGases.SODIUM.getStack(1),
              MekanismGases.CHLORINE.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(FluidTags.WATER, 2),
              MekanismGases.HYDROGEN.getStack(2),
              MekanismGases.OXYGEN.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "water"));
    }
}