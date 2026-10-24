package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class ThermalEvaporationRecipeProvider extends BaseSubRecipeProvider {

    ThermalEvaporationRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "thermal_evaporation/";
        //Block
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_COPPER)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(consumer, Mekanism.rl(basePath + "block"));
        //Controller
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, MekanismRecipeProvider.GLASS_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.BUCKET, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_EMPTY)
              .define(MekanismRecipeProvider.GLASS_CHAR, this.items, Tags.BlockItems.GLASS_PANES)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.THERMAL_EVAPORATION_BLOCK))
              .save(consumer, Mekanism.rl(basePath + "controller"));
        //Valve
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.THERMAL_EVAPORATION_VALVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.THERMAL_EVAPORATION_BLOCK))
              .save(consumer, Mekanism.rl(basePath + "valve"));
    }
}