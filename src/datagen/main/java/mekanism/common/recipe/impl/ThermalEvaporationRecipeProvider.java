package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

class ThermalEvaporationRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "thermal_evaporation/";
        //Block
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, Tags.Items.INGOTS_COPPER)
              .build(consumer, Mekanism.rl(basePath + "block"));
        //Controller
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, MekanismRecipeProvider.GLASS_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.BUCKET, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.BUCKET, Items.BUCKET)
              .key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS_PANES)
              .build(consumer, Mekanism.rl(basePath + "controller"));
        //Valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_VALVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .build(consumer, Mekanism.rl(basePath + "valve"));
    }
}