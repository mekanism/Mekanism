package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

class CombinerRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "combining/";
        addCombinerDyeRecipes(consumer, basePath + "dye/");
        //Gravel
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Items.FLINT),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "gravel"));
        //Obsidian
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_OBSIDIAN, 4),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.OBSIDIAN)
        ).build(consumer, Mekanism.rl(basePath + "obsidian"));
    }

    private void addCombinerDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black + white -> light gray
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLACK),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE, 2),
              new ItemStack(Items.LIGHT_GRAY_DYE, 6)
        ).build(consumer, Mekanism.rl(basePath + "black_to_light_gray"));
        //Blue + green -> cyan
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_GREEN),
              new ItemStack(Items.CYAN_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "cyan"));
        //Gray + white -> light gray
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_GRAY),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_GRAY_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "gray_to_light_gray"));
        //Blue + white -> light blue
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_BLUE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Green + white -> lime
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_GREEN),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIME_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "lime"));
        //Purple + pink -> magenta
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_PURPLE),
              ItemStackIngredient.from(Tags.Items.DYES_PINK),
              new ItemStack(Items.MAGENTA_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "magenta"));
        //Red + yellow -> orange
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              ItemStackIngredient.from(Tags.Items.DYES_YELLOW),
              new ItemStack(Items.ORANGE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //Red + white -> pink
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.PINK_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "pink"));
        //Blue + red -> purple
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              new ItemStack(Items.PURPLE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "purple"));
    }
}