package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class ChemicalInfuserRecipeProvider extends BaseSubRecipeProvider {

    ChemicalInfuserRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "chemical_infusing/";
        //Hydrogen Chloride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.CHLORINE, 1),
              chemicalTemplate(ChemicalIds.HYDROGEN_CHLORIDE, 1)
        ).unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .save(output, basePath + "hydrogen_chloride");
        //Sulfur Trioxide
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFUR_DIOXIDE, 2),
              chemicalTemplate(ChemicalIds.SULFUR_TRIOXIDE, 2)
        ).unlockedBy("has_sulfur_dust", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(output, basePath + "sulfur_trioxide");
        //Sulfuric Acid
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFUR_TRIOXIDE, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.WATER_VAPOR, 1),
              chemicalTemplate(ChemicalIds.SULFURIC_ACID, 1)
        ).unlockedBy("has_sulfur_dust", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(output, basePath + "sulfuric_acid");
    }
}