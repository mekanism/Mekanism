package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
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
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.CHLORINE, 1),
              chemicalTemplate(ChemicalIds.HYDROGEN_CHLORIDE, 1)
        ).save(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFUR_DIOXIDE, 2),
              chemicalTemplate(ChemicalIds.SULFUR_TRIOXIDE, 2)
        ).save(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFUR_TRIOXIDE, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.WATER_VAPOR, 1),
              chemicalTemplate(ChemicalIds.SULFURIC_ACID, 1)
        ).save(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }
}