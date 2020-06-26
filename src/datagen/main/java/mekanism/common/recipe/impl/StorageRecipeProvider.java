package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.providers.IItemProvider;
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
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;

class StorageRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        addNuggetRecipes(consumer);
        addStorageBlockRecipes(consumer);
    }

    private void addNuggetRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "nuggets/";
        addNuggetRecipe(consumer, MekanismItems.BRONZE_NUGGET, MekanismTags.Items.INGOTS_BRONZE, basePath, "bronze");
        addNuggetRecipe(consumer, MekanismItems.REFINED_GLOWSTONE_NUGGET, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, basePath, "refined_glowstone");
        addNuggetRecipe(consumer, MekanismItems.REFINED_OBSIDIAN_NUGGET, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, basePath, "refined_obsidian");
        addNuggetRecipe(consumer, MekanismItems.STEEL_NUGGET, MekanismTags.Items.INGOTS_STEEL, basePath, "steel");
    }

    private void addNuggetRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider nugget, ITag<Item> ingotTag, String basePath, String name) {
        ExtendedShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
              .addIngredient(ingotTag)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addStorageBlockRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "storage_blocks/";
        addStorageBlockRecipe(consumer, MekanismBlocks.BRONZE_BLOCK, MekanismTags.Items.INGOTS_BRONZE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.STEEL_BLOCK, MekanismTags.Items.INGOTS_STEEL, basePath);
        //Charcoal
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARCOAL_BLOCK)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, Items.CHARCOAL)
              .build(consumer, Mekanism.rl(basePath + MekanismBlocks.CHARCOAL_BLOCK.getBlock().getResourceInfo().getRegistrySuffix()));
        //Salt
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SALT_BLOCK)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.DUSTS_SALT)
              .build(consumer, Mekanism.rl(basePath + "salt"));
    }

    private void addStorageBlockRecipe(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<BlockResource, ?> block, ITag<Item> ingotTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(block)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, ingotTag)
              .build(consumer, Mekanism.rl(basePath + block.getBlock().getResourceInfo().getRegistrySuffix()));
    }
}