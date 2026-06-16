package mekanism.common.recipe.impl;

import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class EnrichingRecipeProvider extends BaseSubRecipeProvider {

    EnrichingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "enriching/";
        addEnrichingConversionRecipes(consumer, basePath + "conversion/");
        addEnrichingDeoxidizingRecipes(consumer, basePath + "deoxidizing/");
        addEnrichingDyeRecipes(consumer, basePath + "dye/");
        addEnrichingEnrichedRecipes(consumer, basePath + "enriched/");
        //Charcoal
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_CHARCOAL),
              template(ItemIds.CHARCOAL)
        ).save(consumer, Mekanism.rl(basePath + "charcoal"));
        //Charcoal dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_WOOD, 8),
              MekanismItems.CHARCOAL_DUST.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Clay ball
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CLAY),
              template(ItemIds.CLAY_BALL, 4)
        ).save(consumer, Mekanism.rl(basePath + "clay_ball"));
        //Glowstone dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GLOWSTONE),
              template(ItemIds.GLOWSTONE_DUST, 4)
        ).save(consumer, Mekanism.rl(basePath + "glowstone_dust"));
        //HDPE Sheet
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().fromHolder(MekanismItems.HDPE_PELLET, 3),
              MekanismItems.HDPE_SHEET.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "hdpe_sheet"));
        //Salt
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().fromHolder(MekanismBlocks.SALT_BLOCK.getItemHolder()),
              MekanismItems.SALT.asTemplate(4)
        ).save(consumer, Mekanism.rl(basePath + "salt"));
        //String
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBWEB),
              template(BlockItemIds.TRIPWIRE, 9)//Value based on bedrock crafting recipe
        ).save(consumer, Mekanism.rl(basePath + "string"));
    }

    private void addEnrichingConversionRecipes(RecipeOutput consumer, String basePath) {
        addEnrichingMudConversionRecipes(consumer, basePath + "mud/");
        addEnrichingStoneConversionRecipes(consumer, basePath + "stone/");
        addEnrichingTuffConversionRecipes(consumer, basePath + "tuff/");
        addEnrichingBlackstoneConversionRecipes(consumer, basePath + "blackstone/");
        addEnrichingDeepslateConversionRecipes(consumer, basePath + "deepslate/");
        addEnrichingQuartzRecipes(consumer, basePath + "quartz/");
        addEnrichingGraniteRecipes(consumer, basePath + "granite/");
        addEnrichingDioriteRecipes(consumer, basePath + "diorite/");
        addEnrichingAndesiteRecipes(consumer, basePath + "andesite/");
        addEnrichingMossyConversionRecipes(consumer, basePath + "mossy/");
        //Purpur Pillar -> Purpur Block
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PURPUR_BLOCK),
              template(BlockItemIds.PURPUR_PILLAR)
        ).save(consumer, Mekanism.rl(basePath + "purpur_pillar_from_block"));
        //Gravel -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GRAVELS),
              template(ItemIds.FLINT)
        ).save(consumer, Mekanism.rl(basePath + "gravel_to_flint"));
        //Gunpowder -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GUNPOWDERS),
              template(ItemIds.FLINT)
        ).save(consumer, Mekanism.rl(basePath + "gunpowder_to_flint"));
        //Sand -> gravel
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.SANDS),
              template(BlockItemIds.GRAVEL)
        ).save(consumer, Mekanism.rl(basePath + "sand_to_gravel"));
        //Soul Sand -> soul soil
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SOUL_SAND),
              template(BlockItemIds.SOUL_SOIL)
        ).save(consumer, Mekanism.rl(basePath + "soul_sand_to_soul_soil"));
        //Sulfur -> gunpowder
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              template(ItemIds.GUNPOWDER)
        ).save(consumer, Mekanism.rl(basePath + "sulfur_to_gunpowder"));
        //Basalt or Smooth -> polished basalt
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.BASALT,
                    Items.SMOOTH_BASALT
              ),
              template(BlockItemIds.POLISHED_BASALT)
        ).save(consumer, Mekanism.rl(basePath + "basalt_or_smooth_to_polished_basalt"));
        //Cracked nether bricks -> nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_NETHER_BRICKS),
              template(BlockItemIds.NETHER_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "cracked_nether_bricks_to_nether_bricks"));
        //Nether bricks -> chiseled nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.NETHER_BRICKS),
              template(BlockItemIds.CHISELED_NETHER_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "nether_bricks_to_chiseled_nether_bricks"));
    }

    private void addEnrichingMudConversionRecipes(RecipeOutput consumer, String basePath) {
        //Mud -> clay
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.CLAY)
        ).save(consumer, Mekanism.rl(basePath + "to_clay"));
        //Packed mud -> mud bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PACKED_MUD),
              template(BlockItemIds.MUD_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "packed_to_bricks"));
    }

    private void addEnrichingStoneConversionRecipes(RecipeOutput consumer, String basePath) {
        //Stone -> cracked stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE),
              template(BlockItemIds.CRACKED_STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "to_cracked_bricks"));
        //Cracked stone bricks -> stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_STONE_BRICKS),
              template(BlockItemIds.STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "cracked_bricks_to_bricks"));
        //Stone bricks -> chiseled stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICKS),
              template(BlockItemIds.CHISELED_STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_chiseled_bricks"));
    }

    private void addEnrichingTuffConversionRecipes(RecipeOutput consumer, String basePath) {
        //Tuff -> Polished Tuff
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF),
              template(BlockItemIds.POLISHED_TUFF)
        ).save(consumer, Mekanism.rl(basePath + "to_polished"));
        //Tuff Stairs -> Polished Tuff Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_STAIRS),
              template(BlockItemIds.POLISHED_TUFF_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_polished"));
        //Tuff Slabs -> Polished Tuff Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_SLAB),
              template(BlockItemIds.POLISHED_TUFF_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slabs_to_polished"));
        //Tuff Walls -> Polished Tuff Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_WALL),
              template(BlockItemIds.POLISHED_TUFF_WALL)
        ).save(consumer, Mekanism.rl(basePath + "wall_to_polished"));

        //Polished Tuff -> Tuff Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF),
              template(BlockItemIds.TUFF_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "polished_to_brick"));
        //Polished Tuff Stairs -> Tuff Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_STAIRS),
              template(BlockItemIds.TUFF_BRICK_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "polished_stairs_to_brick"));
        //Polished Tuff Slabs -> Tuff Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_SLAB),
              template(BlockItemIds.TUFF_BRICK_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "polished_slabs_to_brick"));
        //Polished Tuff Walls -> Tuff Brick Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_WALL),
              template(BlockItemIds.TUFF_BRICK_WALL)
        ).save(consumer, Mekanism.rl(basePath + "polished_wall_to_brick"));

        //Tuff Bricks -> Chiseled Tuff
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICKS),
              template(BlockItemIds.CHISELED_TUFF)
        ).save(consumer, Mekanism.rl(basePath + "brick_to_chiseled"));

        //Chiseled Tuff -> Tuff
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_TUFF),
              template(BlockItemIds.TUFF)
        ).save(consumer, Mekanism.rl(basePath + "from_chiseled"));
        //Tuff Brick Stairs -> Tuff Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_STAIRS),
              template(BlockItemIds.TUFF_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_from_brick"));
        //Tuff Brick Slabs -> Tuff Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_SLAB),
              template(BlockItemIds.TUFF_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_from_brick"));
        //Tuff Brick Walls -> Tuff Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICK_WALL),
              template(BlockItemIds.TUFF_WALL)
        ).save(consumer, Mekanism.rl(basePath + "wall_from_brick"));
    }

    private void addEnrichingDeepslateConversionRecipes(RecipeOutput consumer, String basePath) {
        //Cobbled Deepslate -> Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "from_cobbled"));
        //Cobbled Deepslate Stairs -> Polished Deepslate Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_STAIRS),
              template(BlockItemIds.POLISHED_DEEPSLATE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "cobbled_stairs_to_polished"));
        //Cobbled Deepslate Slabs -> Polished Deepslate Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_SLAB),
              template(BlockItemIds.POLISHED_DEEPSLATE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "cobbled_slabs_to_polished"));
        //Cobbled Deepslate Wall -> Polished Deepslate Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_WALL),
              template(BlockItemIds.POLISHED_DEEPSLATE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "cobbled_wall_to_polished"));

        //Deepslate -> Polished Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE),
              template(BlockItemIds.POLISHED_DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "to_polished"));
        //Polished Deepslate -> Chiseled Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE),
              template(BlockItemIds.CHISELED_DEEPSLATE)
        ).save(consumer, Mekanism.rl(basePath + "polished_chiseled"));
        //Chiseled Deepslate -> Cracked Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_DEEPSLATE),
              template(BlockItemIds.CRACKED_DEEPSLATE_TILES)
        ).save(consumer, Mekanism.rl(basePath + "chiseled_to_cracked_tile"));
        //Cracked Deepslate Tiles -> Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_TILES),
              template(BlockItemIds.DEEPSLATE_TILES)
        ).save(consumer, Mekanism.rl(basePath + "cracked_tile_to_tile"));
        //Deepslate Tiles -> Cracked Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILES),
              template(BlockItemIds.CRACKED_DEEPSLATE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "tiles_to_cracked_bricks"));
        //Cracked Deepslate Bricks -> Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_BRICKS),
              template(BlockItemIds.DEEPSLATE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "cracked_bricks_to_bricks"));

        //Deepslate Tile Stairs -> Deepslate Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_STAIRS),
              template(BlockItemIds.DEEPSLATE_BRICK_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "tile_stairs_to_brick"));
        //Deepslate Tile Slabs -> Deepslate Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_SLAB),
              template(BlockItemIds.DEEPSLATE_BRICK_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "tile_slabs_to_brick"));
        //Deepslate Tile Wall -> Deepslate Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_WALL),
              template(BlockItemIds.DEEPSLATE_BRICK_WALL)
        ).save(consumer, Mekanism.rl(basePath + "tile_wall_to_brick"));

        //Polished Deepslate Stairs -> Deepslate Tile Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_STAIRS),
              template(BlockItemIds.DEEPSLATE_TILE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "polished_stairs_to_tile"));
        //Polished Deepslate Slabs -> Deepslate Tile Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_SLAB),
              template(BlockItemIds.DEEPSLATE_TILE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "polished_slabs_to_tile"));
        //Polished Deepslate Wall -> Deepslate Tile Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_WALL),
              template(BlockItemIds.DEEPSLATE_TILE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "polished_wall_to_tile"));
    }

    private void addEnrichingBlackstoneConversionRecipes(RecipeOutput consumer, String basePath) {
        //Polished blackstone -> cracked polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE),
              template(BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "to_cracked_bricks"));
        //Cracked polished blackstone bricks -> polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              template(BlockItemIds.POLISHED_BLACKSTONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "cracked_bricks_to_bricks"));
        //Polished blackstone bricks -> chiseled polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_BRICKS),
              template(BlockItemIds.CHISELED_POLISHED_BLACKSTONE)
        ).save(consumer, Mekanism.rl(basePath + "bricks_to_chiseled_bricks"));
    }

    private void addEnrichingGraniteRecipes(RecipeOutput consumer, String basePath) {
        //Granite -> Polished Granite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE),
              template(BlockItemIds.POLISHED_GRANITE)
        ).save(consumer, Mekanism.rl(basePath + "to_polished"));
        //Granite Stairs -> Polished Granite Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE_STAIRS),
              template(BlockItemIds.POLISHED_GRANITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_polished_stairs"));
        //Granite Slab -> Polished Granite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE_SLAB),
              template(BlockItemIds.POLISHED_GRANITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_to_polished_slab"));
    }

    private void addEnrichingDioriteRecipes(RecipeOutput consumer, String basePath) {
        //Diorite -> Polished Diorite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE),
              template(BlockItemIds.POLISHED_DIORITE)
        ).save(consumer, Mekanism.rl(basePath + "to_polished"));
        //Diorite Stairs -> Polished Granite Diorite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE_STAIRS),
              template(BlockItemIds.POLISHED_DIORITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_polished_stairs"));
        //Diorite Slab -> Polished Diorite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE_SLAB),
              template(BlockItemIds.POLISHED_DIORITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_to_polished_slab"));
    }

    private void addEnrichingAndesiteRecipes(RecipeOutput consumer, String basePath) {
        //Andesite -> Polished Andesite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE),
              template(BlockItemIds.POLISHED_ANDESITE)
        ).save(consumer, Mekanism.rl(basePath + "to_polished"));
        //Polished Andesite Stairs -> Andesite Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE_STAIRS),
              template(BlockItemIds.POLISHED_ANDESITE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stairs_to_polished_stairs"));
        //Polished Andesite Slab -> Andesite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE_SLAB),
              template(BlockItemIds.POLISHED_ANDESITE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "slab_to_polished_slab"));
    }

    private void addEnrichingQuartzRecipes(RecipeOutput consumer, String basePath) {
        //Quartz Block -> Quartz Item
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.QUARTZ_BLOCK,
                    Items.QUARTZ_BRICKS,
                    Items.CHISELED_QUARTZ_BLOCK,
                    Items.QUARTZ_PILLAR
              ),
              template(ItemIds.QUARTZ, 4)
        ).save(consumer, Mekanism.rl(basePath + "to_item"));
        //Smooth Quartz Block -> Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ),
              template(BlockItemIds.QUARTZ_BLOCK)
        ).save(consumer, Mekanism.rl(basePath + "from_smooth_quartz"));
        //Smooth Quartz Slab -> Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ_SLAB),
              template(BlockItemIds.QUARTZ_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "smooth_slab_to_slab"));
        //Smooth Quartz Stairs -> Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ_STAIRS),
              template(BlockItemIds.QUARTZ_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "smooth_stairs_to_stairs"));
    }

    private void addEnrichingMossyConversionRecipes(RecipeOutput consumer, String basePath) {
        //Mossy Stone Brick -> Stone Brick recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICKS),
              template(BlockItemIds.STONE_BRICKS)
        ).save(consumer, Mekanism.rl(basePath + "stone_bricks"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_STAIRS),
              template(BlockItemIds.STONE_BRICK_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_SLAB),
              template(BlockItemIds.STONE_BRICK_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_slabs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_WALL),
              template(BlockItemIds.STONE_BRICK_WALL)
        ).save(consumer, Mekanism.rl(basePath + "stone_brick_walls"));
        //Mossy Cobblestone -> Cobblestone recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE),
              template(BlockItemIds.COBBLESTONE)
        ).save(consumer, Mekanism.rl(basePath + "cobblestone"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_STAIRS),
              template(BlockItemIds.COBBLESTONE_STAIRS)
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_SLAB),
              template(BlockItemIds.COBBLESTONE_SLAB)
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_slabs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_WALL),
              template(BlockItemIds.COBBLESTONE_WALL)
        ).save(consumer, Mekanism.rl(basePath + "cobblestone_walls"));
    }

    private void addEnrichingDeoxidizingRecipes(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from weathering recipe set
        for (Map.Entry<Block, Block> entry : WeatheringCopper.PREVIOUS_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackToItemStackRecipeBuilder.enriching(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  new ItemStackTemplate(result.asItem())
            ).save(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }

    private void addEnrichingDyeRecipes(RecipeOutput consumer, String basePath) {
        //Red
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ROSE_BUSH),
              template(ItemIds.DYE.red(), 4)
        ).save(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.BEETROOT,
                    Items.POPPY,
                    Items.RED_TULIP
              ),
              template(ItemIds.DYE.red(), 2)
        ).save(consumer, Mekanism.rl(basePath + "small_red"));
        //Cyan
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PITCHER_PLANT),
              template(ItemIds.DYE.cyan(), 4)
        ).save(consumer, Mekanism.rl(basePath + "large_cyan"));
        //Green
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CACTUS),
              template(ItemIds.DYE.green(), 2)
        ).save(consumer, Mekanism.rl(basePath + "green"));
        //Light gray
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.OXEYE_DAISY,
                    Items.AZURE_BLUET,
                    Items.WHITE_TULIP
              ),
              template(ItemIds.DYE.lightGray(), 2)
        ).save(consumer, Mekanism.rl(basePath + "light_gray"));
        //Pink
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PEONY),
              template(ItemIds.DYE.pink(), 4)
        ).save(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.PINK_TULIP,
                    Items.PINK_PETALS
              ),
              template(ItemIds.DYE.pink(), 2)
        ).save(consumer, Mekanism.rl(basePath + "small_pink"));
        //Lime
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SEA_PICKLE),
              template(ItemIds.DYE.lime(), 2)
        ).save(consumer, Mekanism.rl(basePath + "lime"));
        //Yellow
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SUNFLOWER),
              template(ItemIds.DYE.yellow(), 4)
        ).save(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DANDELION),
              template(ItemIds.DYE.yellow(), 2)
        ).save(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BLUE_ORCHID),
              template(ItemIds.DYE.lightBlue(), 2)
        ).save(consumer, Mekanism.rl(basePath + "light_blue"));
        //Magenta
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.LILAC),
              template(ItemIds.DYE.magenta(), 4)
        ).save(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ALLIUM),
              template(ItemIds.DYE.magenta(), 2)
        ).save(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Orange
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.ORANGE_TULIP,
                    Items.TORCHFLOWER
              ),
              template(ItemIds.DYE.orange(), 2)
        ).save(consumer, Mekanism.rl(basePath + "orange"));
        //Blue
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.CORNFLOWER,
                    Items.LAPIS_LAZULI
              ),
              template(ItemIds.DYE.blue(), 2)
        ).save(consumer, Mekanism.rl(basePath + "blue"));
        //Brown
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COCOA_CROP),
              template(ItemIds.DYE.brown(), 2)
        ).save(consumer, Mekanism.rl(basePath + "brown"));
        //Black
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.INK_SAC,
                    Items.WITHER_ROSE
              ),
              template(ItemIds.DYE.black(), 2)
        ).save(consumer, Mekanism.rl(basePath + "black"));
        //White
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(
                    Items.BONE_MEAL,
                    Items.LILY_OF_THE_VALLEY
              ),
              template(ItemIds.DYE.white(), 2)
        ).save(consumer, Mekanism.rl(basePath + "white"));
    }

    private void addEnrichingEnrichedRecipes(RecipeOutput consumer, String basePath) {
        //Carbon
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, ItemTags.COALS),
              MekanismItems.ENRICHED_CARBON.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "carbon"));
        //Diamond
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_DIAMOND),
              MekanismItems.ENRICHED_DIAMOND.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "diamond"));
        //Redstone
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_REDSTONE),
              MekanismItems.ENRICHED_REDSTONE.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "redstone"));
        //Refined Obsidian
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismItems.ENRICHED_OBSIDIAN.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "refined_obsidian"));
        //Gold
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD)),
              MekanismItems.ENRICHED_GOLD.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "gold"));
        //Tin
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN)),
              MekanismItems.ENRICHED_TIN.asTemplate()
        ).save(consumer, Mekanism.rl(basePath + "tin"));
    }
}