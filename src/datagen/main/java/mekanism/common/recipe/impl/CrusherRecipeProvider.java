package mekanism.common.recipe.impl;

import java.util.Arrays;
import java.util.Map;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

class CrusherRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "crushing/";
        addCrusherBioFuelRecipes(consumer, basePath + "biofuel/");
        addCrusherDewaxingRecipes(consumer, basePath + "dewax/");
        addCrusherStoneRecipes(consumer, basePath + "stone/");
        addCrusherTuffRecipes(consumer, basePath + "tuff/");
        addCrusherDeepslateRecipes(consumer, basePath + "deepslate/");
        addCrusherBlackstoneRecipes(consumer, basePath + "blackstone/");
        addCrusherQuartzRecipes(consumer, basePath + "quartz/");
        addCrusherGraniteRecipes(consumer, basePath + "granite/");
        addCrusherDioriteRecipes(consumer, basePath + "diorite/");
        addCrusherAndesiteRecipes(consumer, basePath + "andesite/");
        addCrusherPrismarineRecipes(consumer, basePath + "prismarine/");
        //Dripstone Block -> Pointed Dripstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DRIPSTONE_BLOCK),
              new ItemStack(Items.POINTED_DRIPSTONE, 4)
        ).build(consumer, Mekanism.rl(basePath + "pointed_dripstone_from_block"));
        //Honecomb Block -> Honeycomb
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.HONEYCOMB_BLOCK),
              new ItemStack(Items.HONEYCOMB, 4)
        ).build(consumer, Mekanism.rl(basePath + "honeycomb_from_block"));
        //Purpur Block -> Purpur Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PURPUR_PILLAR),
              new ItemStack(Items.PURPUR_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "purpur_block_from_pillar"));
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_NORMAL),
              new ItemStack(Items.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.FLINT),
              new ItemStack(Items.GUNPOWDER)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.GRAVELS),
              new ItemStack(Items.SAND)
        ).build(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //Mud bricks -> packed mud
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.MUD_BRICKS),
              new ItemStack(Items.PACKED_MUD)
        ).build(consumer, Mekanism.rl(basePath + "mud_bricks_to_packed"));
        //Break music disc 5
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.MUSIC_DISC_5),
              new ItemStack(Items.DISC_FRAGMENT_5, 9)
        ).build(consumer, Mekanism.rl(basePath + "break_disc_5"));
        //Obsidian -> obsidian dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.OBSIDIANS_NORMAL),
              MekanismItems.OBSIDIAN_DUST.asStack(4)
        ).build(consumer, Mekanism.rl(basePath + "obsidian_to_dust"));
        //Blaze Rod -> blaze powder
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.RODS_BLAZE),
              new ItemStack(Items.BLAZE_POWDER, 4)
        ).build(consumer, Mekanism.rl(basePath + "blaze_rod"));
        //Breeze Rod -> wind charge
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.RODS_BREEZE),
              new ItemStack(Items.WIND_CHARGE, 6)
        ).build(consumer, Mekanism.rl(basePath + "breeze_rod"));
        //Bone -> bone meal
        final int BONEMEAL_FROM_BONE = 6;
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.BONE),
              new ItemStack(Items.BONE_MEAL, BONEMEAL_FROM_BONE)
        ).build(consumer, Mekanism.rl(basePath + "bone"));
        //Bone block -> bone meal
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.BONE_BLOCK),
              new ItemStack(Items.BONE_MEAL, 9)//must be the same as vanilla needs to make a block
        ).build(consumer, Mekanism.rl(basePath + "bone_block"));
        //Red Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "red_sandstone_to_sand", null, Items.RED_SAND, Tags.Items.SANDSTONE_RED_BLOCKS);
        //Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "sandstone_to_sand", null, Items.SAND, Tags.Items.SANDSTONE_UNCOLORED_BLOCKS);
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).build(consumer, Mekanism.rl(basePath + "wool_to_string"));
        //Soul Soil -> Soul Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.SOUL_SOIL),
              new ItemStack(Items.SOUL_SAND)
        ).build(consumer, Mekanism.rl(basePath + "soul_soil_to_soul_sand"));
        //Polished or Smooth Basalt -> Basalt
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(
                    Items.POLISHED_BASALT,
                    Items.SMOOTH_BASALT
              ),
              new ItemStack(Items.BASALT)
        ).build(consumer, Mekanism.rl(basePath + "polished_or_smooth_basalt_to_basalt"));
        //Chiseled Nether Bricks -> Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_NETHER_BRICKS),
              new ItemStack(Items.NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_nether_bricks_to_nether_bricks"));
        //Nether Bricks -> Cracked Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.NETHER_BRICKS),
              new ItemStack(Items.CRACKED_NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "nether_bricks_to_cracked_nether_bricks"));
    }

    private void addCrusherStoneRecipes(RecipeOutput consumer, String basePath) {
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.STONE),
              new ItemStack(Items.COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "to_cobblestone"));
        //Stone Stairs -> Cobblestone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.STONE_STAIRS),
              new ItemStack(Items.COBBLESTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_cobblestone_stairs"));
        //Stone Slabs -> Cobblestone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.STONE_SLAB),
              new ItemStack(Items.COBBLESTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slabs_to_cobblestone_slabs"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_STONE_BRICKS),
              new ItemStack(Items.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.STONE_BRICKS),
              new ItemStack(Items.CRACKED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CRACKED_STONE_BRICKS),
              new ItemStack(Items.STONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherTuffRecipes(RecipeOutput consumer, String basePath) {
        //Polished Tuff -> Tuff
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_TUFF),
              new ItemStack(Items.TUFF)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Tuff Stairs -> Tuff Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_TUFF_STAIRS),
              new ItemStack(Items.TUFF_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished"));
        //Polished Tuff Slabs -> Tuff Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_TUFF_SLAB),
              new ItemStack(Items.TUFF_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slabs_from_polished"));
        //Polished Tuff Walls -> Tuff Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_TUFF_WALL),
              new ItemStack(Items.TUFF_WALL)
        ).build(consumer, Mekanism.rl(basePath + "wall_from_polished"));

        //Tuff Bricks -> Polished Tuff
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_BRICKS),
              new ItemStack(Items.POLISHED_TUFF)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_polished"));
        //Tuff Brick Stairs -> Polished Tuff Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_BRICK_STAIRS),
              new ItemStack(Items.POLISHED_TUFF_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "brick_stairs_to_polished"));
        //Tuff Brick Slabs -> Polished Tuff Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_BRICK_SLAB),
              new ItemStack(Items.POLISHED_TUFF_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "brick_slabs_to_polished"));
        //Tuff Brick Walls -> Polished Tuff Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_BRICK_WALL),
              new ItemStack(Items.POLISHED_TUFF_WALL)
        ).build(consumer, Mekanism.rl(basePath + "brick_wall_to_polished"));

        //Chiseled Tuff -> Tuff Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_TUFF),
              new ItemStack(Items.TUFF_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_to_brick"));

        //Tuff -> Chiseled Tuff
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF),
              new ItemStack(Items.CHISELED_TUFF)
        ).build(consumer, Mekanism.rl(basePath + "to_chiseled"));
        //Tuff Stairs -> Tuff Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_STAIRS),
              new ItemStack(Items.TUFF_BRICK_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_brick"));
        //Tuff Slabs -> Tuff Brick Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_SLAB),
              new ItemStack(Items.TUFF_BRICK_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_to_brick"));
        //Tuff Walls -> Tuff Brick Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.TUFF_WALL),
              new ItemStack(Items.TUFF_BRICK_WALL)
        ).build(consumer, Mekanism.rl(basePath + "wall_to_brick"));
    }

    private void addCrusherDeepslateRecipes(RecipeOutput consumer, String basePath) {
        //Deepslate -> Cobbled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE),
              new ItemStack(Items.COBBLED_DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "to_cobbled"));

        //Polished Deepslate -> Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DEEPSLATE),
              new ItemStack(Items.DEEPSLATE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "polished_to_bricks"));
        //Polished Deepslate Stairs -> Deepslate Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DEEPSLATE_STAIRS),
              new ItemStack(Items.DEEPSLATE_BRICK_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_brick"));
        //Polished Deepslate Slabs -> Deepslate Brick Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DEEPSLATE_SLAB),
              new ItemStack(Items.DEEPSLATE_BRICK_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_brick"));
        //Polished Deepslate Wall -> Deepslate Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DEEPSLATE_WALL),
              new ItemStack(Items.DEEPSLATE_BRICK_WALL)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_brick"));

        //Deepslate Bricks -> Cracked Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_BRICKS),
              new ItemStack(Items.CRACKED_DEEPSLATE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Deepslate Bricks -> Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CRACKED_DEEPSLATE_BRICKS),
              new ItemStack(Items.DEEPSLATE_TILES)
        ).build(consumer, Mekanism.rl(basePath + "cracked_bricks_to_tile"));

        //Deepslate Brick Stairs -> Deepslate Tile Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_BRICK_STAIRS),
              new ItemStack(Items.DEEPSLATE_TILE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "brick_stairs_to_tile"));
        //Deepslate Brick Slabs -> Deepslate Tile Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_BRICK_SLAB),
              new ItemStack(Items.DEEPSLATE_TILE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "brick_slabs_to_tile"));
        //Deepslate Brick Wall -> Deepslate Tile Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_BRICK_WALL),
              new ItemStack(Items.DEEPSLATE_TILE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "brick_wall_to_tile"));

        //Deepslate Tiles -> Cracked Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_TILES),
              new ItemStack(Items.CRACKED_DEEPSLATE_TILES)
        ).build(consumer, Mekanism.rl(basePath + "tile_to_cracked_tile"));
        //Cracked Deepslate Tiles -> Chiseled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CRACKED_DEEPSLATE_TILES),
              new ItemStack(Items.CHISELED_DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "cracked_tile_to_chiseled"));

        //Deepslate Tile Stairs -> Cobbled Deepslate Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_TILE_STAIRS),
              new ItemStack(Items.COBBLED_DEEPSLATE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "tile_stairs_to_cobbled"));
        //Deepslate Tile Slabs -> Cobbled Deepslate Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_TILE_SLAB),
              new ItemStack(Items.COBBLED_DEEPSLATE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "tile_slabs_to_cobbled"));
        //Deepslate Tile Wall -> Cobbled Deepslate Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.DEEPSLATE_TILE_WALL),
              new ItemStack(Items.COBBLED_DEEPSLATE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "tile_wall_to_cobbled"));

        //Chiseled Deepslate -> Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_DEEPSLATE),
              new ItemStack(Items.DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "from_chiseled"));
    }

    private void addCrusherBlackstoneRecipes(RecipeOutput consumer, String basePath) {
        //Polished Blackstone -> Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_BLACKSTONE),
              new ItemStack(Items.BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Blackstone Wall -> Blackstone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_BLACKSTONE_WALL),
              new ItemStack(Items.BLACKSTONE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_wall"));
        //Polished Blackstone Stairs -> Blackstone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_BLACKSTONE_STAIRS),
              new ItemStack(Items.BLACKSTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_stairs"));
        //Polished Blackstone Slabs -> Blackstone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_BLACKSTONE_SLAB),
              new ItemStack(Items.BLACKSTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_slabs"));
        //Chiseled Polished Blackstone Bricks -> Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_POLISHED_BLACKSTONE),
              new ItemStack(Items.POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Polished Blackstone Bricks -> Cracked Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Polished Blackstone Bricks -> Polished Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Items.POLISHED_BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherQuartzRecipes(RecipeOutput consumer, String basePath) {
        //Quartz Block -> Smooth Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.QUARTZ_BLOCK),
              new ItemStack(Items.SMOOTH_QUARTZ)
        ).build(consumer, Mekanism.rl(basePath + "to_smooth_quartz"));
        //Quartz Slab -> Smooth Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.QUARTZ_SLAB),
              new ItemStack(Items.SMOOTH_QUARTZ_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Quartz Stairs -> Smooth Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.QUARTZ_STAIRS),
              new ItemStack(Items.SMOOTH_QUARTZ_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Smooth Quartz Block -> Quartz Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.SMOOTH_QUARTZ),
              new ItemStack(Items.QUARTZ_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "smooth_to_bricks"));
        //Quartz Bricks -> Chiseled Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.QUARTZ_BRICKS),
              new ItemStack(Items.CHISELED_QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_chiseled"));
        //Chiseled Quartz Block -> Quartz Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHISELED_QUARTZ_BLOCK),
              new ItemStack(Items.QUARTZ_PILLAR)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_to_pillar"));
        //Quartz Pillar -> Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.QUARTZ_PILLAR),
              new ItemStack(Items.QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherGraniteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Granite -> Granite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_GRANITE),
              new ItemStack(Items.GRANITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Granite Stairs -> Granite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_GRANITE_STAIRS),
              new ItemStack(Items.GRANITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Granite Slab -> Granite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_GRANITE_SLAB),
              new ItemStack(Items.GRANITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherDioriteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Diorite -> Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DIORITE),
              new ItemStack(Items.DIORITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Diorite Stairs -> Granite Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DIORITE_STAIRS),
              new ItemStack(Items.DIORITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Diorite Slab -> Diorite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_DIORITE_SLAB),
              new ItemStack(Items.DIORITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherAndesiteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Andesite -> Andesite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_ANDESITE),
              new ItemStack(Items.ANDESITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Andesite Stairs -> Andesite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_ANDESITE_STAIRS),
              new ItemStack(Items.ANDESITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Andesite Slab -> Andesite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.POLISHED_ANDESITE_SLAB),
              new ItemStack(Items.ANDESITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherPrismarineRecipes(RecipeOutput consumer, String basePath) {
        //Prismarine -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_block"));
        //Prismarine Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_SLAB),
              new ItemStack(Items.PRISMARINE_SHARD, 2)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_slabs"));
        //Prismarine Stairs -> Prismarine Shards
        // Note: Uses 1 -> 4 as he stone cutter allows for one prismarine block to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_STAIRS),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_stairs"));
        //Prismarine Wall -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_WALL),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_wall"));
        //Prismarine Brick -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_BRICKS),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick"));
        //Prismarine Brick Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_BRICK_SLAB, 2),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick_slabs"));
        //Prismarine Brick Stairs -> Prismarine Shards
        // Note: Uses 1 -> 9 as the stone cutter allows for one brick to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.PRISMARINE_BRICK_STAIRS),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick_stairs"));
    }

    private void addCrusherDewaxingRecipes(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from de-waxing recipe set
        for (Map.Entry<Block, Block> entry : HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackToItemStackRecipeBuilder.crushing(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  new ItemStack(result)
            ).build(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }

    private void addCrusherBioFuelRecipes(RecipeOutput consumer, String basePath) {
        biofuel(consumer, basePath, "apple", 2, 1, Items.APPLE);
        biofuel(consumer, basePath, "baked_potato", 2, 1, Items.BAKED_POTATO);
        biofuel(consumer, basePath, "bamboo", 2, 1, Items.BAMBOO);
        biofuel(consumer, basePath, "berries", 1, 1, Tags.Items.FOODS_BERRY);
        biofuel(consumer, basePath, "big_dripleaf", 2, 1, Items.BIG_DRIPLEAF);
        biofuel(consumer, basePath, "bread", 4, 1, Items.BREAD);
        biofuel(consumer, basePath, "cake", 6, 1, Items.CAKE);
        biofuel(consumer, basePath, "carved_pumpkin", 2, 1, Items.CARVED_PUMPKIN);//pumpkin minus 4 seeds (sawmill)
        biofuel(consumer, basePath, "crimson_roots", 1, 1, Items.CRIMSON_ROOTS);
        biofuel(consumer, basePath, "cookie", 3, 4, Items.COOKIE);
        biofuel(consumer, basePath, "crops_tags", 2, 1, Tags.Items.CROPS_CARROT, Tags.Items.CROPS_CACTUS, Tags.Items.CROPS_POTATO, Tags.Items.CROPS_WHEAT, Tags.Items.CROPS_BEETROOT, Tags.Items.CROPS_NETHER_WART, Tags.Items.CROPS_COCOA_BEAN);
        biofuel(consumer, basePath, "crops", 2, 1, Items.POISONOUS_POTATO);
        biofuel(consumer, basePath, "dried_kelp", 1, 1, Items.DRIED_KELP);
        biofuelBlock(consumer, basePath, "dried_kelp_block", 1, 1, Tags.Items.STORAGE_BLOCKS_DRIED_KELP);
        biofuel(consumer, basePath, "fern", 2, 1, Items.FERN);
        biofuel(consumer, basePath, "fungus", 1, 1, Items.CRIMSON_FUNGUS, Items.WARPED_FUNGUS);
        biofuel(consumer, basePath, "glow_lichen", 1, 1, Items.GLOW_LICHEN);
        biofuel(consumer, basePath, "hanging_roots", 1, 1, Items.HANGING_ROOTS);
        biofuelBlock(consumer, basePath, "hay_block", 2, 1, Tags.Items.STORAGE_BLOCKS_WHEAT);
        biofuel(consumer, basePath, "kelp", 2, 1, Items.KELP);
        biofuel(consumer, basePath, "large_fern", 3, 1, Items.LARGE_FERN);
        biofuel(consumer, basePath, "leaves", 1, 10, ItemTags.LEAVES);
        biofuel(consumer, basePath, "lily_pad", 1, 1, Items.LILY_PAD);
        biofuel(consumer, basePath, "mangrove_roots", 1, 1, Items.MANGROVE_ROOTS);
        biofuel(consumer, basePath, "melon", 6, 1, Tags.Items.CROPS_MELON);
        biofuel(consumer, basePath, "melon_slice", 1, 1, Items.MELON_SLICE);
        biofuel(consumer, basePath, "moss_block", 2, 1, Items.MOSS_BLOCK);
        biofuel(consumer, basePath, "moss_carpet", 4, 3, Items.MOSS_CARPET);
        biofuel(consumer, basePath, "mushroom_blocks", 4, 1, Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM);
        biofuel(consumer, basePath, "mushrooms", 1, 1, Tags.Items.MUSHROOMS);
        biofuel(consumer, basePath, "nether_sprouts", 1, 1, Items.NETHER_SPROUTS);
        biofuelBlock(consumer, basePath, "nether_wart_block", 2, 1, Items.NETHER_WART_BLOCK);
        biofuel(consumer, basePath, "pitcher_pod", 1, 1, Items.PITCHER_POD);
        final int pumpkinOutput = 6;
        biofuel(consumer, basePath, "pumpkin", pumpkinOutput, 1, Tags.Items.CROPS_PUMPKIN);
        biofuel(consumer, basePath, "pumpkin_pie", pumpkinOutput + 1, 1, Items.PUMPKIN_PIE);
        biofuel(consumer, basePath, "rotten_flesh", 1, 1, Items.ROTTEN_FLESH);
        biofuel(consumer, basePath, "saplings", 1, 1, ItemTags.SAPLINGS);
        biofuel(consumer, basePath, "sea_pickle", 1, 1, Items.SEA_PICKLE);
        biofuel(consumer, basePath, "seagrass", 1, 1, Items.SEAGRASS);
        biofuel(consumer, basePath, "seeds", 1, 1, Tags.Items.SEEDS);
        biofuel(consumer, basePath, "short_grass", 1, 1, Items.SHORT_GRASS);
        biofuel(consumer, basePath, "shroomlight", 4, 1, Items.SHROOMLIGHT);
        biofuel(consumer, basePath, "small_dripleaf", 1, 1, Items.SMALL_DRIPLEAF);
        biofuel(consumer, basePath, "small_flowers", 1, 1, ItemTags.SMALL_FLOWERS);
        biofuel(consumer, basePath, "pink_petals", 1, 1, Items.PINK_PETALS);//not in small_flowers tag
        biofuel(consumer, basePath, "spore_blossom", 2, 1, Items.SPORE_BLOSSOM);
        biofuel(consumer, basePath, "sugar_cane", 1, 1, Tags.Items.CROPS_SUGAR_CANE);
        biofuel(consumer, basePath, "tall_flowers", 2, 1, ItemTags.TALL_FLOWERS);
        biofuel(consumer, basePath, "tall_grass", 2, 1, Items.TALL_GRASS);
        biofuel(consumer, basePath, "vines", 1, 1, Items.VINE, Items.TWISTING_VINES, Items.WEEPING_VINES);
        biofuel(consumer, basePath, "warped_roots", 1, 1, Items.WARPED_ROOTS);
        biofuel(consumer, basePath, "warped_wart_block", 4, 1, Items.WARPED_WART_BLOCK);
    }

    private static void biofuel(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, Item... items) {
        biofuel(consumer, basePath, filename, MekanismItems.BIO_FUEL, countOutput, countInput, items);
    }

    private static void biofuelBlock(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, Item... items) {
        biofuel(consumer, basePath, filename, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder(), countOutput, countInput, items);
    }

    private static void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, int countInput, Item... items) {
        biofuel(consumer, basePath, filename, bioFuelType, countOutput, IngredientCreatorAccess.item().from(countInput, items));
    }

    @SafeVarargs
    private static void biofuelBlock(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, TagKey<Item>... tags) {
        biofuel(consumer, basePath, filename, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder(), countOutput, countInput, tags);
    }

    @SafeVarargs
    private static void biofuel(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, TagKey<Item>... tags) {
        biofuel(consumer, basePath, filename, MekanismItems.BIO_FUEL, countOutput, countInput, tags);
    }

    @SafeVarargs
    private static void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, int countInput, TagKey<Item>... tags) {
        biofuel(consumer, basePath, filename, bioFuelType, countOutput, IngredientCreatorAccess.item().from(countInput, Arrays.asList(tags)));
    }

    private static void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, ItemStackIngredient input) {
        ItemStackToItemStackRecipeBuilder.crushing(
              input,
              new ItemStack(bioFuelType, countOutput)
        ).build(consumer, Mekanism.rl(basePath + filename));
    }
}