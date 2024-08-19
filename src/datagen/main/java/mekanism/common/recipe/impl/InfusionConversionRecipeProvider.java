package mekanism.common.recipe.impl;

import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        addInfusionConversionBioRecipes(consumer, "bio/");
        addInfusionConversionCarbonRecipes(consumer, "carbon/");
        addInfusionConversionDiamondRecipes(consumer, "diamond/");
        addInfusionConversionFungiRecipes(consumer, "fungi/");
        addInfusionConversionRedstoneRecipes(consumer, "redstone/");
        addInfusionConversionRefinedObsidianRecipes(consumer, "refined_obsidian/");
        addInfusionConversionGoldRecipes(consumer, "gold/");
        addInfusionConversionTinRecipes(consumer, "tin/");
    }

    private static void infusionConversion(RecipeOutput consumer, ItemStackIngredient input, ChemicalStack output, String basePath, String recipeName) {
        ItemStackToChemicalRecipeBuilder.chemicalConversion(input, output).build(consumer, Mekanism.rl("chemical_conversion/" + basePath + recipeName));
        ItemStackToChemicalRecipeBuilder.oxidizing(input, output).build(consumer, Mekanism.rl("oxidizing/" + basePath + recipeName));
    }

    private void addInfusionConversionBioRecipes(RecipeOutput consumer, String basePath) {
        //Bio fuel
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.FUELS_BIO), MekanismChemicals.BIO.getStack(5), basePath, "from_bio_fuel");
        //Bio fuel block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.FUELS_BLOCK_BIO), MekanismChemicals.BIO.getStack(5 * 9), basePath, "from_bio_fuel_block");
    }

    private void addInfusionConversionCarbonRecipes(RecipeOutput consumer, String basePath) {
        //Charcoal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL), MekanismChemicals.CARBON.getStack(180), basePath, "from_charcoal_block");
        //Charcoal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              MekanismTags.Items.DUSTS_CHARCOAL,
              Items.CHARCOAL
        )), MekanismChemicals.CARBON.getStack(20), basePath, "from_charcoal");

        //Coal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_COAL), MekanismChemicals.CARBON.getStack(90), basePath, "from_coal_block");
        //Coal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              MekanismTags.Items.DUSTS_COAL,
              Items.COAL
        )), MekanismChemicals.CARBON.getStack(10), basePath, "from_coal");

        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_CARBON), MekanismChemicals.CARBON.getStack(80), basePath, "from_enriched");
    }

    private void addInfusionConversionDiamondRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_DIAMOND), MekanismChemicals.DIAMOND.getStack(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_DIAMOND), MekanismChemicals.DIAMOND.getStack(80), basePath, "from_enriched");
    }

    private void addInfusionConversionFungiRecipes(RecipeOutput consumer, String basePath) {
        //Mushrooms
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              Tags.Items.MUSHROOMS,
              //TODO: If these get added to the mushroom tag then we can remove them from here
              Blocks.WARPED_FUNGUS,
              Blocks.CRIMSON_FUNGUS
        )), MekanismChemicals.FUNGI.getStack(10), basePath, "from_mushrooms");
    }

    private void addInfusionConversionRedstoneRecipes(RecipeOutput consumer, String basePath) {
        //Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_REDSTONE), MekanismChemicals.REDSTONE.getStack(90), basePath, "from_block");
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(Tags.Items.DUSTS_REDSTONE), MekanismChemicals.REDSTONE.getStack(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_REDSTONE), MekanismChemicals.REDSTONE.getStack(80), basePath, "from_enriched");
    }

    private void addInfusionConversionRefinedObsidianRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN), MekanismChemicals.REFINED_OBSIDIAN.getStack(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_OBSIDIAN), MekanismChemicals.REFINED_OBSIDIAN.getStack(80), basePath, "from_enriched");
    }

    private void addInfusionConversionGoldRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD)), MekanismChemicals.GOLD.getStack(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_GOLD), MekanismChemicals.GOLD.getStack(80), basePath, "from_enriched");
    }

    private void addInfusionConversionTinRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN)), MekanismChemicals.TIN.getStack(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_TIN), MekanismChemicals.TIN.getStack(80), basePath, "from_enriched");
    }
}