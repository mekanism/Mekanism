package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToInfuseTypeRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "infusion_conversion/";
        addInfusionConversionBioRecipes(consumer, basePath + "bio/");
        addInfusionConversionCarbonRecipes(consumer, basePath + "carbon/");
        addInfusionConversionDiamondRecipes(consumer, basePath + "diamond/");
        addInfusionConversionFungiRecipes(consumer, basePath + "fungi/");
        addInfusionConversionRedstoneRecipes(consumer, basePath + "redstone/");
        addInfusionConversionRefinedObsidianRecipes(consumer, basePath + "refined_obsidian/");
        addInfusionConversionTinRecipes(consumer, basePath + "tin/");
    }

    private void addInfusionConversionBioRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Bio fuel
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.FUELS_BIO),
              MekanismInfuseTypes.BIO.getInfusionStack(5)
        ).build(consumer, Mekanism.rl(basePath + "from_bio_fuel"));
    }

    private void addInfusionConversionCarbonRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Charcoal Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL),
              MekanismInfuseTypes.CARBON.getInfusionStack(180)
        ).build(consumer, Mekanism.rl(basePath + "from_charcoal_block"));
        //Charcoal
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.CHARCOAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL)
              ),
              MekanismInfuseTypes.CARBON.getInfusionStack(20)
        ).build(consumer, Mekanism.rl(basePath + "from_charcoal"));

        //Coal Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL),
              MekanismInfuseTypes.CARBON.getInfusionStack(90)
        ).build(consumer, Mekanism.rl(basePath + "from_coal_block"));
        //Coal
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.COAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL)
              ),
              MekanismInfuseTypes.CARBON.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_coal"));

        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_CARBON),
              MekanismInfuseTypes.CARBON.getInfusionStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionDiamondRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getInfusionStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionFungiRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Mushrooms
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.MUSHROOMS),
              MekanismInfuseTypes.FUNGI.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_mushrooms"));
    }

    private void addInfusionConversionRedstoneRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(90)
        ).build(consumer, Mekanism.rl(basePath + "from_block"));
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionRefinedObsidianRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getInfusionStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionTinRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN)),
              MekanismInfuseTypes.TIN.getInfusionStack(10)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_TIN),
              MekanismInfuseTypes.TIN.getInfusionStack(80)
        ).build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }
}