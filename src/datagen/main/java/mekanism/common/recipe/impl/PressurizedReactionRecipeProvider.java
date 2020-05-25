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
        addWoodGasificationRecipes(consumer, basePath + "wood_gasification/");
        addSubstrateRecipes(consumer, basePath + "substrate/");
    }

    private void addWoodGasificationRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Blocks coal
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 1_000),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1_000),
              900,
              MekanismItems.SULFUR_DUST.getItemStack(9),
              MekanismGases.HYDROGEN.getGasStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "blocks_coals"));
        //Coals
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.COALS),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "coals"));
        //Dusts coal
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "dusts_coal"));
        //Dusts wood
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_WOOD),
              FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismGases.OXYGEN, 20),
              30,
              MekanismGases.HYDROGEN.getGasStack(20)
        ).build(consumer, Mekanism.rl(basePath + "dusts_wood"));
        //Logs
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.LOGS),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              150,
              MekanismGases.HYDROGEN.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "logs"));
        //Planks
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.PLANKS),
              FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismGases.OXYGEN, 20),
              30,
              MekanismGases.HYDROGEN.getGasStack(20)
        ).build(consumer, Mekanism.rl(basePath + "planks"));
        //Rods wooden
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(Tags.Items.RODS_WOODEN),
              FluidStackIngredient.from(FluidTags.WATER, 4),
              GasStackIngredient.from(MekanismGases.OXYGEN, 4),
              6,
              MekanismGases.HYDROGEN.getGasStack(4)
        ).build(consumer, Mekanism.rl(basePath + "rods_wooden"));
        //Slabs wooden
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.WOODEN_SLABS),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              15,
              MekanismGases.HYDROGEN.getGasStack(10)
        ).build(consumer, Mekanism.rl(basePath + "slabs_wooden"));
    }

    private void addSubstrateRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ethene + oxygen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(MekanismTags.Fluids.ETHENE, 50),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              60,
              MekanismItems.HDPE_PELLET.getItemStack(),
              MekanismGases.OXYGEN.getGasStack(5)
        ).energyRequired(FloatingLong.createConst(1_000))
              .build(consumer, Mekanism.rl(basePath + "ethene_oxygen"));
        //Water + ethene
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(FluidTags.WATER, 200),
              GasStackIngredient.from(MekanismGases.ETHENE, 100),
              400,
              MekanismItems.SUBSTRATE.getItemStack(8),
              MekanismGases.OXYGEN.getGasStack(10)
        ).energyRequired(FloatingLong.createConst(200))
              .build(consumer, Mekanism.rl(basePath + "water_ethene"));
        //Water + hydrogen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.FUELS_BIO, 2),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.HYDROGEN, 100),
              100,
              MekanismItems.SUBSTRATE.getItemStack(),
              MekanismGases.ETHENE.getGasStack(100)
        ).build(consumer, Mekanism.rl(basePath + "water_hydrogen"));
    }
}