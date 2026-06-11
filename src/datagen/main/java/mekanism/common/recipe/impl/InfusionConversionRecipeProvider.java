package mekanism.common.recipe.impl;

import mekanism.api.chemical.ChemicalStackTemplate;
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
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider implements ISubRecipeProvider {

    private final HolderGetter<Item> items;

    public InfusionConversionRecipeProvider(HolderGetter<Item> items) {
        this.items = items;
    }

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

    private static void infusionConversion(RecipeOutput consumer, ItemStackIngredient input, ChemicalStackTemplate output, String basePath, String recipeName) {
        ItemStackToChemicalRecipeBuilder.chemicalConversion(input, output).save(consumer, Mekanism.rl("chemical_conversion/" + basePath + recipeName));
        ItemStackToChemicalRecipeBuilder.oxidizing(input, output).save(consumer, Mekanism.rl("oxidizing/" + basePath + recipeName));
    }

    private void addInfusionConversionBioRecipes(RecipeOutput consumer, String basePath) {
        //Bio fuel
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BIO), MekanismChemicals.BIO.asTemplate(5), basePath, "from_bio_fuel");
        //Bio fuel block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BLOCK_BIO), MekanismChemicals.BIO.asTemplate(5 * 9), basePath, "from_bio_fuel_block");
    }

    private void addInfusionConversionCarbonRecipes(RecipeOutput consumer, String basePath) {
        //Charcoal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL), MekanismChemicals.CARBON.asTemplate(180), basePath, "from_charcoal_block");
        //Charcoal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              this.items,
              MekanismTags.Items.DUSTS_CHARCOAL,
              Items.CHARCOAL
        )), MekanismChemicals.CARBON.asTemplate(20), basePath, "from_charcoal");

        //Coal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.Items.STORAGE_BLOCKS_COAL), MekanismChemicals.CARBON.asTemplate(90), basePath, "from_coal_block");
        //Coal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              this.items,
              MekanismTags.Items.DUSTS_COAL,
              Items.COAL
        )), MekanismChemicals.CARBON.asTemplate(10), basePath, "from_coal");

        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_CARBON), MekanismChemicals.CARBON.asTemplate(80), basePath, "from_enriched");
    }

    private void addInfusionConversionDiamondRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_DIAMOND), MekanismChemicals.DIAMOND.asTemplate(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_DIAMOND), MekanismChemicals.DIAMOND.asTemplate(80), basePath, "from_enriched");
    }

    private void addInfusionConversionFungiRecipes(RecipeOutput consumer, String basePath) {
        //Mushrooms
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
              this.items,
              Tags.Items.MUSHROOMS,
              //TODO: If these get added to the mushroom tag then we can remove them from here
              Items.WARPED_FUNGUS,
              Items.CRIMSON_FUNGUS
        )), MekanismChemicals.FUNGI.asTemplate(10), basePath, "from_mushrooms");
    }

    private void addInfusionConversionRedstoneRecipes(RecipeOutput consumer, String basePath) {
        //Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.Items.STORAGE_BLOCKS_REDSTONE), MekanismChemicals.REDSTONE.asTemplate(90), basePath, "from_block");
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_REDSTONE), MekanismChemicals.REDSTONE.asTemplate(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_REDSTONE), MekanismChemicals.REDSTONE.asTemplate(80), basePath, "from_enriched");
    }

    private void addInfusionConversionRefinedObsidianRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN), MekanismChemicals.REFINED_OBSIDIAN.asTemplate(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_OBSIDIAN), MekanismChemicals.REFINED_OBSIDIAN.asTemplate(80), basePath, "from_enriched");
    }

    private void addInfusionConversionGoldRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD)), MekanismChemicals.GOLD.asTemplate(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_GOLD), MekanismChemicals.GOLD.asTemplate(80), basePath, "from_enriched");
    }

    private void addInfusionConversionTinRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN)), MekanismChemicals.TIN.asTemplate(10), basePath, "from_dust");
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_TIN), MekanismChemicals.TIN.asTemplate(80), basePath, "from_enriched");
    }
}