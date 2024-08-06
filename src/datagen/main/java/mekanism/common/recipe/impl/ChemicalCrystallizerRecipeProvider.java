package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

class ChemicalCrystallizerRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "crystallizing/";
        //Salt
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.BRINE, 15),
              MekanismItems.SALT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "salt"));
        //Lithium
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "lithium"));
    }
}