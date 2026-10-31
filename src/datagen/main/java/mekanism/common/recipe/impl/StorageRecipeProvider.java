package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
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
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.ItemIds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class StorageRecipeProvider extends BaseSubRecipeProvider {

    StorageRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        addNuggetRecipes(output);
        addStorageBlockRecipes(output);
    }

    private void addNuggetRecipes(RecipeOutput output) {
        String basePath = "nuggets/";
        addNuggetRecipe(output, MekanismItems.BRONZE_NUGGET, MekanismItems.BRONZE_INGOT, basePath, "bronze");
        addNuggetRecipe(output, MekanismItems.REFINED_GLOWSTONE_NUGGET, MekanismItems.REFINED_GLOWSTONE_INGOT, basePath, "refined_glowstone");
        addNuggetRecipe(output, MekanismItems.REFINED_OBSIDIAN_NUGGET, MekanismItems.REFINED_OBSIDIAN_INGOT, basePath, "refined_obsidian");
        addNuggetRecipe(output, MekanismItems.STEEL_NUGGET, MekanismItems.STEEL_INGOT, basePath, "steel");
    }

    private void addNuggetRecipe(RecipeOutput output, Holder<Item> nugget, Holder<Item> ingot, String basePath, String name) {
        ExtendedShapelessRecipeBuilder.shapeless(nugget, 9)
              .requires(ingot)
              .unlockedBy(hasCriterion(nugget))
              .save(output, basePath + name);
    }

    private void addStorageBlockRecipes(RecipeOutput output) {
        String basePath = "storage_blocks/";
        addStorageBlockRecipe(output, MekanismBlocks.BRONZE_BLOCK, MekanismItems.BRONZE_INGOT, MekanismTags.Items.INGOTS_BRONZE, basePath);
        addStorageBlockRecipe(output, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismItems.REFINED_GLOWSTONE_INGOT, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, basePath);
        addStorageBlockRecipe(output, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismItems.REFINED_OBSIDIAN_INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, basePath);
        addStorageBlockRecipe(output, MekanismBlocks.STEEL_BLOCK, MekanismItems.STEEL_INGOT, MekanismTags.Items.INGOTS_STEEL, basePath);
        addStorageBlockRecipe(output, MekanismBlocks.FLUORITE_BLOCK, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, basePath);
        //Bio Fuel
        addStorageBlockRecipe(output, MekanismBlocks.BIO_FUEL_BLOCK, MekanismItems.BIO_FUEL, MekanismTags.Items.FUELS_BIO, basePath, "bio_fuel");
        //Charcoal
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.CHARCOAL_BLOCK)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .define(Pattern.CONSTANT, this.items, ItemIds.CHARCOAL)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy(hasCriterion(ItemIds.CHARCOAL))
              .save(output, basePath + BlockResourceInfo.CHARCOAL.getRegistrySuffix());
        //Salt
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SALT_BLOCK)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_SALT)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_salt", has(MekanismTags.Items.DUSTS_SALT))
              .save(output, basePath + "salt");
    }

    private void addStorageBlockRecipe(RecipeOutput output, BlockRegistryObject<?, ?> block, Holder<Item> ingot, TagKey<Item> ingotTag, String basePath) {
        addStorageBlockRecipe(output, block, ingot, ingotTag, basePath, block.getId().getPath().substring(6));
    }

    private void addStorageBlockRecipe(RecipeOutput output, BlockRegistryObject<?, ?> block, Holder<Item> ingot, TagKey<Item> ingotTag,
          String basePath, String suffix) {
        ExtendedShapedRecipeBuilder.shaped(block)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .define(Pattern.PREVIOUS, ingot)
              .define(Pattern.CONSTANT, this.items, ingotTag)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy(hasCriterion(ingot))
              .save(output, basePath + suffix);
    }
}