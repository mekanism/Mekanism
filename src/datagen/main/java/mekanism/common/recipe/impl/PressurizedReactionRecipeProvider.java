package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

class PressurizedReactionRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "reaction/";
        addCoalGasificationRecipes(consumer, basePath + "coal_gasification/");
        addWoodGasificationRecipes(consumer, basePath + "wood_gasification/");
        addSubstrateRecipes(consumer, basePath + "substrate/");
    }

    private void addCoalGasificationRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Blocks
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 1_000),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1_000),
              900,
              MekanismItems.SULFUR_DUST.getItemStack(9),
              MekanismGases.HYDROGEN.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "blocks_coals"));
        //Coals
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.COALS),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "coals"));
        //Dusts
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "dusts_coals"));

    }

    private void addWoodGasificationRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //TODO: Figure out a way to specify only the woods that burn. Vanilla has a logs_that_burn tag
        // but doe snot have one for the other types of wood
        //Dusts, each worth a 32th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_WOOD, 8),
              FluidStackIngredient.from(FluidTags.WATER, 25),
              GasStackIngredient.from(MekanismGases.OXYGEN, 25),
              37,
              MekanismGases.HYDROGEN.getStack(25)
        ).build(consumer, Mekanism.rl(basePath + "dusts_wood"));
        //Logs, each worth one log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.LOGS, 4),
              FluidStackIngredient.from(FluidTags.WATER, 400),
              GasStackIngredient.from(MekanismGases.OXYGEN, 400),
              600,
              MekanismItems.CHARCOAL_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getStack(400)
        ).build(consumer, Mekanism.rl(basePath + "logs"));
        //Planks, each worth a 5th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.PLANKS, 20),
              FluidStackIngredient.from(FluidTags.WATER, 400),
              GasStackIngredient.from(MekanismGases.OXYGEN, 400),
              600,
              MekanismItems.CHARCOAL_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getStack(400)
        ).build(consumer, Mekanism.rl(basePath + "planks"));
        //Rods, each worth a 30th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(Tags.Items.RODS_WOODEN, 3),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              15,
              MekanismGases.HYDROGEN.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "rods_wooden"));
        //Slabs, each worth a 10th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.WOODEN_SLABS),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              15,
              MekanismGases.HYDROGEN.getStack(10)
        ).build(consumer, Mekanism.rl(basePath + "wooden_slabs"));
        //Stairs, each worth a 6⅔th of a log.
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.WOODEN_STAIRS),
              FluidStackIngredient.from(FluidTags.WATER, 15),
              GasStackIngredient.from(MekanismGases.OXYGEN, 15),
              22,
              MekanismGases.HYDROGEN.getStack(15)
        ).build(consumer, Mekanism.rl(basePath + "wooden_stairs"));
    }

    private void addSubstrateRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ethene + oxygen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(MekanismTags.Fluids.ETHENE, 50),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              60,
              MekanismItems.HDPE_PELLET.getItemStack(),
              MekanismGases.OXYGEN.getStack(5)
        ).energyRequired(FloatingLong.createConst(1_000))
              .build(consumer, Mekanism.rl(basePath + "ethene_oxygen"));
        //Water + ethene
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(FluidTags.WATER, 200),
              GasStackIngredient.from(MekanismGases.ETHENE, 100),
              400,
              MekanismItems.SUBSTRATE.getItemStack(8),
              MekanismGases.OXYGEN.getStack(10)
        ).energyRequired(FloatingLong.createConst(200))
              .build(consumer, Mekanism.rl(basePath + "water_ethene"));
        //Water + hydrogen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.FUELS_BIO, 2),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.HYDROGEN, 100),
              100,
              MekanismItems.SUBSTRATE.getItemStack(),
              MekanismGases.ETHENE.getStack(100)
        ).build(consumer, Mekanism.rl(basePath + "water_hydrogen"));
    }
}