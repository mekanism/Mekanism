package mekanism.common.recipe.impl;

import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismItems;
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
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHISELED_STONE_BRICKS),
              new ItemStack(Items.STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "chiseled_stone_bricks_to_stone_bricks"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.GRAVEL)
        ).build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CRACKED_STONE_BRICKS),
              new ItemStack(Items.STONE)
        ).build(consumer, Mekanism.rl(basePath + "cracked_stone_bricks_to_stone"));
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
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.STONE_BRICKS),
              new ItemStack(Items.CRACKED_STONE_BRICKS)
        ).build(consumer, Mekanism.rl(basePath + "stone_bricks_to_cracked_stone_bricks"));
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.STONE),
              new ItemStack(Items.COBBLESTONE)
        ).build(consumer, Mekanism.rl(basePath + "stone_to_cobblestone"));
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).build(consumer, Mekanism.rl(basePath + "wool_to_string"));
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