package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class ChemicalInfuserRecipeProvider extends BaseSubRecipeProvider {

    ChemicalInfuserRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "chemical_infusing/";
        //Hydrogen Chloride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.CHLORINE, 1),
              MekanismChemicals.HYDROGEN_CHLORIDE.asTemplate(1)
        ).save(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFUR_DIOXIDE, 2),
              MekanismChemicals.SULFUR_TRIOXIDE.asTemplate(2)
        ).save(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFUR_TRIOXIDE, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.WATER_VAPOR, 1),
              MekanismChemicals.SULFURIC_ACID.asTemplate(1)
        ).save(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }
}