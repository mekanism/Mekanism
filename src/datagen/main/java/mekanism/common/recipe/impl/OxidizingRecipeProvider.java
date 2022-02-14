package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.recipes.FinishedRecipe;

class OxidizingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "oxidizing/";
        //Brine
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.BRINE.getStack(15)
        ).build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_LITHIUM),
              MekanismGases.LITHIUM.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
        //Sulfur dioxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFUR_DIOXIDE.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_dioxide"));
    }
}