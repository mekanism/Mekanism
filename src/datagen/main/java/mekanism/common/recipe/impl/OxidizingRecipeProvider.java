package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class OxidizingRecipeProvider extends BaseSubRecipeProvider {

    OxidizingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "oxidizing/";
        //Brine
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SALT),
              chemicalTemplate(ChemicalIds.BRINE, 15)
        ).unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .save(output, basePath + "brine");
        //Lithium
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_LITHIUM),
              chemicalTemplate(ChemicalIds.LITHIUM, 100)
        ).unlockedBy("has_lithium", has(MekanismTags.Items.DUSTS_LITHIUM))
              .save(output, basePath + "lithium");
        //Sulfur dioxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              chemicalTemplate(ChemicalIds.SULFUR_DIOXIDE, 100)
        ).unlockedBy("has_sulfur_dust", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(output, basePath + "sulfur_dioxide");
    }
}