package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import net.minecraft.data.recipes.RecipeOutput;

class ChemicalInfuserRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "chemical_infusing/";
        //Hydrogen Chloride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gasStack().from(MekanismGases.HYDROGEN, 1),
              IngredientCreatorAccess.gasStack().from(MekanismGases.CHLORINE, 1),
              MekanismGases.HYDROGEN_CHLORIDE.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gasStack().from(MekanismGases.OXYGEN, 1),
              IngredientCreatorAccess.gasStack().from(MekanismGases.SULFUR_DIOXIDE, 2),
              MekanismGases.SULFUR_TRIOXIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gasStack().from(MekanismGases.SULFUR_TRIOXIDE, 1),
              IngredientCreatorAccess.gasStack().from(MekanismGases.WATER_VAPOR, 1),
              MekanismGases.SULFURIC_ACID.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }
}