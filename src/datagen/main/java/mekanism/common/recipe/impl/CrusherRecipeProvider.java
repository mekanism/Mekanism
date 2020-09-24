package mekanism.common.recipe.impl;

import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;

class CrusherRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "crushing/";
        addCrusherBioFuelRecipes(consumer, basePath + "biofuel/");
        addCrusherStoneRecipes(consumer, basePath + "stone/");
        addCrusherBlackstoneRecipes(consumer, basePath + "blackstone/");
        addCrusherQuartzRecipes(consumer, basePath + "quartz/");
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.FLINT),
              new ItemStack(Items.GUNPOWDER)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              new ItemStack(Blocks.SAND)
        ).build(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //TODO: Do we just want to make a clear and red tag for sandstone?
        //Red Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Blocks.RED_SANDSTONE),
                    ItemStackIngredient.from(Blocks.CHISELED_RED_SANDSTONE),
                    ItemStackIngredient.from(Blocks.CUT_RED_SANDSTONE),
                    ItemStackIngredient.from(Blocks.SMOOTH_RED_SANDSTONE)
              ),
              new ItemStack(Blocks.RED_SAND, 2)
        ).build(consumer, Mekanism.rl(basePath + "red_sandstone_to_sand"));
        //Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Blocks.SANDSTONE),
                    ItemStackIngredient.from(Blocks.CHISELED_SANDSTONE),
                    ItemStackIngredient.from(Blocks.CUT_SANDSTONE),
                    ItemStackIngredient.from(Blocks.SMOOTH_SANDSTONE)
              ),
              new ItemStack(Blocks.SAND, 2)
        ).build(consumer, Mekanism.rl(basePath + "sandstone_to_sand"));
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).build(consumer, Mekanism.rl(basePath + "wool_to_string"));
        //Soul Soil -> Soul Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.SOUL_SOIL),
              new ItemStack(Blocks.SOUL_SAND)
        ).build(consumer, Mekanism.rl(basePath + "soul_soil_to_soul_sand"));
        //Polished Basalt -> Basalt
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BASALT),
              new ItemStack(Blocks.BASALT)
        ).build(consumer, Mekanism.rl(basePath + "polished_basalt_to_basalt"));
        //Chiseled Nether Bricks -> Nether Bricks Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CHISELED_NETHER_BRICKS),
              new ItemStack(Blocks.NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_nether_bricks_to_nether_bricks"));
        //Nether Bricks Bricks -> Cracked Nether Bricks Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.NETHER_BRICKS),
              new ItemStack(Blocks.CRACKED_NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "nether_bricks_to_cracked_nether_bricks"));
    }

    private void addCrusherStoneRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.STONE),
              new ItemStack(Blocks.COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "to_cobblestone"));
        //Stone Stairs -> Cobblestone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.STONE_STAIRS),
              new ItemStack(Blocks.COBBLESTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_cobblestone_stairs"));
        //Stone Slabs -> Cobblestone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.STONE_SLAB),
              new ItemStack(Blocks.COBBLESTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slabs_to_cobblestone_slabs"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CHISELED_STONE_BRICKS),
              new ItemStack(Blocks.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.STONE_BRICKS),
              new ItemStack(Blocks.CRACKED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CRACKED_STONE_BRICKS),
              new ItemStack(Blocks.STONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherBlackstoneRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Polished Blackstone -> Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE),
              new ItemStack(Blocks.BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Blackstone Wall -> Blackstone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE_WALL),
              new ItemStack(Blocks.BLACKSTONE_WALL)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_wall"));
        //Polished Blackstone Stairs -> Blackstone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE_STAIRS),
              new ItemStack(Blocks.BLACKSTONE_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_stairs"));
        //Polished Blackstone Slabs -> Blackstone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE_SLAB),
              new ItemStack(Blocks.BLACKSTONE_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_slabs"));
        //Chiseled Polished Blackstone Bricks -> Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CHISELED_POLISHED_BLACKSTONE),
              new ItemStack(Blocks.POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Polished Blackstone Bricks -> Cracked Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Polished Blackstone Bricks -> Polished Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS),
              new ItemStack(Blocks.POLISHED_BLACKSTONE)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
    }

    private void addCrusherQuartzRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Quartz Block -> Smooth Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_QUARTZ),
              new ItemStack(Blocks.SMOOTH_QUARTZ)
        ).build(consumer, Mekanism.rl(basePath + "to_smooth_quartz"));
        //Quartz Slab -> Smooth Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.QUARTZ_SLAB),
              new ItemStack(Blocks.SMOOTH_QUARTZ_SLAB)
        ).build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Quartz Stairs -> Smooth Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.QUARTZ_STAIRS),
              new ItemStack(Blocks.SMOOTH_QUARTZ_STAIRS)
        ).build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Smooth Quartz Block -> Quartz Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.SMOOTH_QUARTZ),
              new ItemStack(Blocks.QUARTZ_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "smooth_to_bricks"));
        //Quartz Bricks -> Chiseled Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.QUARTZ_BRICKS),
              new ItemStack(Blocks.CHISELED_QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_chiseled"));
        //Chiseled Quartz Block -> Quartz Pillar
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.CHISELED_QUARTZ_BLOCK),
              new ItemStack(Blocks.QUARTZ_PILLAR)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_to_pillar"));
        //Quartz Pillar -> Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.QUARTZ_PILLAR),
              new ItemStack(Blocks.QUARTZ_BLOCK)
        ).build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherBioFuelRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Generate baseline recipes from Composter recipe set
        for (Entry<IItemProvider> chance : ComposterBlock.CHANCES.object2FloatEntrySet()) {
            ItemStackToItemStackRecipeBuilder.crushing(
                  ItemStackIngredient.from(chance.getKey().asItem()),
                  MekanismItems.BIO_FUEL.getItemStack(Math.round(chance.getFloatValue() * 8))
            ).build(consumer, Mekanism.rl(basePath + chance.getKey().asItem().toString()));
        }
    }
}