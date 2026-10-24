package mekanism.common.recipe.impl;

import java.util.Arrays;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider extends BaseSubRecipeProvider {

    InfusionConversionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        addInfusionConversionBioRecipes(consumer, "bio/");
        addInfusionConversionCarbonRecipes(consumer, "carbon/");
        addInfusionConversionDiamondRecipes(consumer, "diamond/");
        addInfusionConversionFungiRecipes(consumer, "fungi/");
        addInfusionConversionRedstoneRecipes(consumer, "redstone/");
        addInfusionConversionRefinedObsidianRecipes(consumer, "refined_obsidian/");
        addInfusionConversionGoldRecipes(consumer, "gold/");
        addInfusionConversionTinRecipes(consumer, "tin/");
    }

    private static void infusionConversion(RecipeOutput consumer, ItemStackIngredient input, ChemicalStackTemplate output, String basePath, String recipeName,
          RecipeCriterion... criteria) {
        ItemStackToChemicalRecipeBuilder.chemicalConversion(input, output)
              .group(output.typeHolder().key())
              .unlockedBy(Arrays.stream(criteria))
              .save(consumer, Mekanism.rl("chemical_conversion/" + basePath + recipeName));
        ItemStackToChemicalRecipeBuilder.oxidizing(input, output)
              .group(output.typeHolder().key())
              .unlockedBy(Arrays.stream(criteria))
              .save(consumer, Mekanism.rl("oxidizing/" + basePath + recipeName));
    }

    private void addInfusionConversionBioRecipes(RecipeOutput consumer, String basePath) {
        //Bio fuel
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BIO),
              chemicalTemplate(ChemicalIds.BIO, 5),
              basePath, "from_bio_fuel",
              new RecipeCriterion("has_bio_fuel", has(MekanismTags.Items.FUELS_BIO))
        );
        //Bio fuel block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BLOCK_BIO),
              chemicalTemplate(ChemicalIds.BIO, 5 * 9),
              basePath, "from_bio_fuel_block",
              new RecipeCriterion("has_bio_fuel", has(MekanismTags.Items.FUELS_BLOCK_BIO))
        );
    }

    private void addInfusionConversionCarbonRecipes(RecipeOutput consumer, String basePath) {
        //Charcoal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL),
              chemicalTemplate(ChemicalIds.CARBON, 180),
              basePath, "from_charcoal_block",
              new RecipeCriterion("has_charcoal", has(MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL))
        );
        //Charcoal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    MekanismTags.Items.DUSTS_CHARCOAL,
                    ItemIds.CHARCOAL
              )), chemicalTemplate(ChemicalIds.CARBON, 20),
              basePath, "from_charcoal",
              new RecipeCriterion("has_charcoal", has(MekanismTags.Items.DUSTS_CHARCOAL))
        );

        //Coal Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.STORAGE_BLOCKS_COAL),
              chemicalTemplate(ChemicalIds.CARBON, 90),
              basePath, "from_coal_block",
              new RecipeCriterion("has_coal", has(Tags.BlockItems.STORAGE_BLOCKS_COAL))
        );
        //Coal
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    MekanismTags.Items.DUSTS_COAL,
                    ItemIds.COAL
              )), chemicalTemplate(ChemicalIds.CARBON, 10),
              basePath, "from_coal",
              new RecipeCriterion("has_coal", has(MekanismTags.Items.DUSTS_COAL))
        );

        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_CARBON),
              chemicalTemplate(ChemicalIds.CARBON, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_CARBON))
        );
    }

    private void addInfusionConversionDiamondRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_DIAMOND),
              chemicalTemplate(ChemicalIds.DIAMOND, 10),
              basePath, "from_dust",
              new RecipeCriterion("has_dust", has(MekanismTags.Items.DUSTS_DIAMOND))
        );
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_DIAMOND),
              chemicalTemplate(ChemicalIds.DIAMOND, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_DIAMOND))
        );
    }

    private void addInfusionConversionFungiRecipes(RecipeOutput consumer, String basePath) {
        //Mushrooms
        infusionConversion(consumer, IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    Tags.Items.MUSHROOMS,
                    //TODO: If these get added to the mushroom tag then we can remove them from here
                    BlockItemIds.CRIMSON_FUNGUS.item(),
                    BlockItemIds.WARPED_FUNGUS.item()
              )), chemicalTemplate(ChemicalIds.FUNGI, 10),
              basePath, "from_mushrooms",
              new RecipeCriterion("has_mushrooms", has(Tags.Items.MUSHROOMS)),
              new RecipeCriterion("has_crimson_fungus", has(BlockItemIds.CRIMSON_FUNGUS)),
              new RecipeCriterion("has_warped_fungus", has(BlockItemIds.WARPED_FUNGUS))
        );
    }

    private void addInfusionConversionRedstoneRecipes(RecipeOutput consumer, String basePath) {
        //Block
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.STORAGE_BLOCKS_REDSTONE),
              chemicalTemplate(ChemicalIds.REDSTONE, 90),
              basePath, "from_block",
              new RecipeCriterion("has_redstone", has(Tags.BlockItems.STORAGE_BLOCKS_REDSTONE))
        );
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_REDSTONE),
              chemicalTemplate(ChemicalIds.REDSTONE, 10),
              basePath, "from_dust",
              new RecipeCriterion("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
        );
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_REDSTONE),
              chemicalTemplate(ChemicalIds.REDSTONE, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_REDSTONE))
        );
    }

    private void addInfusionConversionRefinedObsidianRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              chemicalTemplate(ChemicalIds.REFINED_OBSIDIAN, 10),
              basePath, "from_dust",
              new RecipeCriterion("has_dust", has(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN))
        );
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_OBSIDIAN),
              chemicalTemplate(ChemicalIds.REFINED_OBSIDIAN, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_OBSIDIAN))
        );
    }

    private void addInfusionConversionGoldRecipes(RecipeOutput consumer, String basePath) {
        TagKey<Item> goldDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD);
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, goldDust),
              chemicalTemplate(ChemicalIds.GOLD, 10),
              basePath, "from_dust",
              new RecipeCriterion("has_dust", has(goldDust))
        );
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_GOLD),
              chemicalTemplate(ChemicalIds.GOLD, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_GOLD))
        );
    }

    private void addInfusionConversionTinRecipes(RecipeOutput consumer, String basePath) {
        TagKey<Item> tinDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN);
        //Dust
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, tinDust),
              chemicalTemplate(ChemicalIds.TIN, 10),
              basePath, "from_dust",
              new RecipeCriterion("has_dust", has(tinDust))
        );
        //Enriched
        infusionConversion(consumer, IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.ENRICHED_TIN),
              chemicalTemplate(ChemicalIds.TIN, 80),
              basePath, "from_enriched",
              new RecipeCriterion("has_enriched", has(MekanismTags.Items.ENRICHED_TIN))
        );
    }
}