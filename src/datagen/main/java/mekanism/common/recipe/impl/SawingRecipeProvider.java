package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class SawingRecipeProvider extends BaseSubRecipeProvider {

    SawingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "sawing/";
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.ACACIA_PLANKS, ItemIds.ACACIA_BOAT, ItemIds.ACACIA_CHEST_BOAT,
              BlockItemIds.ACACIA_DOOR, BlockItemIds.ACACIA_FENCE_GATE, ItemTags.ACACIA_LOGS, BlockItemIds.ACACIA_PRESSURE_PLATE, BlockItemIds.ACACIA_TRAPDOOR,
              BlockItemIds.ACACIA_HANGING_SIGN, BlockItemIds.STRIPPED_ACACIA_LOG, BlockItemIds.ACACIA_SHELF, WoodType.ACACIA);
        //Note: We intentionally do not treat bamboo mosaic as wood as vanilla doesn't seem to do so anywhere
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.BAMBOO_PLANKS, ItemIds.BAMBOO_RAFT, ItemIds.BAMBOO_CHEST_RAFT,
              BlockItemIds.BAMBOO_DOOR, BlockItemIds.BAMBOO_FENCE_GATE, null, BlockItemIds.BAMBOO_PRESSURE_PLATE, BlockItemIds.BAMBOO_TRAPDOOR,
              BlockItemIds.BAMBOO_HANGING_SIGN, BlockItemIds.STRIPPED_BAMBOO_BLOCK, BlockItemIds.BAMBOO_SHELF, WoodType.BAMBOO);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.BIRCH_PLANKS, ItemIds.BIRCH_BOAT, ItemIds.BIRCH_CHEST_BOAT,
              BlockItemIds.BIRCH_DOOR, BlockItemIds.BIRCH_FENCE_GATE, ItemTags.BIRCH_LOGS, BlockItemIds.BIRCH_PRESSURE_PLATE, BlockItemIds.BIRCH_TRAPDOOR,
              BlockItemIds.BIRCH_HANGING_SIGN, BlockItemIds.STRIPPED_BIRCH_LOG, BlockItemIds.BIRCH_SHELF, WoodType.BIRCH);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.CHERRY_PLANKS, ItemIds.CHERRY_BOAT, ItemIds.CHERRY_CHEST_BOAT,
              BlockItemIds.CHERRY_DOOR, BlockItemIds.CHERRY_FENCE_GATE, ItemTags.CHERRY_LOGS, BlockItemIds.CHERRY_PRESSURE_PLATE, BlockItemIds.CHERRY_TRAPDOOR,
              BlockItemIds.CHERRY_HANGING_SIGN, BlockItemIds.STRIPPED_CHERRY_LOG, BlockItemIds.CHERRY_SHELF, WoodType.CHERRY);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.DARK_OAK_PLANKS, ItemIds.DARK_OAK_BOAT,
              ItemIds.DARK_OAK_CHEST_BOAT, BlockItemIds.DARK_OAK_DOOR, BlockItemIds.DARK_OAK_FENCE_GATE, ItemTags.DARK_OAK_LOGS, BlockItemIds.DARK_OAK_PRESSURE_PLATE,
              BlockItemIds.DARK_OAK_TRAPDOOR, BlockItemIds.DARK_OAK_HANGING_SIGN, BlockItemIds.STRIPPED_DARK_OAK_LOG, BlockItemIds.DARK_OAK_SHELF, WoodType.DARK_OAK);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.JUNGLE_PLANKS, ItemIds.JUNGLE_BOAT, ItemIds.JUNGLE_CHEST_BOAT,
              BlockItemIds.JUNGLE_DOOR, BlockItemIds.JUNGLE_FENCE_GATE, ItemTags.JUNGLE_LOGS, BlockItemIds.JUNGLE_PRESSURE_PLATE, BlockItemIds.JUNGLE_TRAPDOOR,
              BlockItemIds.JUNGLE_HANGING_SIGN, BlockItemIds.STRIPPED_JUNGLE_LOG, BlockItemIds.JUNGLE_SHELF, WoodType.JUNGLE);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.MANGROVE_PLANKS, ItemIds.MANGROVE_BOAT,
              ItemIds.MANGROVE_CHEST_BOAT, BlockItemIds.MANGROVE_DOOR, BlockItemIds.MANGROVE_FENCE_GATE, ItemTags.MANGROVE_LOGS, BlockItemIds.MANGROVE_PRESSURE_PLATE,
              BlockItemIds.MANGROVE_TRAPDOOR, BlockItemIds.MANGROVE_HANGING_SIGN, BlockItemIds.STRIPPED_MANGROVE_LOG, BlockItemIds.MANGROVE_SHELF, WoodType.MANGROVE);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.OAK_PLANKS, ItemIds.OAK_BOAT, ItemIds.OAK_CHEST_BOAT,
              BlockItemIds.OAK_DOOR, BlockItemIds.OAK_FENCE_GATE, ItemTags.OAK_LOGS, BlockItemIds.OAK_PRESSURE_PLATE, BlockItemIds.OAK_TRAPDOOR,
              BlockItemIds.OAK_HANGING_SIGN, BlockItemIds.STRIPPED_OAK_LOG, BlockItemIds.OAK_SHELF, WoodType.OAK);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.PALE_OAK_PLANKS, ItemIds.PALE_OAK_BOAT,
              ItemIds.PALE_OAK_CHEST_BOAT, BlockItemIds.PALE_OAK_DOOR, BlockItemIds.PALE_OAK_FENCE_GATE, ItemTags.PALE_OAK_LOGS, BlockItemIds.PALE_OAK_PRESSURE_PLATE,
              BlockItemIds.PALE_OAK_TRAPDOOR, BlockItemIds.PALE_OAK_HANGING_SIGN, BlockItemIds.STRIPPED_PALE_OAK_LOG, BlockItemIds.PALE_OAK_SHELF, WoodType.PALE_OAK);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.POPLAR_PLANKS, ItemIds.POPLAR_BOAT,
              ItemIds.POPLAR_CHEST_BOAT, BlockItemIds.POPLAR_DOOR, BlockItemIds.POPLAR_FENCE_GATE, ItemTags.POPLAR_LOGS, BlockItemIds.POPLAR_PRESSURE_PLATE,
              BlockItemIds.POPLAR_TRAPDOOR, BlockItemIds.POPLAR_HANGING_SIGN, BlockItemIds.STRIPPED_POPLAR_LOG, BlockItemIds.POPLAR_SHELF, WoodType.POPLAR);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.SPRUCE_PLANKS, ItemIds.SPRUCE_BOAT, ItemIds.SPRUCE_CHEST_BOAT,
              BlockItemIds.SPRUCE_DOOR, BlockItemIds.SPRUCE_FENCE_GATE, ItemTags.SPRUCE_LOGS, BlockItemIds.SPRUCE_PRESSURE_PLATE, BlockItemIds.SPRUCE_TRAPDOOR,
              BlockItemIds.SPRUCE_HANGING_SIGN, BlockItemIds.STRIPPED_SPRUCE_LOG, BlockItemIds.SPRUCE_SHELF, WoodType.SPRUCE);

        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.CRIMSON_PLANKS, null, null,
              BlockItemIds.CRIMSON_DOOR, BlockItemIds.CRIMSON_FENCE_GATE, ItemTags.CRIMSON_STEMS, BlockItemIds.CRIMSON_PRESSURE_PLATE, BlockItemIds.CRIMSON_TRAPDOOR,
              BlockItemIds.CRIMSON_HANGING_SIGN, BlockItemIds.STRIPPED_CRIMSON_STEM, BlockItemIds.CRIMSON_SHELF, WoodType.CRIMSON);
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, this.items, basePath, BlockItemIds.WARPED_PLANKS, null, null,
              BlockItemIds.WARPED_DOOR, BlockItemIds.WARPED_FENCE_GATE, ItemTags.WARPED_STEMS, BlockItemIds.WARPED_PRESSURE_PLATE, BlockItemIds.WARPED_TRAPDOOR,
              BlockItemIds.WARPED_HANGING_SIGN, BlockItemIds.STRIPPED_WARPED_STEM, BlockItemIds.WARPED_SHELF, WoodType.WARPED);

        addBeds(consumer, basePath + "bed/");
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
              IngredientCreatorAccess.item().from(items, Tags.Items.PUMPKINS_NORMAL),
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
        //Creaking heart
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.CREAKING_HEART),
              template(BlockItemIds.PALE_OAK_LOG, 2),
              template(BlockItemIds.RESIN_BLOCK),
              1
        ).save(consumer, Mekanism.rl(basePath + "creaking_heart"));
        //Dried Ghast -> Ghast Tears
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DRIED_GHAST),
              template(ItemIds.GHAST_TEAR, 2),
              template(ItemIds.GHAST_TEAR, 3),
              0.33
        ).save(consumer, Mekanism.rl(basePath + "dried_ghast"));
        //Saddle
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, ItemIds.SADDLE),
              template(ItemIds.LEATHER, 3),
              template(ItemIds.IRON_INGOT),
              0.05
        ).save(consumer, Mekanism.rl(basePath + "saddle"));
    }

    private void addBeds(RecipeOutput consumer, String basePath) {
        ColorCollection.VALUES.forEach(color -> SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.BED.pick(color)),
              template(BlockItemIds.OAK_PLANKS, 3),
              template(BlockItemIds.WOOL.pick(color), 3),
              1
        ).save(consumer, Mekanism.rl(basePath + color)));
        SawmillRecipeBuilder.sawing(
              IngredientCreatorAccess.item().from(items, BlockItemIds.STRAW_BED),
              template(ItemIds.WHEAT, 6),
              template(ItemIds.WHEAT),
              0.75
        ).save(consumer, Mekanism.rl(basePath + "straw"));
    }
}