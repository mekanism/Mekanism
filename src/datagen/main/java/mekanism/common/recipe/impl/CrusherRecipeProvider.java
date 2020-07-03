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
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.FLINT),
              new ItemStack(Items.GUNPOWDER)
        ).build(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              new ItemStack(Items.SAND)
        ).build(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //TODO: Do we just want to make a clear and red tag for sandstone?
        //Red Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.RED_SANDSTONE),
                    ItemStackIngredient.from(Items.CHISELED_RED_SANDSTONE),
                    ItemStackIngredient.from(Items.CUT_RED_SANDSTONE),
                    ItemStackIngredient.from(Items.SMOOTH_RED_SANDSTONE)
              ),
              new ItemStack(Items.RED_SAND, 2)
        ).build(consumer, Mekanism.rl(basePath + "red_sandstone_to_sand"));
        //Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.SANDSTONE),
                    ItemStackIngredient.from(Items.CHISELED_SANDSTONE),
                    ItemStackIngredient.from(Items.CUT_SANDSTONE),
                    ItemStackIngredient.from(Items.SMOOTH_SANDSTONE)
              ),
              new ItemStack(Items.SAND, 2)
        ).build(consumer, Mekanism.rl(basePath + "sandstone_to_sand"));
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).build(consumer, Mekanism.rl(basePath + "wool_to_string"));
        //Soul Soil -> Soul Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235336_cN_),
              new ItemStack(Blocks.SOUL_SAND)
        ).build(consumer, Mekanism.rl(basePath + "soul_soil_to_soul_sand"));
        //Polished Basalt -> Basalt
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235338_cP_),
              new ItemStack(Blocks.field_235337_cO_)
        ).build(consumer, Mekanism.rl(basePath + "polished_basalt_to_basalt"));
        //Chiseled Nether Bricks -> Nether Bricks Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235393_nG_),
              new ItemStack(Blocks.NETHER_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_nether_bricks_to_nether_bricks"));
        //Nether Bricks Bricks -> Cracked Nether Bricks Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.NETHER_BRICKS),
              new ItemStack(Blocks.field_235394_nH_)
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
              ItemStackIngredient.from(Blocks.field_235410_nt_),
              new ItemStack(Blocks.field_235406_np_)
        ).build(consumer, Mekanism.rl(basePath + "from_polished"));
        //Polished Blackstone Wall -> Blackstone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235392_nF_),
              new ItemStack(Blocks.field_235408_nr_)
        ).build(consumer, Mekanism.rl(basePath + "polished_wall_to_wall"));
        //Polished Blackstone Stairs -> Blackstone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235388_nB_),
              new ItemStack(Blocks.field_235407_nq_)
        ).build(consumer, Mekanism.rl(basePath + "polished_stairs_to_stairs"));
        //Polished Blackstone Slabs -> Blackstone Slabs
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235389_nC_),
              new ItemStack(Blocks.field_235409_ns_)
        ).build(consumer, Mekanism.rl(basePath + "polished_slabs_to_slabs"));
        //Chiseled Polished Blackstone Bricks -> Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235413_nw_),
              new ItemStack(Blocks.field_235411_nu_)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_bricks_to_bricks"));
        //Polished Blackstone Bricks -> Cracked Polished Blackstone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235411_nu_),
              new ItemStack(Blocks.field_235412_nv_)
        ).build(consumer, Mekanism.rl(basePath + "bricks_to_cracked_bricks"));
        //Cracked Polished Blackstone Bricks -> Polished Blackstone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Blocks.field_235412_nv_),
              new ItemStack(Blocks.field_235410_nt_)
        ).build(consumer, Mekanism.rl(basePath + "from_cracked_bricks"));
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