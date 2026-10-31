package mekanism.common.recipe.impl;

import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.book.ReactingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class PressurizedReactionRecipeProvider extends BaseSubRecipeProvider {

    PressurizedReactionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "reaction/";
        addCoalGasificationRecipes(output, basePath + "coal_gasification/");
        addWoodGasificationRecipes(output, basePath + "wood_gasification/");
        addSubstrateRecipes(output, basePath + "substrate/");
    }

    private void addCoalGasificationRecipes(RecipeOutput output, String basePath) {
        //Blocks
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, 1, List.of(
                    Tags.BlockItems.STORAGE_BLOCKS_COAL.item(),
                    MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1_000),
              900,
              MekanismItems.SULFUR_DUST.asTemplate(9),
              chemicalTemplate(ChemicalIds.HYDROGEN, 1_000)
        ).category(ReactingBookCategory.GASIFICATION)
              .group(MekanismItems.SULFUR_DUST.key())
              .unlockedBy("has_coal", has(Tags.BlockItems.STORAGE_BLOCKS_COAL))
              .unlockedBy("has_charcoal", has(MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL))
              .save(output, basePath + "blocks_coals");
        //Coals
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, ItemTags.COALS),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 100),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 100)
        ).category(ReactingBookCategory.GASIFICATION)
              .group(MekanismItems.SULFUR_DUST.key())
              .unlockedBy("has_coal", has(ItemTags.COALS))
              .save(output, basePath + "coals");
        //Dusts
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, 1, List.of(
                    MekanismTags.Items.DUSTS_COAL,
                    MekanismTags.Items.DUSTS_CHARCOAL
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 100),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 100)
        ).category(ReactingBookCategory.GASIFICATION)
              .group(MekanismItems.SULFUR_DUST.key())
              .unlockedBy("has_coal", has(MekanismTags.Items.DUSTS_COAL))
              .unlockedBy("has_charcoal", has(MekanismTags.Items.DUSTS_CHARCOAL))
              .save(output, basePath + "dusts_coals");
    }

    private void addWoodGasificationRecipes(RecipeOutput output, String basePath) {
        //TODO: Figure out a way to specify only the woods that burn. Vanilla has a logs_that_burn tag but does not have one for the other types of wood
        // Maybe we could do a data component ingredient to match the presence of the fuel component?
        //Dusts, each worth a 32th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_WOOD, 8),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 25),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 25),
              37,
              chemicalTemplate(ChemicalIds.HYDROGEN, 25)
        ).category(ReactingBookCategory.GASIFICATION)
              .group("hydrogen")
              .unlockedBy("has_sawdust", has(MekanismTags.Items.DUSTS_WOOD))
              .save(output, basePath + "dusts_wood");
        //Logs, each worth one log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, ItemTags.LOGS_THAT_BURN, 4),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 400),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 400),
              600,
              MekanismItems.CHARCOAL_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 400)
        ).category(ReactingBookCategory.GASIFICATION)
              .group(MekanismItems.CHARCOAL_DUST.key())
              .unlockedBy("has_logs", has(ItemTags.LOGS_THAT_BURN))
              .save(output, basePath + "logs");
        //Planks, each worth a 5th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    ItemTags.PLANKS,
                    //Allow mosaic as it can be smelted, so it makes sense it can be used in wood gasification
                    BlockItemIds.BAMBOO_MOSAIC.item()
              ), 20),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 400),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 400),
              600,
              MekanismItems.CHARCOAL_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 400)
        ).category(ReactingBookCategory.GASIFICATION)
              .group(MekanismItems.CHARCOAL_DUST.key())
              .unlockedBy("has_planks", has(ItemTags.PLANKS))
              .unlockedBy(hasCriterion(BlockItemIds.BAMBOO_MOSAIC))
              .save(output, basePath + "planks");
        //Rods, each worth a 30th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RODS_WOODEN, 3),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
              15,
              chemicalTemplate(ChemicalIds.HYDROGEN, 10)
        ).category(ReactingBookCategory.GASIFICATION)
              .group("hydrogen")
              .unlockedBy("has_sticks", has(Tags.Items.RODS_WOODEN))
              .save(output, basePath + "rods_wooden");
        //Slabs, each worth a 10th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    ItemTags.WOODEN_SLABS,
                    //Allow mosaic as it can be smelted, so it makes sense it can be used in wood gasification
                    BlockItemIds.BAMBOO_MOSAIC_SLAB.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
              15,
              chemicalTemplate(ChemicalIds.HYDROGEN, 10)
        ).category(ReactingBookCategory.GASIFICATION)
              .group("hydrogen")
              .unlockedBy("has_wooden_slabs", has(ItemTags.WOODEN_SLABS))
              .unlockedBy(hasCriterion(BlockItemIds.BAMBOO_MOSAIC_SLAB))
              .save(output, basePath + "wooden_slabs");
        //Stairs, each worth a 6⅔th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.ingredient(
                    this.items,
                    ItemTags.WOODEN_STAIRS,
                    //Allow mosaic as it can be smelted, so it makes sense it can be used in wood gasification
                    BlockItemIds.BAMBOO_MOSAIC_STAIRS.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 15),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 15),
              22,
              chemicalTemplate(ChemicalIds.HYDROGEN, 15)
        ).category(ReactingBookCategory.GASIFICATION)
              .group("hydrogen")
              .unlockedBy("has_wooden_stairs", has(ItemTags.WOODEN_STAIRS))
              .unlockedBy(hasCriterion(BlockItemIds.BAMBOO_MOSAIC_STAIRS))
              .save(output, basePath + "wooden_stairs");
    }

    private void addSubstrateRecipes(RecipeOutput output, String basePath) {
        //Ethene + oxygen
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().fromHolder(MekanismItems.SUBSTRATE),
                    IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.ETHENE, 50),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
                    60,
                    MekanismItems.HDPE_PELLET.asTemplate()
              ).energyRequired(10)
              .unlockedBy(hasCriterion(MekanismItems.SUBSTRATE))
              .save(output, basePath + "ethene_oxygen");
        //Water + ethene
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().fromHolder(MekanismItems.SUBSTRATE),
                    IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 200),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ETHENE, 100),
                    400,
                    MekanismItems.SUBSTRATE.asTemplate(8),
                    chemicalTemplate(ChemicalIds.OXYGEN, 10)
              ).energyRequired(2)
              .group(MekanismItems.SUBSTRATE.key())
              //Only unlock once the player has made at least one substrate
              .unlockedBy(hasCriterion(MekanismItems.SUBSTRATE))
              .save(output, basePath + "water_ethene");
        //Water + hydrogen
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BIO, 2),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN, 100),
              100,
              MekanismItems.SUBSTRATE.asTemplate(),
              chemicalTemplate(ChemicalIds.ETHENE, 300)
        ).group(MekanismItems.SUBSTRATE.key())
              .unlockedBy("has_bio_fuel", has(MekanismTags.Items.FUELS_BIO))
              .save(output, basePath + "water_hydrogen");
    }
}