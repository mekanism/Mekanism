package mekanism.common.recipe.impl;

import java.util.Map;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.neoforge.common.Tags;

class ChemicalInjectorRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "injecting/";
        //Brick -> clay ball
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Tags.Items.BRICKS_NORMAL),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY_BALL),
              true
        ).build(consumer, Mekanism.rl(basePath + "brick_to_clay_ball"));
        //Dirt -> mud
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Blocks.DIRT),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(Blocks.MUD),
              true
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_mud"));
        //Gunpowder -> sulfur
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Tags.Items.GUNPOWDERS),
              IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.getItemStack(),
              true
        ).build(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Blocks.TERRACOTTA),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(Blocks.CLAY),
              true
        ).build(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
        addChemicalInjectorConcreteRecipes(consumer, basePath + "concrete/");
        addChemicalInjectorCoralRevivalRecipes(consumer, basePath + "coral/");
        addChemicalInjectorOxidizingRecipe(consumer, basePath + "oxidizing/");
    }

    private void addChemicalInjectorConcreteRecipes(RecipeOutput consumer, String basePath) {
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE, "black");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE, "blue");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE, "brown");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE, "cyan");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE, "gray");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE, "green");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE, "light_blue");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE, "light_gray");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE, "lime");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE, "magenta");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE, "orange");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE, "pink");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE, "purple");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE, "red");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE, "white");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE, "yellow");
    }

    private void addChemicalInjectorConcreteRecipe(RecipeOutput consumer, String basePath, ItemLike powder, ItemLike concrete, String name) {
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(powder),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(concrete),
              true
        ).build(consumer, Mekanism.rl(basePath + name));
    }

    private void addChemicalInjectorCoralRevivalRecipes(RecipeOutput consumer, String basePath) {
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_BRAIN_CORAL, Blocks.BRAIN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_BUBBLE_CORAL, Blocks.BUBBLE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_FIRE_CORAL, Blocks.FIRE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_HORN_CORAL, Blocks.HORN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Blocks.DEAD_TUBE_CORAL, Blocks.TUBE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BRAIN_CORAL_FAN, Items.BRAIN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BUBBLE_CORAL_FAN, Items.BUBBLE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_FIRE_CORAL_FAN, Items.FIRE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_HORN_CORAL_FAN, Items.HORN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_TUBE_CORAL_FAN, Items.TUBE_CORAL_FAN, 3);
    }

    private void addChemicalInjectorCoralRevivalRecipe(RecipeOutput consumer, String basePath, ItemLike dead, ItemLike living, int water) {
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(dead),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, water),
              new ItemStack(living),
              true
        ).build(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(living.asItem())));
    }

    private void addChemicalInjectorOxidizingRecipe(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from weathering recipe set
        ChemicalStackIngredient oxygen = IngredientCreatorAccess.chemicalStack().from(MekanismChemicals.OXYGEN, 1);
        //TODO - 1.21: Switch this to being created at runtime and making use of the Neo DataMap?
        // https://github.com/neoforged/NeoForge/commit/87875183dcd8239404cbddbe8717db1dbe4f64ee
        for (Map.Entry<Block, Block> entry : WeatheringCopper.NEXT_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackChemicalToItemStackRecipeBuilder.injecting(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  oxygen,
                  new ItemStack(result),
                  true
            ).build(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result.asItem())));
        }
    }
}