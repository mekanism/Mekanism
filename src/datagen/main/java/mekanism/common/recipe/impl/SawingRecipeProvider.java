package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class SawingRecipeProvider extends BaseSubRecipeProvider {

    SawingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "sawing/";
        ColorCollection.VALUES.forEach(color -> SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.BED.pick(color)),
              template(BlockItemIds.OAK_PLANKS, 3),
              template(BlockItemIds.WOOL.pick(color), 3),
              1
        ).save(consumer, Mekanism.rl(basePath + "bed/" + color)));
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.ACACIA_PLANKS, Items.ACACIA_BOAT, Items.ACACIA_CHEST_BOAT, Items.ACACIA_DOOR,
              Items.ACACIA_FENCE_GATE, ItemTags.ACACIA_LOGS, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, Items.ACACIA_HANGING_SIGN, "acacia");
        //Note: We intentionally do not treat bamboo mosaic as wood as vanilla doesn't seem to do so anywhere
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.BAMBOO_PLANKS, Items.BAMBOO_RAFT, Items.BAMBOO_CHEST_RAFT,
              Items.BAMBOO_DOOR, Items.BAMBOO_FENCE_GATE, null, Items.BAMBOO_PRESSURE_PLATE, Items.BAMBOO_TRAPDOOR, Items.BAMBOO_HANGING_SIGN, "bamboo");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.BIRCH_PLANKS, Items.BIRCH_BOAT, Items.BIRCH_CHEST_BOAT, Items.BIRCH_DOOR,
              Items.BIRCH_FENCE_GATE, ItemTags.BIRCH_LOGS, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, Items.BIRCH_HANGING_SIGN, "birch");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.CHERRY_PLANKS, Items.CHERRY_BOAT, Items.CHERRY_CHEST_BOAT,
              Items.CHERRY_DOOR, Items.CHERRY_FENCE_GATE, ItemTags.CHERRY_LOGS, Items.CHERRY_PRESSURE_PLATE, Items.CHERRY_TRAPDOOR, Items.CHERRY_HANGING_SIGN, "cherry");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.DARK_OAK_PLANKS, Items.DARK_OAK_BOAT, Items.DARK_OAK_CHEST_BOAT,
              Items.DARK_OAK_DOOR, Items.DARK_OAK_FENCE_GATE, ItemTags.DARK_OAK_LOGS, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR,
              Items.DARK_OAK_HANGING_SIGN, "dark_oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.JUNGLE_PLANKS, Items.JUNGLE_BOAT, Items.JUNGLE_CHEST_BOAT, Items.JUNGLE_DOOR,
              Items.JUNGLE_FENCE_GATE, ItemTags.JUNGLE_LOGS, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, Items.JUNGLE_HANGING_SIGN, "jungle");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.MANGROVE_PLANKS, Items.MANGROVE_BOAT, Items.MANGROVE_CHEST_BOAT,
              Items.MANGROVE_DOOR, Items.MANGROVE_FENCE_GATE, ItemTags.MANGROVE_LOGS, Items.MANGROVE_PRESSURE_PLATE, Items.MANGROVE_TRAPDOOR,
              Items.MANGROVE_HANGING_SIGN, "mangrove");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.OAK_PLANKS, Items.OAK_BOAT, Items.OAK_CHEST_BOAT, Items.OAK_DOOR,
              Items.OAK_FENCE_GATE, ItemTags.OAK_LOGS, Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, Items.OAK_HANGING_SIGN, "oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.SPRUCE_PLANKS, Items.SPRUCE_BOAT, Items.SPRUCE_CHEST_BOAT, Items.SPRUCE_DOOR,
              Items.SPRUCE_FENCE_GATE, ItemTags.SPRUCE_LOGS, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, Items.SPRUCE_HANGING_SIGN, "spruce");

        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.CRIMSON_PLANKS, null, null, Items.CRIMSON_DOOR,
              Items.CRIMSON_FENCE_GATE, ItemTags.CRIMSON_STEMS, Items.CRIMSON_PRESSURE_PLATE, Items.CRIMSON_TRAPDOOR, Items.CRIMSON_HANGING_SIGN, "crimson");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, Items.WARPED_PLANKS, null, null, Items.WARPED_DOOR,
              Items.WARPED_FENCE_GATE, ItemTags.WARPED_STEMS, Items.WARPED_PRESSURE_PLATE, Items.WARPED_TRAPDOOR, Items.WARPED_HANGING_SIGN, "warped");
        //Barrel
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.BARREL),
              template(BlockItemIds.OAK_PLANKS, 7)
        ).save(consumer, Mekanism.rl(basePath + "barrel"));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.BOOKSHELVES),
              template(BlockItemIds.OAK_PLANKS, 6),
              template(ItemIds.BOOK, 3),
              1
        ).save(consumer, Mekanism.rl(basePath + "bookshelf"));
        //Chiseled Bookshelf
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.CHISELED_BOOKSHELF),
              template(BlockItemIds.OAK_PLANKS, 6),
              template(BlockItemIds.OAK_SLAB, 3),
              1
        ).save(consumer, Mekanism.rl(basePath + "chiseled_bookshelf"));
        //Chest
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.CHEST),
              template(BlockItemIds.OAK_PLANKS, 8)
        ).save(consumer, Mekanism.rl(basePath + "chest"));
        //Composter
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.COMPOSTER),
              template(BlockItemIds.OAK_SLAB, 7)
        ).save(consumer, Mekanism.rl(basePath + "composter"));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.CRAFTING_TABLE),
              template(BlockItemIds.OAK_PLANKS, 4)
        ).save(consumer, Mekanism.rl(basePath + "crafting_table"));
        //Fences
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.FENCES_WOODEN),
              template(ItemIds.STICK, 3)
        ).save(consumer, Mekanism.rl(basePath + "fences"));
        //Item Frame
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, ItemIds.ITEM_FRAME),
              template(ItemIds.STICK, 8),
              template(ItemIds.LEATHER),
              1
        ).save(consumer, Mekanism.rl(basePath + "item_frame"));
        //Jukebox
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.JUKEBOX),
              template(BlockItemIds.OAK_PLANKS, 8),
              template(ItemIds.DIAMOND),
              1
        ).save(consumer, Mekanism.rl(basePath + "jukebox"));
        //Ladder
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.LADDER, 3),
              template(ItemIds.STICK, 7)
        ).save(consumer, Mekanism.rl(basePath + "ladder"));
        //Lectern
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.LECTERN),
              template(BlockItemIds.OAK_PLANKS, 8),
              template(ItemIds.BOOK, 3),
              1
        ).save(consumer, Mekanism.rl(basePath + "lectern"));
        //Note block
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.NOTE_BLOCK),
              template(BlockItemIds.OAK_PLANKS, 8),
              template(BlockItemIds.REDSTONE_DUST),
              1
        ).save(consumer, Mekanism.rl(basePath + "note_block"));
        //Melons
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.MELON),
              template(ItemIds.MELON_SLICE, 9)
        ).save(consumer, Mekanism.rl(basePath + "melon"));
        //Planks
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.PLANKS),
              template(ItemIds.STICK, 6),
              MekanismItems.SAWDUST.asTemplate(),
              0.25
        ).save(consumer, Mekanism.rl(basePath + "planks"));
        //Pumpkin
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.PUMPKIN),
              template(BlockItemIds.CARVED_PUMPKIN, 1),
              template(BlockItemIds.PUMPKIN_CROP, 4),
              1
        ).save(consumer, Mekanism.rl(basePath + "pumpkin"));
        //Redstone torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.REDSTONE_TORCH),
              template(ItemIds.STICK),
              template(BlockItemIds.REDSTONE_DUST),
              1
        ).save(consumer, Mekanism.rl(basePath + "redstone_torch"));
        //Slabs
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOODEN_SLABS),
              template(ItemIds.STICK, 3),
              MekanismItems.SAWDUST.asTemplate(),
              0.125
        ).save(consumer, Mekanism.rl(basePath + "slabs"));
        //Stairs
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOODEN_STAIRS),
              template(ItemIds.STICK, 9),
              MekanismItems.SAWDUST.asTemplate(),
              0.375
        ).save(consumer, Mekanism.rl(basePath + "stairs"));
        //Stick
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RODS_WOODEN),
              MekanismItems.SAWDUST.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "stick"));
        //Buttons
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOODEN_BUTTONS),
              MekanismItems.SAWDUST.asTemplate(),
              MekanismItems.SAWDUST.asTemplate(),
              0.25
        ).save(consumer, Mekanism.rl(basePath + "button"));
        //Signs
        SawmillRecipeBuilder.sawing(
              //Note: We use the signs tag as vanilla only adds wood signs to it and also adds a burn time for things in this tag
              // as the only usage of the item tag, so it seems safe to assume any added ones are likely to be burnable
              IngredientCreatorAccess.item().from(this.items, ItemTags.SIGNS),
              template(ItemIds.STICK, 3),
              MekanismItems.SAWDUST.asTemplate(),
              0.25
        ).save(consumer, Mekanism.rl(basePath + "sign"));
        //Torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.TORCH, 4),
              template(ItemIds.STICK),
              template(ItemIds.COAL),
              1
        ).save(consumer, Mekanism.rl(basePath + "torch"));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.SOUL_TORCH, 4),
              template(BlockItemIds.TORCH, 4),
              template(BlockItemIds.SOUL_SOIL),
              1
        ).save(consumer, Mekanism.rl(basePath + "soul_torch"));
        //Trapped chest
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.TRAPPED_CHEST),
              template(BlockItemIds.OAK_PLANKS, 8),
              template(BlockItemIds.TRIPWIRE_HOOK),
              0.75
        ).save(consumer, Mekanism.rl(basePath + "trapped_chest"));
        //Bamboo block
        SawmillRecipeBuilder.sawing(
              //Note: We don't use the tag as turning stripped bamboo back into regular bamboo makes no sense
              IngredientCreatorAccess.item().from(items, BlockItemIds.BAMBOO_BLOCK),
              template(BlockItemIds.BAMBOO, 9)
        ).save(consumer, Mekanism.rl(basePath + "bamboo_block"));
    }
}