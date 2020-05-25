package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToGasRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;

class OxidizingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "oxidizing/";
        //Brine
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.BRINE.getGasStack(15)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_LITHIUM),
              MekanismGases.LITHIUM.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
        //Sulfur dioxide
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFUR_DIOXIDE.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_dioxide"));
    }
}