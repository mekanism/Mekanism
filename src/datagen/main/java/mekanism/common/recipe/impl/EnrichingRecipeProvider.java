package mekanism.common.recipe.impl;

import java.util.Arrays;
import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class EnrichingRecipeProvider extends BaseSubRecipeProvider {

    EnrichingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "enriching/";
        addEnrichingConversionRecipes(output, basePath + "conversion/");
        addEnrichingDeoxidizingRecipes(output, basePath + "deoxidizing/");
        addEnrichingDyeRecipes(output, basePath + "dye/");
        addEnrichingEnrichedRecipes(output, basePath + "enriched/");
        //Charcoal
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_CHARCOAL),
              template(ItemIds.CHARCOAL)
        ).unlockedBy("has_charcoal_dust", has(MekanismTags.Items.DUSTS_CHARCOAL))
              .save(output, basePath + "charcoal");
        //Charcoal dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_WOOD, 8),
              MekanismItems.CHARCOAL_DUST.asTemplate()
        ).unlockedBy("has_sawdust", has(MekanismTags.Items.DUSTS_WOOD))
              .save(output, basePath + "charcoal_dust");
        //Clay ball
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CLAY),
              template(ItemIds.CLAY_BALL, 4)
        ).unlockedBy(hasCriterion(BlockItemIds.CLAY))
              .save(output, basePath + "clay_ball");
        //Glowstone dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GLOWSTONE),
              template(ItemIds.GLOWSTONE_DUST, 4)
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy(hasCriterion(BlockItemIds.GLOWSTONE))
              .save(output, basePath + "glowstone_dust");
        //HDPE Sheet
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().fromHolder(MekanismItems.HDPE_PELLET, 3),
              MekanismItems.HDPE_SHEET.asTemplate()
        ).unlockedBy(hasCriterion(MekanismItems.HDPE_PELLET))
              .save(output, basePath + "hdpe_sheet");
        //Salt
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().fromHolder(MekanismBlocks.SALT_BLOCK.getItemHolder()),
              MekanismItems.SALT.asTemplate(4)
        ).unlockedBy("has_salt", has(MekanismBlocks.SALT_BLOCK))
              .save(output, basePath + "salt");
        //String
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBWEB),
              template(BlockItemIds.TRIPWIRE, 9)//Value based on bedrock crafting recipe
        ).unlockedBy(hasCriterion(BlockItemIds.COBWEB))
              .save(output, basePath + "string");
    }

    private void addEnrichingConversionRecipes(RecipeOutput output, String basePath) {
        addEnrichingMudConversionRecipes(output, basePath + "mud/");
        addEnrichingStoneConversionRecipes(output, basePath + "stone/");
        addEnrichingCinnabarConversionRecipes(output, basePath + "cinnabar/");
        addEnrichingSulfurConversionRecipes(output, basePath + "sulfur/");
        addEnrichingTuffConversionRecipes(output, basePath + "tuff/");
        addEnrichingBlackstoneConversionRecipes(output, basePath + "blackstone/");
        addEnrichingDeepslateConversionRecipes(output, basePath + "deepslate/");
        addEnrichingQuartzRecipes(output, basePath + "quartz/");
        addEnrichingGraniteRecipes(output, basePath + "granite/");
        addEnrichingDioriteRecipes(output, basePath + "diorite/");
        addEnrichingAndesiteRecipes(output, basePath + "andesite/");
        addEnrichingResinRecipes(output, basePath + "resin/");
        addEnrichingMossyConversionRecipes(output, basePath + "mossy/");
        //Purpur Pillar -> Purpur Block
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PURPUR_BLOCK),
              template(BlockItemIds.PURPUR_PILLAR)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.PURPUR_BLOCK))
              .save(output, basePath + "purpur_pillar_from_block");
        //Gravel -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.GRAVELS),
              template(ItemIds.FLINT)
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_gravel", has(Tags.BlockItems.GRAVELS))
              .save(output, basePath + "gravel_to_flint");
        //Gunpowder -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GUNPOWDERS),
              template(ItemIds.FLINT)
        ).unlockedBy("has_gunpowder", has(Tags.Items.GUNPOWDERS))
              .save(output, basePath + "gunpowder_to_flint");
        //Sand -> gravel
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.SANDS),
              template(BlockItemIds.GRAVEL)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy("has_sand", has(Tags.BlockItems.SANDS))
              .save(output, basePath + "sand_to_gravel");
        //Soul Sand -> soul soil
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SOUL_SAND),
              template(BlockItemIds.SOUL_SOIL)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.SOUL_SAND))
              .save(output, basePath + "soul_sand_to_soul_soil");
        //Basalt or Smooth -> polished basalt
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items,
                    BlockItemIds.BASALT,
                    BlockItemIds.SMOOTH_BASALT
              ),
              template(BlockItemIds.POLISHED_BASALT)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.BASALT))
              .unlockedBy(hasCriterion(BlockItemIds.SMOOTH_BASALT))
              .save(output, basePath + "basalt_or_smooth_to_polished_basalt");
        //Cracked nether bricks -> nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_NETHER_BRICKS),
              template(BlockItemIds.NETHER_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.CRACKED_NETHER_BRICKS))
              .save(output, basePath + "cracked_nether_bricks_to_nether_bricks");
        //Nether bricks -> chiseled nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.NETHER_BRICKS),
              template(BlockItemIds.CHISELED_NETHER_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.NETHER_BRICKS))
              .save(output, basePath + "nether_bricks_to_chiseled_nether_bricks");
    }

    private void addEnrichingMudConversionRecipes(RecipeOutput output, String basePath) {
        //Mud -> clay
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MUD),
              template(BlockItemIds.CLAY)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.MUD))
              .save(output, basePath + "to_clay");
        //Packed mud -> mud bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.PACKED_MUD),
              template(BlockItemIds.MUD_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.PACKED_MUD))
              .save(output, basePath + "packed_to_bricks");
    }

    private void addEnrichingStoneConversionRecipes(RecipeOutput output, String basePath) {
        //Stone -> cracked stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE),
              template(BlockItemIds.CRACKED_STONE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.STONE))
              .save(output, basePath + "to_cracked_bricks");
        //Cracked stone bricks -> stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_STONE_BRICKS),
              template(BlockItemIds.STONE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.CRACKED_STONE_BRICKS))
              .save(output, basePath + "cracked_bricks_to_bricks");
        //Stone bricks -> chiseled stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.STONE_BRICKS),
              template(BlockItemIds.CHISELED_STONE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.STONE_BRICKS))
              .save(output, basePath + "bricks_to_chiseled_bricks");
    }

    private void addEnrichingCinnabarConversionRecipes(RecipeOutput output, String basePath) {
        //Cinnabar -> Polished Cinnabar
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR),
              template(BlockItemIds.POLISHED_CINNABAR)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_cinnabar_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CINNABAR))
              .save(output, basePath + "to_polished");
        //Cinnabar Stairs -> Polished Cinnabar Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_STAIRS),
              template(BlockItemIds.POLISHED_CINNABAR_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_cinnabar_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CINNABAR_STAIRS))
              .save(output, basePath + "stairs_to_polished");
        //Cinnabar Slabs -> Polished Cinnabar Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_SLAB),
              template(BlockItemIds.POLISHED_CINNABAR_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_cinnabar_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CINNABAR_SLAB))
              .save(output, basePath + "slabs_to_polished");
        //Cinnabar Walls -> Polished Cinnabar Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_WALL),
              template(BlockItemIds.POLISHED_CINNABAR_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_cinnabar_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CINNABAR_WALL))
              .save(output, basePath + "wall_to_polished");

        //Polished Cinnabar -> Cinnabar Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR),
              template(BlockItemIds.CINNABAR_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cinnabar_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_CINNABAR))
              .save(output, basePath + "polished_to_brick");
        //Polished Cinnabar Stairs -> Cinnabar Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_STAIRS),
              template(BlockItemIds.CINNABAR_BRICK_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cinnabar_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_CINNABAR_STAIRS))
              .save(output, basePath + "polished_stairs_to_brick");
        //Polished Cinnabar Slabs -> Cinnabar Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_SLAB),
              template(BlockItemIds.CINNABAR_BRICK_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cinnabar_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_CINNABAR_SLAB))
              .save(output, basePath + "polished_slabs_to_brick");
        //Polished Cinnabar Walls -> Cinnabar Brick Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_CINNABAR_WALL),
              template(BlockItemIds.CINNABAR_BRICK_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cinnabar_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_CINNABAR_WALL))
              .save(output, basePath + "polished_wall_to_brick");

        //Cinnabar Bricks -> Chiseled Cinnabar
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CINNABAR_BRICKS),
              template(BlockItemIds.CHISELED_CINNABAR)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("chiseled_cinnabar_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CINNABAR_BRICKS))
              .save(output, basePath + "brick_to_chiseled");
    }

    private void addEnrichingSulfurConversionRecipes(RecipeOutput output, String basePath) {
        //Sulfur Dust -> Sulfur Spike
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_SULFUR),
              template(BlockItemIds.SULFUR_SPIKE)
        ).unlockedBy("has_sulfur_dust", has(MekanismTags.Items.DUSTS_SULFUR))
              .save(output, basePath + "dust_to_spike");
        //Sulfur Spike -> gunpowder
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_SPIKE),
              template(ItemIds.GUNPOWDER)
        ).unlockedBy(hasCriterion(BlockItemIds.SULFUR_SPIKE))
              .save(output, basePath + "spike_to_gunpowder");

        //Sulfur -> Polished Sulfur
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR),
              template(BlockItemIds.POLISHED_SULFUR)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_sulfur_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SULFUR))
              .save(output, basePath + "to_polished");
        //Sulfur Stairs -> Polished Sulfur Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_STAIRS),
              template(BlockItemIds.POLISHED_SULFUR_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_sulfur_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SULFUR_STAIRS))
              .save(output, basePath + "stairs_to_polished");
        //Sulfur Slabs -> Polished Sulfur Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_SLAB),
              template(BlockItemIds.POLISHED_SULFUR_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_sulfur_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SULFUR_SLAB))
              .save(output, basePath + "slabs_to_polished");
        //Sulfur Walls -> Polished Sulfur Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_WALL),
              template(BlockItemIds.POLISHED_SULFUR_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_sulfur_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SULFUR_WALL))
              .save(output, basePath + "wall_to_polished");

        //Polished Sulfur -> Sulfur Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR),
              template(BlockItemIds.SULFUR_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("sulfur_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_SULFUR))
              .save(output, basePath + "polished_to_brick");
        //Polished Sulfur Stairs -> Sulfur Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_STAIRS),
              template(BlockItemIds.SULFUR_BRICK_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("sulfur_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_SULFUR_STAIRS))
              .save(output, basePath + "polished_stairs_to_brick");
        //Polished Sulfur Slabs -> Sulfur Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_SLAB),
              template(BlockItemIds.SULFUR_BRICK_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("sulfur_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_SULFUR_SLAB))
              .save(output, basePath + "polished_slabs_to_brick");
        //Polished Sulfur Walls -> Sulfur Brick Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_SULFUR_WALL),
              template(BlockItemIds.SULFUR_BRICK_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("sulfur_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_SULFUR_WALL))
              .save(output, basePath + "polished_wall_to_brick");

        //Sulfur Bricks -> Chiseled Sulfur
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SULFUR_BRICKS),
              template(BlockItemIds.CHISELED_SULFUR)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("chiseled_sulfur_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SULFUR_BRICKS))
              .save(output, basePath + "brick_to_chiseled");
    }

    private void addEnrichingTuffConversionRecipes(RecipeOutput output, String basePath) {
        //Tuff -> Polished Tuff
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF),
              template(BlockItemIds.POLISHED_TUFF)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_tuff_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.TUFF))
              .save(output, basePath + "to_polished");
        //Tuff Stairs -> Polished Tuff Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_STAIRS),
              template(BlockItemIds.POLISHED_TUFF_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_tuff_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.TUFF_STAIRS))
              .save(output, basePath + "stairs_to_polished");
        //Tuff Slabs -> Polished Tuff Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_SLAB),
              template(BlockItemIds.POLISHED_TUFF_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_tuff_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.TUFF_SLAB))
              .save(output, basePath + "slabs_to_polished");
        //Tuff Walls -> Polished Tuff Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_WALL),
              template(BlockItemIds.POLISHED_TUFF_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_tuff_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.TUFF_WALL))
              .save(output, basePath + "wall_to_polished");

        //Polished Tuff -> Tuff Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF),
              template(BlockItemIds.TUFF_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("tuff_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_TUFF))
              .save(output, basePath + "polished_to_brick");
        //Polished Tuff Stairs -> Tuff Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_STAIRS),
              template(BlockItemIds.TUFF_BRICK_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("tuff_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_TUFF_STAIRS))
              .save(output, basePath + "polished_stairs_to_brick");
        //Polished Tuff Slabs -> Tuff Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_SLAB),
              template(BlockItemIds.TUFF_BRICK_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("tuff_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_TUFF_SLAB))
              .save(output, basePath + "polished_slabs_to_brick");
        //Polished Tuff Walls -> Tuff Brick Walls
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_TUFF_WALL),
              template(BlockItemIds.TUFF_BRICK_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("tuff_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_TUFF_WALL))
              .save(output, basePath + "polished_wall_to_brick");

        //Tuff Bricks -> Chiseled Tuff
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TUFF_BRICKS),
              template(BlockItemIds.CHISELED_TUFF)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("chiseled_tuff_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.TUFF_BRICKS))
              .save(output, basePath + "brick_to_chiseled");
    }

    private void addEnrichingDeepslateConversionRecipes(RecipeOutput output, String basePath) {
        //Cobbled Deepslate -> Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_conversion"))
              .unlockedBy("has_deepslate_cobblestone", has(Tags.BlockItems.COBBLESTONES_DEEPSLATE))
              .save(output, basePath + "from_cobbled");
        //Cobbled Deepslate Stairs -> Polished Deepslate Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_STAIRS),
              template(BlockItemIds.POLISHED_DEEPSLATE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLED_DEEPSLATE_STAIRS))
              .save(output, basePath + "cobbled_stairs_to_polished");
        //Cobbled Deepslate Slabs -> Polished Deepslate Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_SLAB),
              template(BlockItemIds.POLISHED_DEEPSLATE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLED_DEEPSLATE_SLAB))
              .save(output, basePath + "cobbled_slabs_to_polished");
        //Cobbled Deepslate Wall -> Polished Deepslate Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.COBBLED_DEEPSLATE_WALL),
              template(BlockItemIds.POLISHED_DEEPSLATE_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.COBBLED_DEEPSLATE_WALL))
              .save(output, basePath + "cobbled_wall_to_polished");

        //Deepslate -> Polished Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE),
              template(BlockItemIds.POLISHED_DEEPSLATE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DEEPSLATE))
              .save(output, basePath + "to_polished");

        //Chiseled Deepslate -> Cracked Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CHISELED_DEEPSLATE),
              template(BlockItemIds.CRACKED_DEEPSLATE_TILES)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CHISELED_DEEPSLATE))
              .save(output, basePath + "chiseled_to_cracked_tile");

        //Cracked Deepslate Tiles -> Deepslate Tiles
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_TILES),
              template(BlockItemIds.DEEPSLATE_TILES)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_tile_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CRACKED_DEEPSLATE_TILES))
              .save(output, basePath + "cracked_tile_to_tile");
        //Cracked Deepslate Bricks -> Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_DEEPSLATE_BRICKS),
              template(BlockItemIds.DEEPSLATE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CRACKED_DEEPSLATE_BRICKS))
              .save(output, basePath + "cracked_bricks_to_bricks");

        //Deepslate Tiles -> Cracked Deepslate Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILES),
                    template(BlockItemIds.CRACKED_DEEPSLATE_BRICKS)
              ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cracked_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DEEPSLATE_TILES))
              .save(output, basePath + "tiles_to_cracked_bricks");
        //Deepslate Tile Stairs -> Deepslate Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_STAIRS),
              template(BlockItemIds.DEEPSLATE_BRICK_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DEEPSLATE_TILE_STAIRS))
              .save(output, basePath + "tile_stairs_to_brick");
        //Deepslate Tile Slabs -> Deepslate Brick Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_SLAB),
              template(BlockItemIds.DEEPSLATE_BRICK_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DEEPSLATE_TILE_SLAB))
              .save(output, basePath + "tile_slabs_to_brick");
        //Deepslate Tile Wall -> Deepslate Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DEEPSLATE_TILE_WALL),
              template(BlockItemIds.DEEPSLATE_BRICK_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_brick_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DEEPSLATE_TILE_WALL))
              .save(output, basePath + "tile_wall_to_brick");

        //Polished Deepslate -> Chiseled Deepslate
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE),
                    template(BlockItemIds.CHISELED_DEEPSLATE)
              ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("chiseled_deepslate_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_DEEPSLATE))
              .save(output, basePath + "polished_chiseled");
        //Polished Deepslate Stairs -> Deepslate Tile Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_STAIRS),
              template(BlockItemIds.DEEPSLATE_TILE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_tile_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_DEEPSLATE_STAIRS))
              .save(output, basePath + "polished_stairs_to_tile");
        //Polished Deepslate Slabs -> Deepslate Tile Slabs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_SLAB),
              template(BlockItemIds.DEEPSLATE_TILE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_tile_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_DEEPSLATE_SLAB))
              .save(output, basePath + "polished_slabs_to_tile");
        //Polished Deepslate Wall -> Deepslate Tile Wall
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_DEEPSLATE_WALL),
              template(BlockItemIds.DEEPSLATE_TILE_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("deepslate_tile_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_DEEPSLATE_WALL))
              .save(output, basePath + "polished_wall_to_tile");
    }

    private void addEnrichingBlackstoneConversionRecipes(RecipeOutput output, String basePath) {
        //Polished blackstone -> cracked polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE),
              template(BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("cracked_blackstone_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_BLACKSTONE))
              .save(output, basePath + "to_cracked_bricks");
        //Polished blackstone bricks -> chiseled polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.POLISHED_BLACKSTONE_BRICKS),
              template(BlockItemIds.CHISELED_POLISHED_BLACKSTONE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("chiseled_blackstone_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.POLISHED_BLACKSTONE_BRICKS))
              .save(output, basePath + "bricks_to_chiseled_bricks");

        //Cracked polished blackstone bricks -> polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS),
                    template(BlockItemIds.POLISHED_BLACKSTONE_BRICKS)
              ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_blackstone_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.CRACKED_POLISHED_BLACKSTONE_BRICKS))
              .save(output, basePath + "cracked_bricks_to_bricks");
    }

    private void addEnrichingGraniteRecipes(RecipeOutput output, String basePath) {
        //Granite -> Polished Granite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE),
              template(BlockItemIds.POLISHED_GRANITE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_granite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.GRANITE))
              .save(output, basePath + "to_polished");
        //Granite Stairs -> Polished Granite Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE_STAIRS),
              template(BlockItemIds.POLISHED_GRANITE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_granite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.GRANITE_STAIRS))
              .save(output, basePath + "stairs_to_polished_stairs");
        //Granite Slab -> Polished Granite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.GRANITE_SLAB),
              template(BlockItemIds.POLISHED_GRANITE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_granite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.GRANITE_SLAB))
              .save(output, basePath + "slab_to_polished_slab");
    }

    private void addEnrichingDioriteRecipes(RecipeOutput output, String basePath) {
        //Diorite -> Polished Diorite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE),
              template(BlockItemIds.POLISHED_DIORITE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_diorite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DIORITE))
              .save(output, basePath + "to_polished");
        //Diorite Stairs -> Polished Granite Diorite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE_STAIRS),
              template(BlockItemIds.POLISHED_DIORITE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_diorite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DIORITE_STAIRS))
              .save(output, basePath + "stairs_to_polished_stairs");
        //Diorite Slab -> Polished Diorite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIORITE_SLAB),
              template(BlockItemIds.POLISHED_DIORITE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_diorite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.DIORITE_SLAB))
              .save(output, basePath + "slab_to_polished_slab");
    }

    private void addEnrichingAndesiteRecipes(RecipeOutput output, String basePath) {
        //Andesite -> Polished Andesite
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE),
              template(BlockItemIds.POLISHED_ANDESITE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_andesite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.ANDESITE))
              .save(output, basePath + "to_polished");
        //Polished Andesite Stairs -> Andesite Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE_STAIRS),
              template(BlockItemIds.POLISHED_ANDESITE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_andesite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.ANDESITE_STAIRS))
              .save(output, basePath + "stairs_to_polished_stairs");
        //Polished Andesite Slab -> Andesite Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.ANDESITE_SLAB),
              template(BlockItemIds.POLISHED_ANDESITE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("polished_andesite_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.ANDESITE_SLAB))
              .save(output, basePath + "slab_to_polished_slab");
    }

    private void addEnrichingQuartzRecipes(RecipeOutput output, String basePath) {
        //Quartz Block -> Quartz Item
        BlockItemId[] quartzBlocks = {
              BlockItemIds.QUARTZ_BLOCK,
              BlockItemIds.QUARTZ_BRICKS,
              BlockItemIds.CHISELED_QUARTZ_BLOCK,
              BlockItemIds.QUARTZ_PILLAR
        };
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, quartzBlocks),
              template(ItemIds.QUARTZ, 4)
        ).unlockedBy(Arrays.stream(quartzBlocks).map(this::hasCriterion))
              .save(output, basePath + "to_item");

        //Smooth Quartz Block -> Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ),
              template(BlockItemIds.QUARTZ_BLOCK)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("quartz_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SMOOTH_QUARTZ))
              .save(output, basePath + "from_smooth_quartz");
        //Smooth Quartz Slab -> Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ_SLAB),
              template(BlockItemIds.QUARTZ_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("quartz_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SMOOTH_QUARTZ_SLAB))
              .save(output, basePath + "smooth_slab_to_slab");
        //Smooth Quartz Stairs -> Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.SMOOTH_QUARTZ_STAIRS),
              template(BlockItemIds.QUARTZ_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("quartz_conversion"))
              .unlockedBy(hasCriterion(BlockItemIds.SMOOTH_QUARTZ_STAIRS))
              .save(output, basePath + "smooth_stairs_to_stairs");
    }

    private void addEnrichingResinRecipes(RecipeOutput output, String basePath) {
        //Resin Bricks -> Resin Blocks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.STORAGE_BLOCKS_RESIN),
              template(BlockItemIds.RESIN_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy("has_resin_block", has(Tags.BlockItems.STORAGE_BLOCKS_RESIN))
              .save(output, basePath + "block_to_bricks");
        //Chiseled Resin Bricks -> Resin Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.RESIN_BRICKS),
              template(BlockItemIds.CHISELED_RESIN_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasCriterion(BlockItemIds.RESIN_BRICKS))
              .save(output, basePath + "bricks_to_chiseled");
    }

    private void addEnrichingMossyConversionRecipes(RecipeOutput output, String basePath) {
        //Mossy Stone Brick -> Stone Brick recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICKS),
              template(BlockItemIds.STONE_BRICKS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_STONE_BRICKS))
              .save(output, basePath + "stone_bricks");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_STAIRS),
              template(BlockItemIds.STONE_BRICK_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_STONE_BRICK_STAIRS))
              .save(output, basePath + "stone_brick_stairs");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_SLAB),
              template(BlockItemIds.STONE_BRICK_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_STONE_BRICK_SLAB))
              .save(output, basePath + "stone_brick_slabs");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_STONE_BRICK_WALL),
              template(BlockItemIds.STONE_BRICK_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_stone_brick_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_STONE_BRICK_WALL))
              .save(output, basePath + "stone_brick_walls");
        //Mossy Cobblestone -> Cobblestone recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_MOSSY),
              template(BlockItemIds.COBBLESTONE)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_removal"))
              .unlockedBy("has_mossy_cobblestone", has(Tags.BlockItems.COBBLESTONES_MOSSY))
              .save(output, basePath + "cobblestone");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_STAIRS),
              template(BlockItemIds.COBBLESTONE_STAIRS)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_COBBLESTONE_STAIRS))
              .save(output, basePath + "cobblestone_stairs");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_SLAB),
              template(BlockItemIds.COBBLESTONE_SLAB)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_COBBLESTONE_SLAB))
              .save(output, basePath + "cobblestone_slabs");
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.MOSSY_COBBLESTONE_WALL),
              template(BlockItemIds.COBBLESTONE_WALL)
        ).category(EnrichingBookCategory.BUILDING)
              .group(Mekanism.rl("mossy_cobble_removal"))
              .unlockedBy(hasCriterion(BlockItemIds.MOSSY_COBBLESTONE_WALL))
              .save(output, basePath + "cobblestone_walls");
    }

    private void addEnrichingDeoxidizingRecipes(RecipeOutput output, String basePath) {
        //Generate baseline recipes from weathering recipe set
        for (Map.Entry<Block, Block> entry : WeatheringCopper.PREVIOUS_BY_BLOCK.get().entrySet()) {
            Block input = entry.getKey();
            Block result = entry.getValue();
            String path = RegistryUtils.getPath(result);
            String group = null;
            //Note: This doesn't need to worry about waxed
            if (path.startsWith("oxidized_")) {
                Mekanism.logger.warn("Found oxidized output for deoxidizing? This doesn't make sense. From: {} to {}", BuiltInRegistries.BLOCK.getKey(input),
                      BuiltInRegistries.BLOCK.getKey(result));
            } else if (path.startsWith("weathered_")) {
                group = "oxidized_to_weathered";
            } else if (path.startsWith("exposed_")) {
                group = "weathered_to_exposed";
            } else {
                group = "exposed_to_unaffected";
            }
            ItemStackToItemStackRecipeBuilder.enriching(
                  IngredientCreatorAccess.item().from(input),
                  new ItemStackTemplate(result.asItem())
            ).category(EnrichingBookCategory.BUILDING)
                  .group(group)
                  .unlockedBy("has_oxidized", has(input.asItem()))
                  .save(output, basePath + path);
        }
    }

    private void addEnrichingDyeRecipes(RecipeOutput output, String basePath) {
        int largeAmount = 4;
        int smallAmount = 2;
        //Red
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.red(), largeAmount, "large_red", BlockItemIds.ROSE_BUSH);
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.red(), smallAmount, "small_red",
              ItemIds.BEETROOT,
              BlockItemIds.POPPY.item(),
              BlockItemIds.RED_TULIP.item()
        );
        //Cyan
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.cyan(), largeAmount, "large_cyan", BlockItemIds.PITCHER_PLANT);
        //Green
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.green(), smallAmount, "green", Tags.Items.CROPS_CACTUS);
        //Light gray
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.lightGray(), smallAmount, "light_gray",
              BlockItemIds.AZURE_BLUET,
              BlockItemIds.OXEYE_DAISY,
              BlockItemIds.WHITE_TULIP
        );
        //Gray
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.gray(), smallAmount, "gray", BlockItemIds.CLOSED_EYEBLOSSOM);
        //Pink
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.pink(), largeAmount, "large_pink", BlockItemIds.PEONY);
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.pink(), smallAmount, "small_pink",
              BlockItemIds.CACTUS_FLOWER,
              BlockItemIds.PINK_TULIP,
              BlockItemIds.PINK_PETALS
        );
        //Lime
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.lime(), smallAmount, "lime", BlockItemIds.SEA_PICKLE);
        //Yellow
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.yellow(), largeAmount, "large_yellow", BlockItemIds.SUNFLOWER);
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.yellow(), smallAmount, "small_yellow",
              BlockItemIds.DANDELION,
              BlockItemIds.GOLDEN_DANDELION,
              BlockItemIds.WILDFLOWERS
        );
        //Light blue
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.lightBlue(), smallAmount, "light_blue", BlockItemIds.BLUE_ORCHID);
        //Magenta
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.magenta(), largeAmount, "large_magenta", BlockItemIds.LILAC);
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.magenta(), smallAmount, "small_magenta", BlockItemIds.ALLIUM);
        //Orange
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.orange(), smallAmount, "orange",
              BlockItemIds.OPEN_EYEBLOSSOM,
              BlockItemIds.ORANGE_TULIP,
              BlockItemIds.TORCHFLOWER
        );
        //Blue
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.blue(), smallAmount, "blue",
              BlockItemIds.CORNFLOWER.item(),
              ItemIds.LAPIS_LAZULI
        );
        //Brown
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.brown(), smallAmount, "brown", Tags.Items.CROPS_COCOA_BEAN);
        //Black
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.black(), smallAmount, "black",
              ItemIds.INK_SAC,
              BlockItemIds.WITHER_ROSE.item()
        );
        //White
        RecipeProviderUtil.addEnrichingDyeRecipe(output, items, basePath, ItemIds.DYE.white(), smallAmount, "white",
              ItemIds.BONE_MEAL,
              BlockItemIds.LILY_OF_THE_VALLEY.item()
        );
    }

    private void addEnrichingEnrichedRecipes(RecipeOutput output, String basePath) {
        //Carbon
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, ItemTags.COALS),
              MekanismItems.ENRICHED_CARBON.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_coal", has(ItemTags.COALS))
              .save(output, basePath + "carbon");
        //Diamond
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GEMS_DIAMOND),
              MekanismItems.ENRICHED_DIAMOND.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
              .save(output, basePath + "diamond");
        //Redstone
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_REDSTONE),
              MekanismItems.ENRICHED_REDSTONE.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
              .save(output, basePath + "redstone");
        //Refined Obsidian
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismItems.ENRICHED_OBSIDIAN.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_refined_obsidian_dust", has(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN))
              .save(output, basePath + "refined_obsidian");
        //Gold
        TagKey<Item> goldDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD);
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, goldDust),
              MekanismItems.ENRICHED_GOLD.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_gold_dust", has(goldDust))
              .save(output, basePath + "gold");
        //Tin
        TagKey<Item> tinDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN);
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(this.items, tinDust),
              MekanismItems.ENRICHED_TIN.asTemplate()
        ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_tin_dust", has(tinDust))
              .save(output, basePath + "tin");
    }
}