package mekanism.common.recipe.impl;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.Tags;

class CrusherRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "crushing/";
        addCrusherBioFuelRecipes(consumer, basePath + "biofuel/");
        addCrusherDewaxingRecipes(consumer, basePath + "dewax/");
        addCrusherStoneRecipes(consumer, basePath + "stone/");
        addCrusherDeepslateRecipes(consumer, basePath + "deepslate/");
        addCrusherBlackstoneRecipes(consumer, basePath + "blackstone/");
        addCrusherQuartzRecipes(consumer, basePath + "quartz/");
        addCrusherGraniteRecipes(consumer, basePath + "granite/");
        addCrusherDioriteRecipes(consumer, basePath + "diorite/");
        addCrusherAndesiteRecipes(consumer, basePath + "andesite/");
        addCrusherPrismarineRecipes(consumer, basePath + "prismarine/");
        //Dripstone Block -> Pointed Dripstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DRIPSTONE_BLOCK),
              new ItemStack(Items.POINTED_DRIPSTONE, 4)
        ).build(consumer, Mekanism.rl(basePath + "pointed_dripstone_from_block"));
        //Purpur Block -> Purpur Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PURPUR_PILLAR),
              new ItemStack(Blocks.PURPUR_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "purpur_block_from_pillar"));
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_NORMAL),
              new ItemStack(Blocks.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.FLINT),
              new ItemStack(Items.GUNPOWDER)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.GRAVEL),
              new ItemStack(Blocks.SAND)
        ).build(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //TODO: Do we just want to make a clear and red tag for sandstone?
        //Red Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "red_sandstone_to_sand", null, Blocks.RED_SAND, Blocks.RED_SANDSTONE,
              Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);
        //Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "sandstone_to_sand", null, Blocks.SAND, Blocks.SANDSTONE,
              Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).build(consumer, Mekanism.rl(basePath + "wool_to_string"));
        //Soul Soil -> Soul Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.SOUL_SOIL),
              new ItemStack(Blocks.SOUL_SAND)
        ).build(consumer, Mekanism.rl(basePath + "soul_soil_to_soul_sand"));
        //Polished or Smooth Basalt -> Basalt
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Ingredient.of(
                    Blocks.POLISHED_BASALT,
                    Blocks.SMOOTH_BASALT
              )),
              new ItemStack(Blocks.BASALT)
        ).build(consumer, Mekanism.rl(basePath + "polished_or_smooth_basalt_to_basalt"));
        //Chiseled Nether Bricks -> Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_NETHER_BRICKS),
              new ItemStack(Blocks.NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_nether_bricks_to_nether_bricks"));
        //Nether Bricks -> Cracked Nether Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.NETHER_BRICKS),
              new ItemStack(Blocks.CRACKED_NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "nether_bricks_to_cracked_nether_bricks"));
    }

    private void addCrusherStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.STONE),
              new ItemStack(Blocks.COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "to_cobblestone"));
        //Stone Stairs -> Cobblestone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.STONE_STAIRS),
              new ItemStack(Blocks.COBBLESTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_cobblestone_stairs"));
        //Stone Slabs -> Cobblestone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.STONE_SLAB),
              new ItemStack(Blocks.COBBLESTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slabs_to_cobblestone_slabs"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_STONE_BRICKS),
              new ItemStack(Blocks.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.STONE_BRICKS),
              new ItemStack(Blocks.CRACKED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CRACKED_STONE_BRICKS),
              new ItemStack(Blocks.STONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherDeepslateRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Deepslate -> Cobbled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE),
              new ItemStack(Blocks.COBBLED_DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "to_cobbled"));

        //Polished Deepslate -> Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DEEPSLATE),
              new ItemStack(Blocks.DEEPSLATE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "polished_to_bricks"));
        //Polished Deepslate Stairs -> Deepslate Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DEEPSLATE_STAIRS),
              new ItemStack(Blocks.DEEPSLATE_BRICK_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_brick"));
        //Polished Deepslate Slabs -> Deepslate Brick Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DEEPSLATE_SLAB),
              new ItemStack(Blocks.DEEPSLATE_BRICK_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_brick"));
        //Polished Deepslate Wall -> Deepslate Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DEEPSLATE_WALL),
              new ItemStack(Blocks.DEEPSLATE_BRICK_WALL)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_brick"));

        //Deepslate Bricks -> Cracked Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_BRICKS),
              new ItemStack(Blocks.CRACKED_DEEPSLATE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Deepslate Bricks -> Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CRACKED_DEEPSLATE_BRICKS),
              new ItemStack(Blocks.DEEPSLATE_TILES)
        ).build(consumer, Mekanism.rl(basePath + "cracked_bricks_to_tile"));

        //Deepslate Brick Stairs -> Deepslate Tile Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_BRICK_STAIRS),
              new ItemStack(Blocks.DEEPSLATE_TILE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "brick_stairs_to_tile"));
        //Deepslate Brick Slabs -> Deepslate Tile Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_BRICK_SLAB),
              new ItemStack(Blocks.DEEPSLATE_TILE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "brick_slabs_to_tile"));
        //Deepslate Brick Wall -> Deepslate Tile Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_BRICK_WALL),
              new ItemStack(Blocks.DEEPSLATE_TILE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "brick_wall_to_tile"));

        //Deepslate Tiles -> Cracked Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_TILES),
              new ItemStack(Blocks.CRACKED_DEEPSLATE_TILES)
        ).build(consumer, Mekanism.rl(basePath + "tile_to_cracked_tile"));
        //Cracked Deepslate Tiles -> Chiseled Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CRACKED_DEEPSLATE_TILES),
              new ItemStack(Blocks.CHISELED_DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "cracked_tile_to_chiseled"));

        //Deepslate Tile Stairs -> Cobbled Deepslate Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_TILE_STAIRS),
              new ItemStack(Blocks.COBBLED_DEEPSLATE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "tile_stairs_to_cobbled"));
        //Deepslate Tile Slabs -> Cobbled Deepslate Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_TILE_SLAB),
              new ItemStack(Blocks.COBBLED_DEEPSLATE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "tile_slabs_to_cobbled"));
        //Deepslate Tile Wall -> Cobbled Deepslate Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.DEEPSLATE_TILE_WALL),
              new ItemStack(Blocks.COBBLED_DEEPSLATE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "tile_wall_to_cobbled"));

        //Chiseled Deepslate -> Deepslate
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_DEEPSLATE),
              new ItemStack(Blocks.DEEPSLATE)
        ).build(consumer, Mekanism.rl(basePath + "from_chiseled"));
    }

    private void addCrusherBlackstoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Polished Blackstone -> Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_BLACKSTONE),
              new ItemStack(Blocks.BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Blackstone Wall -> Blackstone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_BLACKSTONE_WALL),
              new ItemStack(Blocks.BLACKSTONE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_wall"));
        //Polished Blackstone Stairs -> Blackstone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_BLACKSTONE_STAIRS),
              new ItemStack(Blocks.BLACKSTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_stairs"));
        //Polished Blackstone Slabs -> Blackstone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_BLACKSTONE_SLAB),
              new ItemStack(Blocks.BLACKSTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_slabs"));
        //Chiseled Polished Blackstone Bricks -> Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_POLISHED_BLACKSTONE),
              new ItemStack(Blocks.POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Polished Blackstone Bricks -> Cracked Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Polished Blackstone Bricks -> Polished Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.POLISHED_BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherQuartzRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Quartz Block -> Smooth Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.STORAGE_BLOCKS_QUARTZ),
              new ItemStack(Blocks.SMOOTH_QUARTZ)
        ).build(consumer, Mekanism.rl(basePath + "to_smooth_quartz"));
        //Quartz Slab -> Smooth Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.QUARTZ_SLAB),
              new ItemStack(Blocks.SMOOTH_QUARTZ_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Quartz Stairs -> Smooth Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.QUARTZ_STAIRS),
              new ItemStack(Blocks.SMOOTH_QUARTZ_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Smooth Quartz Block -> Quartz Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.SMOOTH_QUARTZ),
              new ItemStack(Blocks.QUARTZ_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "smooth_to_bricks"));
        //Quartz Bricks -> Chiseled Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.QUARTZ_BRICKS),
              new ItemStack(Blocks.CHISELED_QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_chiseled"));
        //Chiseled Quartz Block -> Quartz Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.CHISELED_QUARTZ_BLOCK),
              new ItemStack(Blocks.QUARTZ_PILLAR)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_to_pillar"));
        //Quartz Pillar -> Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.QUARTZ_PILLAR),
              new ItemStack(Blocks.QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherGraniteRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Polished Granite -> Granite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_GRANITE),
              new ItemStack(Blocks.GRANITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Granite Stairs -> Granite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_GRANITE_STAIRS),
              new ItemStack(Blocks.GRANITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Granite Slab -> Granite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_GRANITE_SLAB),
              new ItemStack(Blocks.GRANITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherDioriteRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Polished Diorite -> Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DIORITE),
              new ItemStack(Blocks.DIORITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Diorite Stairs -> Granite Diorite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DIORITE_STAIRS),
              new ItemStack(Blocks.DIORITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Diorite Slab -> Diorite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_DIORITE_SLAB),
              new ItemStack(Blocks.DIORITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherAndesiteRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Polished Andesite -> Andesite
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_ANDESITE),
              new ItemStack(Blocks.ANDESITE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Andesite Stairs -> Andesite Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_ANDESITE_STAIRS),
              new ItemStack(Blocks.ANDESITE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_from_polished_stairs"));
        //Polished Andesite Slab -> Andesite Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.POLISHED_ANDESITE_SLAB),
              new ItemStack(Blocks.ANDESITE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_from_polished_slab"));
    }

    private void addCrusherPrismarineRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Prismarine -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_block"));
        //Prismarine Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_SLAB),
              new ItemStack(Items.PRISMARINE_SHARD, 2)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_slabs"));
        //Prismarine Stairs -> Prismarine Shards
        // Note: Uses 1 -> 4 as he stone cutter allows for one prismarine block to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_STAIRS),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_stairs"));
        //Prismarine Wall -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_WALL),
              new ItemStack(Items.PRISMARINE_SHARD, 4)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_wall"));
        //Prismarine Brick -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_BRICKS),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick"));
        //Prismarine Brick Slabs -> Prismarine Shards
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_BRICK_SLAB, 2),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick_slabs"));
        //Prismarine Brick Stairs -> Prismarine Shards
        // Note: Uses 1 -> 9 as the stone cutter allows for one brick to one step
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Blocks.PRISMARINE_BRICK_STAIRS),
              new ItemStack(Items.PRISMARINE_SHARD, 9)
        ).build(consumer, Mekanism.rl(basePath + "shard_from_brick_stairs"));
    }

    private void addCrusherDewaxingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Generate baseline recipes from de-waxing recipe set
        for (Map.Entry<Block, Block> entry : HoneycombItem.WAX_OFF_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackToItemStackRecipeBuilder.crushing(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  new ItemStack(result)
            ).build(consumer, Mekanism.rl(basePath + result.asItem()));
        }
    }

    private void addCrusherBioFuelRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Generate baseline recipes from Composter recipe set
        for (Object2FloatMap.Entry<ItemLike> chance : ComposterBlock.COMPOSTABLES.object2FloatEntrySet()) {
            ItemLike input = chance.getKey();
            ItemStackToItemStackRecipeBuilder.crushing(
                  IngredientCreatorAccess.item().from(input),
                  MekanismItems.BIO_FUEL.getItemStack(Math.round(chance.getFloatValue() * 8))
            ).build(consumer, Mekanism.rl(basePath + input.asItem()));
        }
    }
}