package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import net.minecraft.data.IFinishedRecipe;

class ChemicalInfuserRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
        //Hydrogen Chloride
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.HYDROGEN, 1),
              GasStackIngredient.from(MekanismGases.CHLORINE, 1),
              MekanismGases.HYDROGEN_CHLORIDE.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.OXYGEN, 1),
              GasStackIngredient.from(MekanismGases.SULFUR_DIOXIDE, 2),
              MekanismGases.SULFUR_TRIOXIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.SULFUR_TRIOXIDE, 1),
              GasStackIngredient.from(MekanismGases.WATER_VAPOR, 1),
              MekanismGases.SULFURIC_ACID.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }
}