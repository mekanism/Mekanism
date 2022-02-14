package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "infusion_conversion/";
        addInfusionConversionBioRecipes(consumer, basePath + "bio/");
        addInfusionConversionCarbonRecipes(consumer, basePath + "carbon/");
        addInfusionConversionDiamondRecipes(consumer, basePath + "diamond/");
        addInfusionConversionFungiRecipes(consumer, basePath + "fungi/");
        addInfusionConversionRedstoneRecipes(consumer, basePath + "redstone/");
        addInfusionConversionRefinedObsidianRecipes(consumer, basePath + "refined_obsidian/");
        addInfusionConversionGoldRecipes(consumer, basePath + "gold/");
        addInfusionConversionTinRecipes(consumer, basePath + "tin/");
    }

    private void addInfusionConversionBioRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Bio fuel
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.FUELS_BIO),
              MekanismInfuseTypes.BIO.getStack(5)
        ).build(consumer, Mekanism.rl(basePath + "from_bio_fuel"));
    }

    private void addInfusionConversionCarbonRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Charcoal Block
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL),
              MekanismInfuseTypes.CARBON.getStack(180)
        ).build(consumer, Mekanism.rl(basePath + "from_charcoal_block"));
        //Charcoal
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                    MekanismTags.Items.DUSTS_CHARCOAL,
                    Items.CHARCOAL
              )),
              MekanismInfuseTypes.CARBON.getStack(20)
        ).build(consumer, Mekanism.rl(basePath + "from_charcoal"));

        //Coal Block
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_COAL),
              MekanismInfuseTypes.CARBON.getStack(90)
        ).build(consumer, Mekanism.rl(basePath + "from_coal_block"));
        //Coal
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                    MekanismTags.Items.DUSTS_COAL,
                    Items.COAL
              )),
              MekanismInfuseTypes.CARBON.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_coal"));

        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_CARBON),
              MekanismInfuseTypes.CARBON.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionDiamondRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionFungiRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Mushrooms
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                    Tags.Items.MUSHROOMS,
                    //TODO: If these get added to the mushroom tag then we can remove them from here
                    Blocks.WARPED_FUNGUS,
                    Blocks.CRIMSON_FUNGUS
              )),
              MekanismInfuseTypes.FUNGI.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_mushrooms"));
    }

    private void addInfusionConversionRedstoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Block
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getStack(90)
        ).build(consumer, Mekanism.rl(basePath + "from_block"));
        //Dust
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(Tags.Items.DUSTS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionRefinedObsidianRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionGoldRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD)),
              MekanismInfuseTypes.GOLD.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_GOLD),
              MekanismInfuseTypes.GOLD.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionTinRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN)),
              MekanismInfuseTypes.TIN.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToChemicalRecipeBuilder.infusionConversion(
              IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_TIN),
              MekanismInfuseTypes.TIN.getStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }
}