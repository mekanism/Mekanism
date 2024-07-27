package mekanism.common.recipe.impl;

import mekanism.api.chemical.infuse.*;
import mekanism.api.datagen.recipe.builder.*;
import mekanism.api.recipes.ingredients.*;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

//TODO: Try to cleanup some of the duplicate code in this class?
class InfusionConversionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer) {

        //Bio recipe section
        //Bio fuel
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.FUELS_BIO),
                MekanismInfuseTypes.BIO, 5, "from_bio_fuel", true);
        //Bio fuel block
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.FUELS_BLOCK_BIO),
                MekanismInfuseTypes.BIO, 5 * 9, "from_bio_fuel_block", true);

        //Carbon recipe section
        //Charcoal Block
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL),
                MekanismInfuseTypes.CARBON, 180, "from_charcoal_block", false);
        //Charcoal
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                        MekanismTags.Items.DUSTS_CHARCOAL,
                        Items.CHARCOAL
                )),
                MekanismInfuseTypes.CARBON, 20, "from_charcoal", false);
        //Coal Block
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_COAL),
                MekanismInfuseTypes.CARBON, 90, "from_coal_block", false);
        //Coal
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                        MekanismTags.Items.DUSTS_COAL,
                        Items.COAL
                )),
                MekanismInfuseTypes.CARBON, 10, "from_coal", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_CARBON),
                MekanismInfuseTypes.CARBON, 80, "from_enriched", true);

        //Diamond recipe section
        //Dust
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_DIAMOND),
                MekanismInfuseTypes.DIAMOND,10 , "from_dust", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_DIAMOND),
                MekanismInfuseTypes.DIAMOND,80 , "from_enriched", true);

        //Fungi  recipe section
        //Mushrooms
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                        Tags.Items.MUSHROOMS,
                        //TODO: If these get added to the mushroom tag then we can remove them from here
                        Blocks.WARPED_FUNGUS,
                        Blocks.CRIMSON_FUNGUS
                )),MekanismInfuseTypes.FUNGI, 10, "from_mushrooms", true);

        //Gold recipe section
        //Dust
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD)),
                MekanismInfuseTypes.GOLD, 10, "from_dust", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_GOLD),
                MekanismInfuseTypes.GOLD,80 , "from_enriched", true);

        //Redstone recipe section
        //Block
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_REDSTONE),
                MekanismInfuseTypes.REDSTONE,90 , "from_block", false);
        //Dust
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(Tags.Items.DUSTS_REDSTONE),
                MekanismInfuseTypes.REDSTONE, 10, "from_dust", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_REDSTONE),
                MekanismInfuseTypes.REDSTONE, 80, "from_enriched", true);

        //Refined obsidian recipe section
        //Dust
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
                MekanismInfuseTypes.REFINED_OBSIDIAN, 10, "from_dust", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_OBSIDIAN),
                MekanismInfuseTypes.REFINED_OBSIDIAN, 80, "from_enriched", true);

        //Tin recipe section
        //Dust
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN)),
                MekanismInfuseTypes.TIN,10 , "from_dust", false);
        //Enriched
        addInfusionConversionRecipe(consumer,
                IngredientCreatorAccess.item().from(MekanismTags.Items.ENRICHED_TIN),
                MekanismInfuseTypes.TIN, 80, "from_enriched", true);
    }

    private void addInfusionConversionRecipe(RecipeOutput consumer, ItemStackIngredient input, DeferredChemical.DeferredInfuseType<InfuseType> output, long outputAmount, String recipeName, boolean addOxidizerRecipe){
        ItemStackToChemicalRecipeBuilder.infusionConversion(input,
                output.getStack(outputAmount)
        ).build(consumer, Mekanism.rl("infusion_conversion/" + output.getName() + "/" + recipeName));

        if (addOxidizerRecipe){
            ChemicalOxidizerRecipeBuilder.oxidizing(input,
                    output.getStack(outputAmount)
            ).build(consumer, Mekanism.rl("oxidizing/" + output.getName() + "/" + recipeName));
        }
    }
}