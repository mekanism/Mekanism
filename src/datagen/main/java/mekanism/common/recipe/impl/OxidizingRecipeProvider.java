package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
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
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "oxidizing/";
        //Brine
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SALT),
              chemicalTemplate(ChemicalIds.BRINE, 15)
        ).unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .save(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_LITHIUM),
              chemicalTemplate(ChemicalIds.LITHIUM, 100)
        ).unlockedBy("has_lithium", has(MekanismTags.Items.DUSTS_LITHIUM))
              .save(consumer, Mekanism.rl(basePath + "lithium"));
        //Sulfur dioxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              chemicalTemplate(ChemicalIds.SULFUR_DIOXIDE, 100)
        ).unlockedBy("has_sulfur_dust", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(consumer, Mekanism.rl(basePath + "sulfur_dioxide"));
    }
}