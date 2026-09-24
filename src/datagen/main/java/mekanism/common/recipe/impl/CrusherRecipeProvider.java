package mekanism.common.recipe.impl;

import java.util.Arrays;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class CrusherRecipeProvider extends BaseSubRecipeProvider {

    CrusherRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "crushing/";
        addCrusherBioFuelRecipes(consumer, basePath + "biofuel/");
        addCrusherDewaxingRecipes(consumer, basePath + "dewax/");
        addCrusherStoneRecipes(consumer, basePath + "stone/");
        addCrusherCinnabarRecipes(consumer, basePath + "cinnabar/");
        addCrusherSulfurRecipes(consumer, basePath + "sulfur/");
        addCrusherTuffRecipes(consumer, basePath + "tuff/");
        addCrusherDeepslateRecipes(consumer, basePath + "deepslate/");
        addCrusherBlackstoneRecipes(consumer, basePath + "blackstone/");
        addCrusherQuartzRecipes(consumer, basePath + "quartz/");
        addCrusherGraniteRecipes(consumer, basePath + "granite/");
        addCrusherDioriteRecipes(consumer, basePath + "diorite/");
        addCrusherAndesiteRecipes(consumer, basePath + "andesite/");
        addCrusherPrismarineRecipes(consumer, basePath + "prismarine/");
        addCrusherResinRecipes(consumer, basePath + "resin/");
        addCrusherStringRecipes(consumer, basePath + "string/");
        //Dripstone Block -> Pointed Dripstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DRIPSTONE_BLOCK),
              template(BlockItemIds.POINTED_DRIPSTONE, 4)
        ).save(consumer, Mekanism.rl(basePath + "pointed_dripstone_from_block"));
        //Honecomb Block -> Honeycomb
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.HONEYCOMB_BLOCK),
              template(ItemIds.HONEYCOMB, 4)
        ).save(consumer, Mekanism.rl(basePath + "honeycomb_from_block"));
        //Purpur Block -> Purpur Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PURPUR_PILLAR),
              template(BlockItemIds.PURPUR_BLOCK)
        ).save(consumer, Mekanism.rl(basePath + "purpur_block_from_pillar"));
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.COBBLESTONES_NORMAL),
              template(BlockItemIds.GRAVEL)
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.FLINT),
              template(ItemIds.GUNPOWDER)
        ).save(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GRAVELS),
              template(BlockItemIds.SAND)
        ).save(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //Mud bricks -> packed mud
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD_BRICKS),
              template(BlockItemIds.PACKED_MUD)
        ).save(consumer, Mekanism.rl(basePath + "mud_bricks_to_packed"));
        //Break music disc 5
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.MUSIC_DISC_5),
              template(ItemIds.DISC_FRAGMENT_5, 9)
        ).save(consumer, Mekanism.rl(basePath + "break_disc_5"));
        //Obsidian -> obsidian dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.OBSIDIANS_NORMAL),
              MekanismItems.OBSIDIAN_DUST.asTemplate(4)
        ).save(consumer, Mekanism.rl(basePath + "obsidian_to_dust"));
        //Blaze Rod -> blaze powder
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RODS_BLAZE),
              template(ItemIds.BLAZE_POWDER, 4)
        ).save(consumer, Mekanism.rl(basePath + "blaze_rod"));
        //Breeze Rod -> wind charge
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RODS_BREEZE),
              template(ItemIds.WIND_CHARGE, 6)
        ).save(consumer, Mekanism.rl(basePath + "breeze_rod"));
        //Bone -> bone meal
        final int BONEMEAL_FROM_BONE = 6;
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.BONE),
              template(ItemIds.BONE_MEAL, BONEMEAL_FROM_BONE)
        ).save(consumer, Mekanism.rl(basePath + "bone"));
        //Bone block -> bone meal
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BONE_BLOCK),
              template(ItemIds.BONE_MEAL, 9)//must be the same as vanilla needs to make a block
        ).save(consumer, Mekanism.rl(basePath + "bone_block"));
        //Red Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, this.items, basePath + "red_sandstone_to_sand", null, BlockItemIds.RED_SAND, Tags.Items.SANDSTONE_RED_BLOCKS);
        //Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, this.items, basePath + "sandstone_to_sand", null, BlockItemIds.SAND, Tags.Items.SANDSTONE_UNCOLORED_BLOCKS);
        //Soul Soil -> Soul Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SOUL_SOIL),
              template(BlockItemIds.SOUL_SAND)
        ).save(consumer, Mekanism.rl(basePath + "soul_soil_to_soul_sand"));
        //Polished or Smooth Basalt -> Basalt
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items,
                    BlockItemIds.POLISHED_BASALT,
                    BlockItemIds.SMOOTH_BASALT
              ),
              template(BlockItemIds.BASALT)
        ).save(consumer, Mekanism.rl(basePath + "polished_or_smooth_basalt_to_basalt"));
        //Chiseled Nether Bricks -> Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_NETHER_BRICKS),
              template(BlockItemIds.NETHER_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_nether_bricks_to_nether_bricks"));
        //Nether Bricks -> Cracked Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.NETHER_BRICKS),
              template(BlockItemIds.CRACKED_NETHER_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "nether_bricks_to_cracked_nether_bricks"));

        //Dried Ghast -> Ghast Tears
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DRIED_GHAST),
              template(ItemIds.GHAST_TEAR, 3)
        ).save(consumer, Mekanism.rl(basePath + "dried_ghast"));
    }

    private void addCrusherStringRecipes(RecipeOutput consumer, String basePath) {
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOOL),
              template(BlockItemIds.TRIPWIRE, 4)
        ).save(consumer, Mekanism.rl(basePath + "from_wool"));
        //Wool Slabs -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOOL_SLABS),
              template(BlockItemIds.TRIPWIRE, 2)
        ).save(consumer, Mekanism.rl(basePath + "from_wool_slabs"));
        //Wool Stairs -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.WOOL_STAIRS),
              template(BlockItemIds.TRIPWIRE, 6)
        ).save(consumer, Mekanism.rl(basePath + "from_wool_stairs"));
        //Cushions -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemTags.CUSHIONS),
              template(BlockItemIds.TRIPWIRE, 6)
        ).save(consumer, Mekanism.rl(basePath + "from_cushions"));
    }

    private void addCrusherStoneRecipes(RecipeOutput consumer, String basePath) {
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE),
              template(BlockItemIds.COBBLESTONE)
        ).save(consumer, Mekanism.rl(basePath + "to_cobblestone"));
        //Stone Stairs -> Cobblestone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_STAIRS),
              template(BlockItemIds.COBBLESTONE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_cobblestone_stairs"));
        //Stone Slabs -> Cobblestone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_SLAB),
              template(BlockItemIds.COBBLESTONE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slabs_to_cobblestone_slabs"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_STONE_BRICKS),
              template(BlockItemIds.STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICKS),
              template(BlockItemIds.CRACKED_STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_STONE_BRICKS),
              template(BlockItemIds.STONE)
        ).save(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherCinnabarRecipes(RecipeOutput consumer, String basePath) {
        //Polished Cinnabar -> Cinnabar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR),
              template(BlockItemIds.CINNABAR)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Cinnabar Stairs -> Cinnabar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_STAIRS),
              template(BlockItemIds.CINNABAR_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished"));
        //Polished Cinnabar Slabs -> Cinnabar Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_SLAB),
              template(BlockItemIds.CINNABAR_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slabs_from_polished"));
        //Polished Cinnabar Walls -> Cinnabar Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_WALL),
              template(BlockItemIds.CINNABAR_WALL)
        ).save(consumer, Mekanism.rl(basePath + "wall_from_polished"));

        //Cinnabar Bricks -> Polished Cinnabar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_BRICKS),
              template(BlockItemIds.POLISHED_CINNABAR)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_polished"));
        //Cinnabar Brick Stairs -> Polished Cinnabar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_BRICK_STAIRS),
              template(BlockItemIds.POLISHED_CINNABAR_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "brick_stairs_to_polished"));
        //Cinnabar Brick Slabs -> Polished Cinnabar Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_BRICK_SLAB),
              template(BlockItemIds.POLISHED_CINNABAR_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "brick_slabs_to_polished"));
        //Cinnabar Brick Walls -> Polished Cinnabar Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_BRICK_WALL),
              template(BlockItemIds.POLISHED_CINNABAR_WALL)
        ).save(consumer, Mekanism.rl(basePath + "brick_wall_to_polished"));

        //Chiseled Cinnabar -> Cinnabar Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_CINNABAR),
              template(BlockItemIds.CINNABAR_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_to_brick"));
    }

    private void addCrusherSulfurRecipes(RecipeOutput consumer, String basePath) {
        int spikeRate = 1;
        int blockRate = 4 * spikeRate;
        int potentRate = 9 * blockRate;
        //Sulfur Spikes -> Sulfur Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_SPIKE),
              MekanismItems.SULFUR_DUST.asTemplate(spikeRate)
        ).save(consumer, Mekanism.rl(basePath + "spike_to_dust"));
        //Sulfur -> Sulfur Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR),
              MekanismItems.SULFUR_DUST.asTemplate(blockRate)
        ).save(consumer, Mekanism.rl(basePath + "to_dust"));
        //Potent Sulfur -> Sulfur Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POTENT_SULFUR),
              MekanismItems.SULFUR_DUST.asTemplate(potentRate)
        ).save(consumer, Mekanism.rl(basePath + "potent_to_dust"));

        //Polished Sulfur -> Sulfur
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR),
              template(BlockItemIds.SULFUR)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Sulfur Stairs -> Sulfur Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_STAIRS),
              template(BlockItemIds.SULFUR_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished"));
        //Polished Sulfur Slabs -> Sulfur Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_SLAB),
              template(BlockItemIds.SULFUR_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slabs_from_polished"));
        //Polished Sulfur Walls -> Sulfur Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_WALL),
              template(BlockItemIds.SULFUR_WALL)
        ).save(consumer, Mekanism.rl(basePath + "wall_from_polished"));

        //Sulfur Bricks -> Polished Sulfur
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_BRICKS),
              template(BlockItemIds.POLISHED_SULFUR)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_polished"));
        //Sulfur Brick Stairs -> Polished Sulfur Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_BRICK_STAIRS),
              template(BlockItemIds.POLISHED_SULFUR_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "brick_stairs_to_polished"));
        //Sulfur Brick Slabs -> Polished Sulfur Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_BRICK_SLAB),
              template(BlockItemIds.POLISHED_SULFUR_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "brick_slabs_to_polished"));
        //Sulfur Brick Walls -> Polished Sulfur Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_BRICK_WALL),
              template(BlockItemIds.POLISHED_SULFUR_WALL)
        ).save(consumer, Mekanism.rl(basePath + "brick_wall_to_polished"));

        //Chiseled Sulfur -> Sulfur Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_SULFUR),
              template(BlockItemIds.SULFUR_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_to_brick"));
    }

    private void addCrusherTuffRecipes(RecipeOutput consumer, String basePath) {
        //Polished Tuff -> Tuff
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF),
              template(BlockItemIds.TUFF)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Tuff Stairs -> Tuff Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_STAIRS),
              template(BlockItemIds.TUFF_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished"));
        //Polished Tuff Slabs -> Tuff Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_SLAB),
              template(BlockItemIds.TUFF_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slabs_from_polished"));
        //Polished Tuff Walls -> Tuff Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_WALL),
              template(BlockItemIds.TUFF_WALL)
        ).save(consumer, Mekanism.rl(basePath + "wall_from_polished"));

        //Tuff Bricks -> Polished Tuff
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICKS),
              template(BlockItemIds.POLISHED_TUFF)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_polished"));
        //Tuff Brick Stairs -> Polished Tuff Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_STAIRS),
              template(BlockItemIds.POLISHED_TUFF_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "brick_stairs_to_polished"));
        //Tuff Brick Slabs -> Polished Tuff Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_SLAB),
              template(BlockItemIds.POLISHED_TUFF_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "brick_slabs_to_polished"));
        //Tuff Brick Walls -> Polished Tuff Walls
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_WALL),
              template(BlockItemIds.POLISHED_TUFF_WALL)
        ).save(consumer, Mekanism.rl(basePath + "brick_wall_to_polished"));

        //Chiseled Tuff -> Tuff Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_TUFF),
              template(BlockItemIds.TUFF_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_to_brick"));
    }

    private void addCrusherDeepslateRecipes(RecipeOutput consumer, String basePath) {
        //Deepslate -> Cobbled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE),
              template(BlockItemIds.COBBLED_DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "to_cobbled"));

        //Polished Deepslate -> Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "polished_to_bricks"));
        //Polished Deepslate Stairs -> Deepslate Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_STAIRS),
              template(BlockItemIds.DEEPSLATE_BRICK_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "polished_stairs_to_brick"));
        //Polished Deepslate Slabs -> Deepslate Brick Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_SLAB),
              template(BlockItemIds.DEEPSLATE_BRICK_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "polished_slabs_to_brick"));
        //Polished Deepslate Wall -> Deepslate Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_WALL),
              template(BlockItemIds.DEEPSLATE_BRICK_WALL)
        ).save(consumer, Mekanism.rl(basePath + "polished_wall_to_brick"));

        //Deepslate Bricks -> Cracked Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_BRICKS),
              template(BlockItemIds.CRACKED_DEEPSLATE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Deepslate Bricks -> Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_BRICKS),
              template(BlockItemIds.DEEPSLATE_TILES)
        ).save(consumer, Mekanism.rl(basePath + "cracked_bricks_to_tile"));

        //Deepslate Brick Stairs -> Deepslate Tile Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_BRICK_STAIRS),
              template(BlockItemIds.DEEPSLATE_TILE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "brick_stairs_to_tile"));
        //Deepslate Brick Slabs -> Deepslate Tile Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_BRICK_SLAB),
              template(BlockItemIds.DEEPSLATE_TILE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "brick_slabs_to_tile"));
        //Deepslate Brick Wall -> Deepslate Tile Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_BRICK_WALL),
              template(BlockItemIds.DEEPSLATE_TILE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "brick_wall_to_tile"));

        //Deepslate Tiles -> Cracked Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILES),
              template(BlockItemIds.CRACKED_DEEPSLATE_TILES)
        ).save(consumer, Mekanism.rl(basePath + "tile_to_cracked_tile"));
        //Cracked Deepslate Tiles -> Chiseled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_TILES),
              template(BlockItemIds.CHISELED_DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "cracked_tile_to_chiseled"));

        //Deepslate Tile Stairs -> Cobbled Deepslate Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_STAIRS),
              template(BlockItemIds.COBBLED_DEEPSLATE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "tile_stairs_to_cobbled"));
        //Deepslate Tile Slabs -> Cobbled Deepslate Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_SLAB),
              template(BlockItemIds.COBBLED_DEEPSLATE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "tile_slabs_to_cobbled"));
        //Deepslate Tile Wall -> Cobbled Deepslate Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_WALL),
              template(BlockItemIds.COBBLED_DEEPSLATE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "tile_wall_to_cobbled"));

        //Chiseled Deepslate -> Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "from_chiseled"));
    }

    private void addCrusherBlackstoneRecipes(RecipeOutput consumer, String basePath) {
        //Polished Blackstone -> Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE),
              template(BlockItemIds.BLACKSTONE)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Blackstone Wall -> Blackstone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_WALL),
              template(BlockItemIds.BLACKSTONE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "polished_wall_to_wall"));
        //Polished Blackstone Stairs -> Blackstone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_STAIRS),
              template(BlockItemIds.BLACKSTONE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "polished_stairs_to_stairs"));
        //Polished Blackstone Slabs -> Blackstone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_SLAB),
              template(BlockItemIds.BLACKSTONE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "polished_slabs_to_slabs"));
        //Chiseled Polished Blackstone Bricks -> Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_POLISHED_BLACKSTONE),
              template(BlockItemIds.POLISHED_BLACKSTONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Polished Blackstone Bricks -> Cracked Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_BRICKS),
              template(BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Polished Blackstone Bricks -> Polished Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              template(BlockItemIds.POLISHED_BLACKSTONE)
        ).save(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherQuartzRecipes(RecipeOutput consumer, String basePath) {
        //Quartz Block -> Smooth Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.QUARTZ_BLOCK),
              template(BlockItemIds.SMOOTH_QUARTZ)
        ).save(consumer, Mekanism.rl(basePath + "to_smooth_quartz"));
        //Quartz Slab -> Smooth Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.QUARTZ_SLAB),
              template(BlockItemIds.SMOOTH_QUARTZ_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Quartz Stairs -> Smooth Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.QUARTZ_STAIRS),
              template(BlockItemIds.SMOOTH_QUARTZ_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Smooth Quartz Block -> Quartz Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ),
              template(BlockItemIds.QUARTZ_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "smooth_to_bricks"));
        //Quartz Bricks -> Chiseled Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.QUARTZ_BRICKS),
              template(BlockItemIds.CHISELED_QUARTZ_BLOCK)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_chiseled"));
        //Chiseled Quartz Block -> Quartz Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_QUARTZ_BLOCK),
              template(BlockItemIds.QUARTZ_PILLAR)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_to_pillar"));
        //Quartz Pillar -> Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.QUARTZ_PILLAR),
              template(BlockItemIds.QUARTZ_BLOCK)
        ).save(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherGraniteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Granite -> Granite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_GRANITE),
              template(BlockItemIds.GRANITE)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Granite Stairs -> Granite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_GRANITE_STAIRS),
              template(BlockItemIds.GRANITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Granite Slab -> Granite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_GRANITE_SLAB),
              template(BlockItemIds.GRANITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherDioriteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Diorite -> Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DIORITE),
              template(BlockItemIds.DIORITE)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Diorite Stairs -> Granite Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DIORITE_STAIRS),
              template(BlockItemIds.DIORITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Diorite Slab -> Diorite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DIORITE_SLAB),
              template(BlockItemIds.DIORITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherAndesiteRecipes(RecipeOutput consumer, String basePath) {
        //Polished Andesite -> Andesite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_ANDESITE),
              template(BlockItemIds.ANDESITE)
        ).save(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Andesite Stairs -> Andesite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_ANDESITE_STAIRS),
              template(BlockItemIds.ANDESITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Andesite Slab -> Andesite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_ANDESITE_SLAB),
              template(BlockItemIds.ANDESITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherPrismarineRecipes(RecipeOutput consumer, String basePath) {
        //Prismarine -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE),
              template(ItemIds.PRISMARINE_SHARD, 4)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_block"));
        //Prismarine Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_SLAB),
              template(ItemIds.PRISMARINE_SHARD, 2)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_slabs"));
        //Prismarine Stairs -> Prismarine Shards
        // Note: Uses 1 -> 4 as he stone cutter allows for one prismarine block to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_STAIRS),
              template(ItemIds.PRISMARINE_SHARD, 4)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_stairs"));
        //Prismarine Wall -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_WALL),
              template(ItemIds.PRISMARINE_SHARD, 4)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_wall"));
        //Prismarine Brick -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_BRICKS),
              template(ItemIds.PRISMARINE_SHARD, 9)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_brick"));
        //Prismarine Brick Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_BRICK_SLAB, 2),
              template(ItemIds.PRISMARINE_SHARD, 9)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_brick_slabs"));
        //Prismarine Brick Stairs -> Prismarine Shards
        // Note: Uses 1 -> 9 as the stone cutter allows for one brick to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PRISMARINE_BRICK_STAIRS),
              template(ItemIds.PRISMARINE_SHARD, 9)
        ).save(consumer, Mekanism.rl(basePath + "shard_from_brick_stairs"));
    }

    private void addCrusherResinRecipes(RecipeOutput consumer, String basePath) {
        //Block of Resin -> Resin clumps
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.RESIN_BLOCK),
              template(BlockItemIds.RESIN_CLUMP, 9)
        ).save(consumer, Mekanism.rl(basePath + "clump_from_block"));
        //Resin Bricks -> Resin Blocks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.RESIN_BRICKS),
              template(BlockItemIds.RESIN_BLOCK)
        ).save(consumer, Mekanism.rl(basePath + "resin_bricks_to_block"));
        //Chiseled Resin Bricks -> Resin Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_RESIN_BRICKS),
              template(BlockItemIds.RESIN_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_resin_bricks_to_bricks"));
    }

    private void addCrusherDewaxingRecipes(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from de-waxing recipe set
        for (Map.Entry<Block, Block> entry : HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackToItemStackRecipeBuilder.crushing(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  new ItemStackTemplate(result.asItem())
            ).save(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }

    private void addCrusherBioFuelRecipes(RecipeOutput consumer, String basePath) {
        biofuel(consumer, basePath, "apple", 2, 1, ItemIds.APPLE);
        biofuel(consumer, basePath, "baked_potato", 2, 1, ItemIds.BAKED_POTATO);
        biofuel(consumer, basePath, "bamboo", 2, 1, BlockItemIds.BAMBOO);
        biofuel(consumer, basePath, "berries", 1, 1, Tags.Items.FOODS_BERRY);
        biofuel(consumer, basePath, "big_dripleaf", 2, 1, BlockItemIds.BIG_DRIPLEAF);
        biofuel(consumer, basePath, "bread", 4, 1, ItemIds.BREAD);
        biofuel(consumer, basePath, "bush", 1, 1, BlockItemIds.BUSH, BlockItemIds.RED_SHRUB);
        biofuel(consumer, basePath, "cactus_flower", 2, 1, BlockItemIds.CACTUS_FLOWER);
        biofuel(consumer, basePath, "cake", 6, 1, BlockItemIds.CAKE);
        biofuel(consumer, basePath, "carved_pumpkin", 2, 1, BlockItemIds.CARVED_PUMPKIN);//pumpkin minus 4 seeds (sawmill)
        biofuel(consumer, basePath, "crimson_roots", 1, 1, BlockItemIds.CRIMSON_ROOTS);
        biofuel(consumer, basePath, "cookie", 3, 4, ItemIds.COOKIE);
        //TODO - 1.21.1: Make our own tag that contains all of these
        biofuel(consumer, basePath, "crops_tags", 2, 1, Tags.Items.CROPS_CARROT, Tags.Items.CROPS_CACTUS, Tags.Items.CROPS_POTATO,
              Tags.Items.CROPS_WHEAT, Tags.Items.CROPS_BEETROOT, Tags.Items.CROPS_NETHER_WART, Tags.Items.CROPS_COCOA_BEAN);
        biofuel(consumer, basePath, "crops", 2, 1, ItemIds.POISONOUS_POTATO);
        biofuel(consumer, basePath, "dried_kelp", 1, 1, ItemIds.DRIED_KELP);
        biofuelBlock(consumer, basePath, "dried_kelp_block", 1, 1, Tags.Items.STORAGE_BLOCKS_DRIED_KELP);
        biofuel(consumer, basePath, "fern", 2, 1, BlockItemIds.FERN);
        biofuel(consumer, basePath, "firefly_bush", 1, 1, BlockItemIds.FIREFLY_BUSH);
        biofuel(consumer, basePath, "fungus", 1, 1, BlockItemIds.CRIMSON_FUNGUS, BlockItemIds.WARPED_FUNGUS);
        biofuel(consumer, basePath, "glow_lichen", 1, 1, BlockItemIds.GLOW_LICHEN);
        biofuel(consumer, basePath, "hanging_roots", 1, 1, BlockItemIds.HANGING_ROOTS);
        biofuelBlock(consumer, basePath, "hay_block", 2, 1, Tags.Items.STORAGE_BLOCKS_WHEAT);
        biofuelBlock(consumer, basePath, "straw_bed", 3, 2, BlockItemIds.STRAW_BED);
        biofuel(consumer, basePath, "kelp", 2, 1, BlockItemIds.KELP);
        biofuel(consumer, basePath, "large_fern", 3, 1, BlockItemIds.LARGE_FERN);
        //1:1 with leaves because they can be produced by smelting leaves
        biofuel(consumer, basePath, "leaf_litter", 1, 10, BlockItemIds.LEAF_LITTER);
        biofuel(consumer, basePath, "leaves", 1, 10, ItemTags.LEAVES);
        biofuel(consumer, basePath, "lily_pad", 1, 1, BlockItemIds.LILY_PAD);
        biofuel(consumer, basePath, "mangrove_roots", 1, 1, BlockItemIds.MANGROVE_ROOTS);
        biofuel(consumer, basePath, "melon", 6, 1, Tags.Items.CROPS_MELON);
        biofuel(consumer, basePath, "melon_slice", 1, 1, ItemIds.MELON_SLICE);
        biofuel(consumer, basePath, "moss_block", 2, 1, BlockItemTags.MOSS_BLOCKS.item());
        biofuel(consumer, basePath, "moss_carpet", 4, 3, BlockItemIds.MOSS_CARPET, BlockItemIds.PALE_MOSS_CARPET);
        biofuel(consumer, basePath, "mushroom_blocks", 4, 1, BlockItemIds.BROWN_MUSHROOM_BLOCK, BlockItemIds.RED_MUSHROOM_BLOCK, BlockItemIds.MUSHROOM_STEM);
        biofuel(consumer, basePath, "mushrooms", 1, 1, Tags.Items.MUSHROOMS);
        biofuel(consumer, basePath, "nether_sprouts", 1, 1, BlockItemIds.NETHER_SPROUTS);
        biofuelBlock(consumer, basePath, "nether_wart_block", 2, 1, BlockItemIds.NETHER_WART_BLOCK);
        biofuel(consumer, basePath, "pale_hanging_moss", 1, 1, BlockItemIds.PALE_HANGING_MOSS);
        biofuel(consumer, basePath, "pitcher_pod", 1, 1, BlockItemIds.PITCHER_CROP);
        final int pumpkinOutput = 6;
        biofuel(consumer, basePath, "pumpkin", pumpkinOutput, 1, Tags.Items.CROPS_PUMPKIN);
        biofuel(consumer, basePath, "pumpkin_pie", pumpkinOutput + 1, 1, ItemIds.PUMPKIN_PIE);
        biofuel(consumer, basePath, "rotten_flesh", 1, 1, ItemIds.ROTTEN_FLESH);
        biofuel(consumer, basePath, "saplings", 1, 1, ItemTags.SAPLINGS);
        biofuel(consumer, basePath, "sea_pickle", 1, 1, BlockItemIds.SEA_PICKLE);
        biofuel(consumer, basePath, "seagrass", 1, 1, BlockItemIds.SEAGRASS);
        biofuel(consumer, basePath, "seeds", 1, 1, Tags.Items.SEEDS);
        biofuel(consumer, basePath, "short_grass", 1, 1, BlockItemIds.SHORT_DRY_GRASS, BlockItemIds.SHORT_GRASS);
        biofuel(consumer, basePath, "shroomlight", 4, 1, BlockItemIds.SHROOMLIGHT);
        biofuel(consumer, basePath, "small_dripleaf", 1, 1, BlockItemIds.SMALL_DRIPLEAF);
        biofuel(consumer, basePath, "small_flowers", 1, 1, BlockItemTags.SMALL_FLOWERS.item());
        biofuel(consumer, basePath, "pink_petals", 1, 1, BlockItemIds.PINK_PETALS);//not in small_flowers tag
        biofuel(consumer, basePath, "spore_blossom", 2, 1, BlockItemIds.SPORE_BLOSSOM);
        biofuel(consumer, basePath, "sugar_cane", 1, 1, Tags.Items.CROPS_SUGAR_CANE);
        biofuel(consumer, basePath, "tall_flowers", 2, 1, Tags.Items.FLOWERS_TALL);
        biofuel(consumer, basePath, "tall_grass", 2, 1, BlockItemIds.TALL_DRY_GRASS, BlockItemIds.TALL_GRASS);
        biofuel(consumer, basePath, "vines", 1, 1, BlockItemIds.VINE, BlockItemIds.TWISTING_VINES, BlockItemIds.WEEPING_VINES);
        biofuel(consumer, basePath, "warped_roots", 1, 1, BlockItemIds.WARPED_ROOTS);
        biofuel(consumer, basePath, "warped_wart_block", 4, 1, BlockItemIds.WARPED_WART_BLOCK);
        biofuel(consumer, basePath, "wildflowers", 1, 1, BlockItemIds.WILDFLOWERS);//not in small_flowers tag
    }

    @SafeVarargs
    private void biofuel(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, ResourceKey<Item>... items) {
        biofuel(consumer, basePath, filename, MekanismItems.BIO_FUEL, countOutput, countInput, items);
    }

    @SafeVarargs
    private void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, int countInput, ResourceKey<Item>... items) {
        biofuel(consumer, basePath, filename, bioFuelType, countOutput, IngredientCreatorAccess.item().from(this.items, countInput, items));
    }

    private void biofuel(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, BlockItemId... items) {
        biofuel(consumer, basePath, filename, MekanismItems.BIO_FUEL, countOutput, countInput, items);
    }

    private void biofuelBlock(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, BlockItemId... items) {
        biofuel(consumer, basePath, filename, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder(), countOutput, countInput, items);
    }

    private void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, int countInput, BlockItemId... items) {
        biofuel(consumer, basePath, filename, bioFuelType, countOutput, IngredientCreatorAccess.item().from(this.items, countInput, items));
    }

    @SafeVarargs
    private void biofuelBlock(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, TagKey<Item>... tag) {
        biofuel(consumer, basePath, filename, MekanismBlocks.BIO_FUEL_BLOCK.getItemHolder(), countOutput, countInput, tag);
    }

    @SafeVarargs
    private void biofuel(RecipeOutput consumer, String basePath, String filename, int countOutput, int countInput, TagKey<Item>... tag) {
        biofuel(consumer, basePath, filename, MekanismItems.BIO_FUEL, countOutput, countInput, tag);
    }

    @SafeVarargs
    private void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, int countInput, TagKey<Item>... tag) {
        biofuel(consumer, basePath, filename, bioFuelType, countOutput, IngredientCreatorAccess.item().from(this.items, countInput, Arrays.asList(tag)));
    }

    private void biofuel(RecipeOutput consumer, String basePath, String filename, Holder<Item> bioFuelType, int countOutput, ItemStackIngredient input) {
        ItemStackToItemStackRecipeBuilder.crushing(
              input,
              new ItemStackTemplate(bioFuelType, countOutput)
        ).save(consumer, Mekanism.rl(basePath + filename));
    }
}