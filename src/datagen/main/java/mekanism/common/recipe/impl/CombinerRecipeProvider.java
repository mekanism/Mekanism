package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.recipes.inputs.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

class CombinerRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "combining/";
        addCombinerDyeRecipes(consumer, basePath + "dye/");
        //Gravel
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Items.FLINT),
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_NORMAL),
              new ItemStack(Blocks.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "gravel"));
        //Obsidian
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_OBSIDIAN, 4),
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_DEEPSLATE),
              new ItemStack(Blocks.OBSIDIAN)
        ).build(consumer, Mekanism.rl(basePath + "obsidian"));
    }

    private void addCombinerDyeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Black + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_BLACK),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_WHITE, 2),
              new ItemStack(Items.LIGHT_GRAY_DYE, 6)
        ).build(consumer, Mekanism.rl(basePath + "black_to_light_gray"));
        //Blue + green -> cyan
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_GREEN),
              new ItemStack(Items.CYAN_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "cyan"));
        //Gray + white -> light gray
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_GRAY),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_GRAY_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "gray_to_light_gray"));
        //Blue + white -> light blue
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_BLUE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Green + white -> lime
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_GREEN),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIME_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "lime"));
        //Purple + pink -> magenta
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_PURPLE),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_PINK),
              new ItemStack(Items.MAGENTA_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "magenta"));
        //Red + yellow -> orange
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_YELLOW),
              new ItemStack(Items.ORANGE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //Red + white -> pink
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_RED),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.PINK_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "pink"));
        //Blue + red -> purple
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.DYES_BLUE),
              IngredientCreatorAccess.item().from(Tags.Items.DYES_RED),
              new ItemStack(Items.PURPLE_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "purple"));
    }
}