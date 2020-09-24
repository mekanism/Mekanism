package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

class EnrichingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "enriching/";
        addEnrichingConversionRecipes(consumer, basePath + "conversion/");
        addEnrichingDyeRecipes(consumer, basePath + "dye/");
        addEnrichingEnrichedRecipes(consumer, basePath + "enriched/");
        //Charcoal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL),
              new ItemStack(Items.CHARCOAL)
        ).build(consumer, Mekanism.rl(basePath + "charcoal"));
        //Charcoal dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_WOOD, 8),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Clay ball
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.CLAY),
              new ItemStack(Items.CLAY_BALL, 4)
        ).build(consumer, Mekanism.rl(basePath + "clay_ball"));
        //Glowstone dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST, 4)
        ).build(consumer, Mekanism.rl(basePath + "glowstone_dust"));
        //HDPE Sheet
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismItems.HDPE_PELLET, 3),
              MekanismItems.HDPE_SHEET.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "hdpe_sheet"));
        //Salt
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismBlocks.SALT_BLOCK),
              MekanismItems.SALT.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "salt"));
    }

    private void addEnrichingConversionRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addEnrichingStoneConversionRecipes(consumer, basePath + "stone/");
        addEnrichingBlackstoneConversionRecipes(consumer, basePath + "blackstone/");
        addEnrichingQuartzRecipes(consumer, basePath + "quartz/");
        addEnrichingMossyConversionRecipes(consumer, basePath + "mossy/");
        //Gravel -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              new ItemStack(Items.FLINT)
        ).build(consumer, Mekanism.rl(basePath + "gravel_to_flint"));
        //Gunpowder -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GUNPOWDER),
              new ItemStack(Items.FLINT)
        ).build(consumer, Mekanism.rl(basePath + "gunpowder_to_flint"));
        //Sand -> gravel
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.SAND),
              new ItemStack(Blocks.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "sand_to_gravel"));
        //Soul Sand -> soul soil
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.SOUL_SAND),
              new ItemStack(Blocks.SOUL_SOIL)
        ).build(consumer, Mekanism.rl(basePath + "soul_sand_to_soul_soil"));
        //Sulfur -> gunpowder
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              new ItemStack(Items.GUNPOWDER)
        ).build(consumer, Mekanism.rl(basePath + "sulfur_to_gunpowder"));
        //Obsidian -> obsidian dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.OBSIDIAN),
              MekanismItems.OBSIDIAN_DUST.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "obsidian_to_obsidian_dust"));
        //Basalt -> polished basalt
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.BASALT),
              new ItemStack(Blocks.POLISHED_BASALT)
        ).build(consumer, Mekanism.rl(basePath + "basalt_to_polished_basalt"));
        //Cracked nether bricks -> nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.CRACKED_NETHER_BRICKS),
              new ItemStack(Blocks.NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "cracked_nether_bricks_to_nether_bricks"));
        //Nether bricks -> chiseled nether bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.NETHER_BRICKS),
              new ItemStack(Blocks.CHISELED_NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "nether_bricks_to_chiseled_nether_bricks"));
    }

    private void addEnrichingStoneConversionRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Stone -> cracked stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.STONE),
              new ItemStack(Blocks.CRACKED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "to_cracked_bricks"));
        //Cracked stone bricks -> stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.CRACKED_STONE_BRICKS),
              new ItemStack(Blocks.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "cracked_bricks_to_bricks"));
        //Stone bricks -> chiseled stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.STONE_BRICKS),
              new ItemStack(Blocks.CHISELED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_chiseled_bricks"));
    }

    private void addEnrichingBlackstoneConversionRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Polished blackstone -> cracked polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE),
              new ItemStack(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "to_cracked_bricks"));
        //Cracked polished blackstone bricks -> polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "cracked_bricks_to_bricks"));
        //Polished blackstone bricks -> chiseled polished blackstone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.CHISELED_POLISHED_BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_chiseled_bricks"));
    }

    private void addEnrichingQuartzRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Quartz Block -> Quartz Item
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_QUARTZ),
                    ItemStackIngredient.from(Blocks.QUARTZ_BRICKS),
                    ItemStackIngredient.from(Blocks.CHISELED_QUARTZ_BLOCK),
                    ItemStackIngredient.from(Blocks.QUARTZ_PILLAR)
              ),
              new ItemStack(Items.QUARTZ, 4)
        ).build(consumer, Mekanism.rl(basePath + "to_item"));
        //Smooth Quartz Block -> Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.SMOOTH_QUARTZ),
              new ItemStack(Blocks.QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "from_smooth_quartz"));
        //Smooth Quartz Slab -> Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.SMOOTH_QUARTZ_SLAB),
              new ItemStack(Blocks.QUARTZ_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "smooth_slab_to_slab"));
        //Smooth Quartz Stairs -> Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.SMOOTH_QUARTZ_STAIRS),
              new ItemStack(Blocks.QUARTZ_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_stairs"));
    }

    private void addEnrichingMossyConversionRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Mossy Stone Brick -> Stone Brick recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_STONE_BRICKS),
              new ItemStack(Blocks.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "stone_bricks"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_STONE_BRICK_STAIRS),
              new ItemStack(Blocks.STONE_BRICK_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_STONE_BRICK_SLAB),
              new ItemStack(Blocks.STONE_BRICK_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_slabs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_STONE_BRICK_WALL),
              new ItemStack(Blocks.STONE_BRICK_WALL)
        ).build(consumer, Mekanism.rl(basePath + "stone_brick_walls"));
        //Mossy Cobblestone -> Cobblestone recipes
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_COBBLESTONE),
              new ItemStack(Blocks.COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_COBBLESTONE_STAIRS),
              new ItemStack(Blocks.COBBLESTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_COBBLESTONE_SLAB),
              new ItemStack(Blocks.COBBLESTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_slabs"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.MOSSY_COBBLESTONE_WALL),
              new ItemStack(Blocks.COBBLESTONE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_walls"));
    }

    private void addEnrichingDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.WITHER_ROSE),
              new ItemStack(Items.BLACK_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "black"));
        //Blue
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.CORNFLOWER),
              new ItemStack(Items.BLUE_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "blue"));
        //Green
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.CACTUS),
              new ItemStack(Items.GREEN_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "green"));
        //Magenta
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.LILAC),
              new ItemStack(Items.MAGENTA_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.ALLIUM),
              new ItemStack(Items.MAGENTA_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Pink
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.PEONY),
              new ItemStack(Items.PINK_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.PINK_TULIP),
              new ItemStack(Items.PINK_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "small_pink"));
        //Red
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.ROSE_BUSH),
              new ItemStack(Items.RED_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Blocks.RED_TULIP),
                    ItemStackIngredient.from(Blocks.POPPY)
              ),
              new ItemStack(Items.RED_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "small_red"));
        //Yellow
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.SUNFLOWER),
              new ItemStack(Items.YELLOW_DYE, 4)
        ).build(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.DANDELION),
              new ItemStack(Items.YELLOW_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.BLUE_ORCHID),
              new ItemStack(Items.LIGHT_BLUE_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Light gray
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Blocks.OXEYE_DAISY),
                    ItemStackIngredient.from(Blocks.AZURE_BLUET),
                    ItemStackIngredient.from(Blocks.WHITE_TULIP)
              ),
              new ItemStack(Items.LIGHT_GRAY_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "light_gray"));
        //Orange
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.ORANGE_TULIP),
              new ItemStack(Items.ORANGE_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "orange"));
        //White
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Blocks.LILY_OF_THE_VALLEY),
              new ItemStack(Items.WHITE_DYE, 2)
        ).build(consumer, Mekanism.rl(basePath + "white"));
    }

    private void addEnrichingEnrichedRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Carbon
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(ItemTags.COALS),
              MekanismItems.ENRICHED_CARBON.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "carbon"));
        //Diamond
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GEMS_DIAMOND),
              MekanismItems.ENRICHED_DIAMOND.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "diamond"));
        //Redstone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE),
              MekanismItems.ENRICHED_REDSTONE.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "redstone"));
        //Refined Obsidian
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismItems.ENRICHED_OBSIDIAN.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "refined_obsidian"));
        //Gold
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD)),
              MekanismItems.ENRICHED_GOLD.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "gold"));
        //Tin
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN)),
              MekanismItems.ENRICHED_TIN.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "tin"));
    }
}