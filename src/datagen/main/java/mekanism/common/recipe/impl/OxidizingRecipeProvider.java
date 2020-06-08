package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
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
        ItemStackToChemicalRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.BRINE.getStack(15)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        ItemStackToChemicalRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_LITHIUM),
              MekanismGases.LITHIUM.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
        //Sulfur dioxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFUR_DIOXIDE.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_dioxide"));
    }
}