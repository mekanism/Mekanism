package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;

class ChemicalInfuserRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "chemical_infusing/";
        //Hydrogen Chloride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.HYDROGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.CHLORINE, 1),
              MekanismChemicals.HYDROGEN_CHLORIDE.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.OXYGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.SULFUR_DIOXIDE, 2),
              MekanismChemicals.SULFUR_TRIOXIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.SULFUR_TRIOXIDE, 1),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.WATER_VAPOR, 1),
              MekanismChemicals.SULFURIC_ACID.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }
}