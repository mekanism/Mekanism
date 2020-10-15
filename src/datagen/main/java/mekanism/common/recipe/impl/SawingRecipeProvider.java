package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

class SawingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "sawing/";
        addPrecisionSawmillBedRecipes(consumer, basePath + "bed/");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.ACACIA_PLANKS, Items.ACACIA_BOAT, Items.ACACIA_DOOR, Blocks.ACACIA_FENCE_GATE,
              ItemTags.ACACIA_LOGS, Blocks.ACACIA_PRESSURE_PLATE, Blocks.ACACIA_TRAPDOOR, "acacia");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.BIRCH_PLANKS, Items.BIRCH_BOAT, Items.BIRCH_DOOR, Blocks.BIRCH_FENCE_GATE,
              ItemTags.BIRCH_LOGS, Blocks.BIRCH_PRESSURE_PLATE, Blocks.BIRCH_TRAPDOOR, "birch");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.DARK_OAK_PLANKS, Items.DARK_OAK_BOAT, Items.DARK_OAK_DOOR, Blocks.DARK_OAK_FENCE_GATE,
              ItemTags.DARK_OAK_LOGS, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.DARK_OAK_TRAPDOOR, "dark_oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.JUNGLE_PLANKS, Items.JUNGLE_BOAT, Items.JUNGLE_DOOR, Blocks.JUNGLE_FENCE_GATE,
              ItemTags.JUNGLE_LOGS, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.JUNGLE_TRAPDOOR, "jungle");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.OAK_PLANKS, Items.OAK_BOAT, Items.OAK_DOOR, Blocks.OAK_FENCE_GATE, ItemTags.OAK_LOGS,
              Blocks.OAK_PRESSURE_PLATE, Blocks.OAK_TRAPDOOR, "oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.SPRUCE_PLANKS, Items.SPRUCE_BOAT, Items.SPRUCE_DOOR, Blocks.SPRUCE_FENCE_GATE,
              ItemTags.SPRUCE_LOGS, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.SPRUCE_TRAPDOOR, "spruce");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.CRIMSON_PLANKS, null, Items.CRIMSON_DOOR, Blocks.CRIMSON_FENCE_GATE,
              ItemTags.CRIMSON_STEMS, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.CRIMSON_TRAPDOOR, "crimson");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.WARPED_PLANKS, null, Items.WARPED_DOOR, Blocks.WARPED_FENCE_GATE,
              ItemTags.WARPED_STEMS, Blocks.WARPED_PRESSURE_PLATE, Blocks.WARPED_TRAPDOOR, "warped");
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.BARREL),
              new ItemStack(Blocks.OAK_PLANKS, 7)
        ).build(consumer, Mekanism.rl(basePath + "barrel"));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Tags.Items.BOOKSHELVES),
              new ItemStack(Blocks.OAK_PLANKS, 6),
              new ItemStack(Items.BOOK, 3),
              1
        ).build(consumer, Mekanism.rl(basePath + "bookshelf"));
        //Chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.CHEST),
              new ItemStack(Blocks.OAK_PLANKS, 8)
        ).build(consumer, Mekanism.rl(basePath + "chest"));
        //Composter
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.COMPOSTER),
              new ItemStack(Blocks.OAK_SLAB, 7)
        ).build(consumer, Mekanism.rl(basePath + "composter"));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.CRAFTING_TABLE),
              new ItemStack(Blocks.OAK_PLANKS, 4)
        ).build(consumer, Mekanism.rl(basePath + "crafting_table"));
        //Fences
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Tags.Items.FENCES_WOODEN),
              new ItemStack(Items.STICK, 3)
        ).build(consumer, Mekanism.rl(basePath + "fences"));
        //Item Frame
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.ITEM_FRAME),
              new ItemStack(Items.STICK, 8),
              new ItemStack(Items.LEATHER),
              1
        ).build(consumer, Mekanism.rl(basePath + "item_frame"));
        //Jukebox
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.JUKEBOX),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.DIAMOND),
              1
        ).build(consumer, Mekanism.rl(basePath + "jukebox"));
        //Ladder
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.LADDER, 3),
              new ItemStack(Items.STICK, 7)
        ).build(consumer, Mekanism.rl(basePath + "ladder"));
        //Lectern
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.LECTERN),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).build(consumer, Mekanism.rl(basePath + "lectern"));
        //Note block
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.NOTE_BLOCK),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.REDSTONE),
              1
        ).build(consumer, Mekanism.rl(basePath + "note_block"));
        //Planks
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.PLANKS),
              new ItemStack(Items.STICK, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).build(consumer, Mekanism.rl(basePath + "planks"));
        //Redstone torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.REDSTONE_TORCH),
              new ItemStack(Items.STICK),
              new ItemStack(Items.REDSTONE),
              1
        ).build(consumer, Mekanism.rl(basePath + "redstone_torch"));
        //Slabs
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.WOODEN_SLABS),
              new ItemStack(Items.STICK, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.125
        ).build(consumer, Mekanism.rl(basePath + "slabs"));
        //Stairs
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.WOODEN_STAIRS),
              new ItemStack(Items.STICK, 9),
              MekanismItems.SAWDUST.getItemStack(),
              0.375
        ).build(consumer, Mekanism.rl(basePath + "stairs"));
        //Stick
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Tags.Items.RODS_WOODEN),
              MekanismItems.SAWDUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "stick"));
        //Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.TORCH, 4),
              new ItemStack(Items.STICK),
              new ItemStack(Items.COAL),
              1
        ).build(consumer, Mekanism.rl(basePath + "torch"));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.SOUL_TORCH, 4),
              new ItemStack(Items.TORCH, 4),
              new ItemStack(Blocks.SOUL_SOIL),
              1
        ).build(consumer, Mekanism.rl(basePath + "soul_torch"));
        //Trapped chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Blocks.TRAPPED_CHEST),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Blocks.TRIPWIRE_HOOK),
              0.75
        ).build(consumer, Mekanism.rl(basePath + "trapped_chest"));
    }

    private void addPrecisionSawmillBedRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, Blocks.BLACK_WOOL, "black");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, Blocks.BLUE_WOOL, "blue");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, Blocks.BROWN_WOOL, "brown");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, Blocks.CYAN_WOOL, "cyan");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, Blocks.GRAY_WOOL, "gray");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, Blocks.GREEN_WOOL, "green");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL, "light_blue");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL, "light_gray");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, Blocks.LIME_WOOL, "lime");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, Blocks.MAGENTA_WOOL, "magenta");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, Blocks.ORANGE_WOOL, "orange");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, Blocks.PINK_WOOL, "pink");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, Blocks.PURPLE_WOOL, "purple");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, Blocks.RED_WOOL, "red");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, Blocks.WHITE_WOOL, "white");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, Blocks.YELLOW_WOOL, "yellow");
    }
}