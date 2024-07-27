package mekanism.common.recipe.impl;

import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

class SawingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "sawing/";
        addPrecisionSawmillBedRecipes(consumer, basePath + "bed/");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.ACACIA_PLANKS, Items.ACACIA_BOAT, Items.ACACIA_CHEST_BOAT, Items.ACACIA_DOOR,
              Blocks.ACACIA_FENCE_GATE, ItemTags.ACACIA_LOGS, Blocks.ACACIA_PRESSURE_PLATE, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_HANGING_SIGN, "acacia");
        //Note: We intentionally do not treat bamboo mosaic as wood as vanilla doesn't seem to do so anywhere
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.BAMBOO_PLANKS, Items.BAMBOO_RAFT, Items.BAMBOO_CHEST_RAFT,
              Items.BAMBOO_DOOR, Blocks.BAMBOO_FENCE_GATE, null, Blocks.BAMBOO_PRESSURE_PLATE, Blocks.BAMBOO_TRAPDOOR, Blocks.BAMBOO_HANGING_SIGN, "bamboo");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.BIRCH_PLANKS, Items.BIRCH_BOAT, Items.BIRCH_CHEST_BOAT, Items.BIRCH_DOOR,
              Blocks.BIRCH_FENCE_GATE, ItemTags.BIRCH_LOGS, Blocks.BIRCH_PRESSURE_PLATE, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_HANGING_SIGN, "birch");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.CHERRY_PLANKS, Items.CHERRY_BOAT, Items.CHERRY_CHEST_BOAT,
              Items.CHERRY_DOOR, Blocks.CHERRY_FENCE_GATE, ItemTags.CHERRY_LOGS, Blocks.CHERRY_PRESSURE_PLATE, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_HANGING_SIGN, "cherry");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.DARK_OAK_PLANKS, Items.DARK_OAK_BOAT, Items.DARK_OAK_CHEST_BOAT,
              Items.DARK_OAK_DOOR, Blocks.DARK_OAK_FENCE_GATE, ItemTags.DARK_OAK_LOGS, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.DARK_OAK_TRAPDOOR,
              Blocks.DARK_OAK_HANGING_SIGN, "dark_oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.JUNGLE_PLANKS, Items.JUNGLE_BOAT, Items.JUNGLE_CHEST_BOAT, Items.JUNGLE_DOOR,
              Blocks.JUNGLE_FENCE_GATE, ItemTags.JUNGLE_LOGS, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_HANGING_SIGN, "jungle");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.MANGROVE_PLANKS, Items.MANGROVE_BOAT, Items.MANGROVE_CHEST_BOAT,
              Items.MANGROVE_DOOR, Blocks.MANGROVE_FENCE_GATE, ItemTags.MANGROVE_LOGS, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.MANGROVE_TRAPDOOR,
              Blocks.MANGROVE_HANGING_SIGN, "mangrove");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.OAK_PLANKS, Items.OAK_BOAT, Items.OAK_CHEST_BOAT, Items.OAK_DOOR,
              Blocks.OAK_FENCE_GATE, ItemTags.OAK_LOGS, Blocks.OAK_PRESSURE_PLATE, Blocks.OAK_TRAPDOOR, Blocks.OAK_HANGING_SIGN, "oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.SPRUCE_PLANKS, Items.SPRUCE_BOAT, Items.SPRUCE_CHEST_BOAT, Items.SPRUCE_DOOR,
              Blocks.SPRUCE_FENCE_GATE, ItemTags.SPRUCE_LOGS, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_HANGING_SIGN, "spruce");

        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.CRIMSON_PLANKS, null, null, Items.CRIMSON_DOOR,
              Blocks.CRIMSON_FENCE_GATE, ItemTags.CRIMSON_STEMS, Blocks.CRIMSON_PRESSURE_PLATE, Blocks.CRIMSON_TRAPDOOR, Blocks.CRIMSON_HANGING_SIGN, "crimson");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Blocks.WARPED_PLANKS, null, null, Items.WARPED_DOOR,
              Blocks.WARPED_FENCE_GATE, ItemTags.WARPED_STEMS, Blocks.WARPED_PRESSURE_PLATE, Blocks.WARPED_TRAPDOOR, Blocks.WARPED_HANGING_SIGN, "warped");
        //Barrel
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.BARREL),
              new ItemStack(Blocks.OAK_PLANKS, 7)
        ).build(consumer, Mekanism.rl(basePath + "barrel"));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Tags.Items.BOOKSHELVES),
              new ItemStack(Blocks.OAK_PLANKS, 6),
              new ItemStack(Items.BOOK, 3),
              1
        ).build(consumer, Mekanism.rl(basePath + "bookshelf"));
        //Chiseled Bookshelf
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_BOOKSHELF),
              new ItemStack(Blocks.OAK_PLANKS, 6),
              new ItemStack(Blocks.OAK_SLAB, 3),
              1
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bookshelf"));
        //Chest
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.CHEST),
              new ItemStack(Blocks.OAK_PLANKS, 8)
        ).build(consumer, Mekanism.rl(basePath + "chest"));
        //Composter
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.COMPOSTER),
              new ItemStack(Blocks.OAK_SLAB, 7)
        ).build(consumer, Mekanism.rl(basePath + "composter"));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.CRAFTING_TABLE),
              new ItemStack(Blocks.OAK_PLANKS, 4)
        ).build(consumer, Mekanism.rl(basePath + "crafting_table"));
        //Fences
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Tags.Items.FENCES_WOODEN),
              new ItemStack(Items.STICK, 3)
        ).build(consumer, Mekanism.rl(basePath + "fences"));
        //Item Frame
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Items.ITEM_FRAME),
              new ItemStack(Items.STICK, 8),
              new ItemStack(Items.LEATHER),
              1
        ).build(consumer, Mekanism.rl(basePath + "item_frame"));
        //Jukebox
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.JUKEBOX),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.DIAMOND),
              1
        ).build(consumer, Mekanism.rl(basePath + "jukebox"));
        //Ladder
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.LADDER, 3),
              new ItemStack(Items.STICK, 7)
        ).build(consumer, Mekanism.rl(basePath + "ladder"));
        //Lectern
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.LECTERN),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).build(consumer, Mekanism.rl(basePath + "lectern"));
        //Note block
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.NOTE_BLOCK),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Items.REDSTONE),
              1
        ).build(consumer, Mekanism.rl(basePath + "note_block"));
        //Melons
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.MELON),
              new ItemStack(Items.MELON_SLICE, 9)
        ).build(consumer, Mekanism.rl(basePath + "melon"));
        //Planks
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(ItemTags.PLANKS),
              new ItemStack(Items.STICK, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).build(consumer, Mekanism.rl(basePath + "planks"));
        //Pumpkin
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.PUMPKIN),
              new ItemStack(Blocks.CARVED_PUMPKIN, 1),
              new ItemStack(Items.PUMPKIN_SEEDS, 4),
              1
        ).build(consumer, Mekanism.rl(basePath + "pumpkin"));
        //Redstone torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Items.REDSTONE_TORCH),
              new ItemStack(Items.STICK),
              new ItemStack(Items.REDSTONE),
              1
        ).build(consumer, Mekanism.rl(basePath + "redstone_torch"));
        //Slabs
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(ItemTags.WOODEN_SLABS),
              new ItemStack(Items.STICK, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.125
        ).build(consumer, Mekanism.rl(basePath + "slabs"));
        //Stairs
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(ItemTags.WOODEN_STAIRS),
              new ItemStack(Items.STICK, 9),
              MekanismItems.SAWDUST.getItemStack(),
              0.375
        ).build(consumer, Mekanism.rl(basePath + "stairs"));
        //Stick
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Tags.Items.RODS_WOODEN),
              MekanismItems.SAWDUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "stick"));
        //Buttons
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(ItemTags.WOODEN_BUTTONS),
              MekanismItems.SAWDUST.getItemStack(),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).build(consumer, Mekanism.rl(basePath + "button"));
        //Signs
        SawmillRecipeBuilder.sawing(
              //Note: We use the signs tag as vanilla only adds wood signs to it and also adds a burn time for things in this tag
              // as the only usage of the item tag, so it seems safe to assume any added ones are likely to be burnable
              IngredientCreatorAccess.item().from(ItemTags.SIGNS),
              new ItemStack(Items.STICK, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).build(consumer, Mekanism.rl(basePath + "sign"));
        //Torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Items.TORCH, 4),
              new ItemStack(Items.STICK),
              new ItemStack(Items.COAL),
              1
        ).build(consumer, Mekanism.rl(basePath + "torch"));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Items.SOUL_TORCH, 4),
              new ItemStack(Items.TORCH, 4),
              new ItemStack(Blocks.SOUL_SOIL),
              1
        ).build(consumer, Mekanism.rl(basePath + "soul_torch"));
        //Trapped chest
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(Blocks.TRAPPED_CHEST),
              new ItemStack(Blocks.OAK_PLANKS, 8),
              new ItemStack(Blocks.TRIPWIRE_HOOK),
              0.75
        ).build(consumer, Mekanism.rl(basePath + "trapped_chest"));
        //Bamboo block
        SawmillRecipeBuilder.sawing(
              //Note: We don't use the tag as turning stripped bamboo back into regular bamboo makes no sense
              IngredientCreatorAccess.item().from(Blocks.BAMBOO_BLOCK),
              new ItemStack(Items.BAMBOO, 9)
        ).build(consumer, Mekanism.rl(basePath + "bamboo_block"));
    }

    private void addPrecisionSawmillBedRecipes(RecipeOutput consumer, String basePath) {
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, DyeColor.BLACK);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, DyeColor.BLUE);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, DyeColor.BROWN);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, DyeColor.CYAN);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, DyeColor.GRAY);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, DyeColor.GREEN);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, DyeColor.LIGHT_BLUE);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, DyeColor.LIGHT_GRAY);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, DyeColor.LIME);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, DyeColor.MAGENTA);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, DyeColor.ORANGE);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, DyeColor.PINK);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, DyeColor.PURPLE);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, DyeColor.RED);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, DyeColor.WHITE);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, DyeColor.YELLOW);
    }
}