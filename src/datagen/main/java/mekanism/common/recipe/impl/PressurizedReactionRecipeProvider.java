package mekanism.common.recipe.impl;

import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "reaction/";
        addCoalGasificationRecipes(consumer, basePath + "coal_gasification/");
        addWoodGasificationRecipes(consumer, basePath + "wood_gasification/");
        addSubstrateRecipes(consumer, basePath + "substrate/");
    }

    private void addCoalGasificationRecipes(RecipeOutput consumer, String basePath) {
        //Blocks
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, 1, List.of(
                    Tags.Items.STORAGE_BLOCKS_COAL,
                    MekanismTags.BlockItems.STORAGE_BLOCKS_CHARCOAL.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1_000),
              900,
              MekanismItems.SULFUR_DUST.asTemplate(9),
              chemicalTemplate(ChemicalIds.HYDROGEN, 1_000)
        ).save(consumer, Mekanism.rl(basePath + "blocks_coals"));
        //Coals
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, ItemTags.COALS),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 100),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 100)
        ).save(consumer, Mekanism.rl(basePath + "coals"));
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
        ).save(consumer, Mekanism.rl(basePath + "dusts_coals"));

    }

    private void addWoodGasificationRecipes(RecipeOutput consumer, String basePath) {
        //TODO: Figure out a way to specify only the woods that burn. Vanilla has a logs_that_burn tag
        // but doe snot have one for the other types of wood
        //Dusts, each worth a 32th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_WOOD, 8),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 25),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 25),
              37,
              chemicalTemplate(ChemicalIds.HYDROGEN, 25)
        ).save(consumer, Mekanism.rl(basePath + "dusts_wood"));
        //Logs, each worth one log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, ItemTags.LOGS, 4),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 400),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 400),
              600,
              MekanismItems.CHARCOAL_DUST.asTemplate(),
              chemicalTemplate(ChemicalIds.HYDROGEN, 400)
        ).save(consumer, Mekanism.rl(basePath + "logs"));
        //Planks, each worth a 5th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
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
        ).save(consumer, Mekanism.rl(basePath + "planks"));
        //Rods, each worth a 30th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RODS_WOODEN, 3),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
              15,
              chemicalTemplate(ChemicalIds.HYDROGEN, 10)
        ).save(consumer, Mekanism.rl(basePath + "rods_wooden"));
        //Slabs, each worth a 10th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                    this.items,
                    ItemTags.WOODEN_SLABS,
                    //Allow mosaic as it can be smelted, so it makes sense it can be used in wood gasification
                    BlockItemIds.BAMBOO_MOSAIC_SLAB.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
              15,
              chemicalTemplate(ChemicalIds.HYDROGEN, 10)
        ).save(consumer, Mekanism.rl(basePath + "wooden_slabs"));
        //Stairs, each worth a 6⅔th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(BaseRecipeProvider.createIngredient(
                    this.items,
                    ItemTags.WOODEN_STAIRS,
                    //Allow mosaic as it can be smelted, so it makes sense it can be used in wood gasification
                    BlockItemIds.BAMBOO_MOSAIC_STAIRS.item()
              )),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 15),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 15),
              22,
              chemicalTemplate(ChemicalIds.HYDROGEN, 15)
        ).save(consumer, Mekanism.rl(basePath + "wooden_stairs"));
    }

    private void addSubstrateRecipes(RecipeOutput consumer, String basePath) {
        //Ethene + oxygen
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().fromHolder(MekanismItems.SUBSTRATE),
                    IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.ETHENE, 50),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 10),
                    60,
                    MekanismItems.HDPE_PELLET.asTemplate()
              ).energyRequired(10)
              .save(consumer, Mekanism.rl(basePath + "ethene_oxygen"));
        //Water + ethene
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().fromHolder(MekanismItems.SUBSTRATE),
                    IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 200),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ETHENE, 100),
                    400,
                    MekanismItems.SUBSTRATE.asTemplate(8),
                    chemicalTemplate(ChemicalIds.OXYGEN, 10)
              ).energyRequired(2)
              .save(consumer, Mekanism.rl(basePath + "water_ethene"));
        //Water + hydrogen
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.FUELS_BIO, 2),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 10),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN, 100),
              100,
              MekanismItems.SUBSTRATE.asTemplate(),
              chemicalTemplate(ChemicalIds.ETHENE, 300)
        ).save(consumer, Mekanism.rl(basePath + "water_hydrogen"));
    }
}