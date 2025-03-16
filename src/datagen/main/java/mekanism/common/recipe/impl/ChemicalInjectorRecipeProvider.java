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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
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
              IngredientCreatorAccess.item().from(Items.DIRT),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(Items.MUD),
              true
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_mud"));
        //Gunpowder -> sulfur
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Tags.Items.GUNPOWDERS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.asStack(),
              true
        ).build(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(Items.TERRACOTTA),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY),
              true
        ).build(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
        addChemicalInjectorConcreteRecipes(consumer, basePath + "concrete/");
        addChemicalInjectorCoralRevivalRecipes(consumer, basePath + "coral/");
        addChemicalInjectorOxidizingRecipe(consumer, basePath + "oxidizing/");
    }

    private void addChemicalInjectorConcreteRecipes(RecipeOutput consumer, String basePath) {
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.BLACK_CONCRETE_POWDER, Items.BLACK_CONCRETE, "black");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.BLUE_CONCRETE_POWDER, Items.BLUE_CONCRETE, "blue");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.BROWN_CONCRETE_POWDER, Items.BROWN_CONCRETE, "brown");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.CYAN_CONCRETE_POWDER, Items.CYAN_CONCRETE, "cyan");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.GRAY_CONCRETE_POWDER, Items.GRAY_CONCRETE, "gray");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.GREEN_CONCRETE_POWDER, Items.GREEN_CONCRETE, "green");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE, "light_blue");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CONCRETE, "light_gray");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.LIME_CONCRETE_POWDER, Items.LIME_CONCRETE, "lime");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_CONCRETE, "magenta");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.ORANGE_CONCRETE_POWDER, Items.ORANGE_CONCRETE, "orange");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.PINK_CONCRETE_POWDER, Items.PINK_CONCRETE, "pink");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.PURPLE_CONCRETE_POWDER, Items.PURPLE_CONCRETE, "purple");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.RED_CONCRETE_POWDER, Items.RED_CONCRETE, "red");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.WHITE_CONCRETE_POWDER, Items.WHITE_CONCRETE, "white");
        addChemicalInjectorConcreteRecipe(consumer, basePath, Items.YELLOW_CONCRETE_POWDER, Items.YELLOW_CONCRETE, "yellow");
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
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BRAIN_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BUBBLE_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_FIRE_CORAL_BLOCK, Items.FIRE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_HORN_CORAL_BLOCK, Items.HORN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_TUBE_CORAL_BLOCK, Items.TUBE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BRAIN_CORAL, Items.BRAIN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BUBBLE_CORAL, Items.BUBBLE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_FIRE_CORAL, Items.FIRE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_HORN_CORAL, Items.HORN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_TUBE_CORAL, Items.TUBE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BRAIN_CORAL_FAN, Items.BRAIN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_BUBBLE_CORAL_FAN, Items.BUBBLE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_FIRE_CORAL_FAN, Items.FIRE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_HORN_CORAL_FAN, Items.HORN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, Items.DEAD_TUBE_CORAL_FAN, Items.TUBE_CORAL_FAN, 3);
    }

    private void addChemicalInjectorCoralRevivalRecipe(RecipeOutput consumer, String basePath, Item dead, Item living, int water) {
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(dead),
              IngredientCreatorAccess.chemicalStack().from(MekanismTags.Chemicals.WATER_VAPOR, water),
              new ItemStack(living),
              true
        ).build(consumer, Mekanism.rl(basePath + BuiltInRegistries.ITEM.getKey(living).getPath()));
    }

    private void addChemicalInjectorOxidizingRecipe(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from weathering recipe set
        ChemicalStackIngredient oxygen = IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1);
        //TODO - 1.22: Switch this to being created at runtime and making use of the Neo DataMap?
        // https://github.com/neoforged/NeoForge/commit/87875183dcd8239404cbddbe8717db1dbe4f64ee
        // Likely will require a PR based on https://github.com/neoforged/NeoForge/pull/1915 to move data maps before registries?
        for (Map.Entry<Block, Block> entry : WeatheringCopper.NEXT_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackChemicalToItemStackRecipeBuilder.injecting(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  oxygen,
                  new ItemStack(result),
                  true
            ).build(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }
}