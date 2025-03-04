package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

class StorageRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        addNuggetRecipes(consumer);
        addStorageBlockRecipes(consumer);
    }

    private void addNuggetRecipes(RecipeOutput consumer) {
        String basePath = "nuggets/";
        addNuggetRecipe(consumer, MekanismItems.BRONZE_NUGGET, MekanismItems.BRONZE_INGOT, basePath, "bronze");
        addNuggetRecipe(consumer, MekanismItems.REFINED_GLOWSTONE_NUGGET, MekanismItems.REFINED_GLOWSTONE_INGOT, basePath, "refined_glowstone");
        addNuggetRecipe(consumer, MekanismItems.REFINED_OBSIDIAN_NUGGET, MekanismItems.REFINED_OBSIDIAN_INGOT, basePath, "refined_obsidian");
        addNuggetRecipe(consumer, MekanismItems.STEEL_NUGGET, MekanismItems.STEEL_INGOT, basePath, "steel");
    }

    private void addNuggetRecipe(RecipeOutput consumer, Holder<Item> nugget, Holder<Item> ingot, String basePath, String name) {
        ExtendedShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
              .addIngredient(ingot)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addStorageBlockRecipes(RecipeOutput consumer) {
        String basePath = "storage_blocks/";
        addStorageBlockRecipe(consumer, MekanismBlocks.BRONZE_BLOCK, MekanismItems.BRONZE_INGOT, MekanismTags.Items.INGOTS_BRONZE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismItems.REFINED_GLOWSTONE_INGOT, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismItems.REFINED_OBSIDIAN_INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.STEEL_BLOCK, MekanismItems.STEEL_INGOT, MekanismTags.Items.INGOTS_STEEL, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.FLUORITE_BLOCK, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, basePath);
        //Bio Fuel
        addStorageBlockRecipe(consumer, MekanismBlocks.BIO_FUEL_BLOCK, MekanismItems.BIO_FUEL, MekanismTags.Items.FUELS_BIO, basePath, "bio_fuel");
        //Charcoal
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARCOAL_BLOCK)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, Items.CHARCOAL)
              .build(consumer, Mekanism.rl(basePath + BlockResourceInfo.CHARCOAL.getRegistrySuffix()));
        //Salt
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SALT_BLOCK)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.DUSTS_SALT)
              .build(consumer, Mekanism.rl(basePath + "salt"));
    }

    private void addStorageBlockRecipe(RecipeOutput consumer, BlockRegistryObject<BlockResource, ?> block, Holder<Item> ingot, TagKey<Item> ingotTag,
          String basePath) {
        addStorageBlockRecipe(consumer, block, ingot, ingotTag, basePath, block.value().getResourceInfo().getRegistrySuffix());
    }

    private void addStorageBlockRecipe(RecipeOutput consumer, BlockRegistryObject<?, ?> block, Holder<Item> ingot, TagKey<Item> ingotTag,
          String basePath, String suffix) {
        ExtendedShapedRecipeBuilder.shapedRecipe(block)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .key(Pattern.PREVIOUS, ingot)
              .key(Pattern.CONSTANT, ingotTag)
              .build(consumer, Mekanism.rl(basePath + suffix));
    }
}