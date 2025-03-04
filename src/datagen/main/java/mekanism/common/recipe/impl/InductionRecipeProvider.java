package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

class InductionRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern INDUCTION_CELL_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY));
    private static final RecipePattern INDUCTION_PROVIDER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "induction/";
        addInductionCellRecipes(consumer, basePath + "cell/");
        addInductionProviderRecipes(consumer, basePath + "provider/");
        //Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUCTION_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .build(consumer, Mekanism.rl(basePath + "casing"));
        //Port
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUCTION_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.INDUCTION_CASING)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .build(consumer, Mekanism.rl(basePath + "port"));
    }

    private void addInductionCellRecipes(RecipeOutput consumer, String basePath) {
        //Basic needs to be handled slightly differently
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_INDUCTION_CELL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM),
                    TripleLine.of(Pattern.ENERGY, Pattern.CONSTANT, Pattern.ENERGY),
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.LITHIUM, MekanismTags.Items.DUSTS_LITHIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_ENERGY_CUBE);
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_ENERGY_CUBE);
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_ENERGY_CUBE);
    }

    private void addTieredInductionCellRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> cell, BlockRegistryObject<?, ?> previousCell,
          BlockRegistryObject<?, ?> energyCube) {
        String tierName = Attribute.getBaseTier(cell).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(cell)
              .pattern(INDUCTION_CELL_PATTERN)
              .key(Pattern.PREVIOUS, previousCell)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }

    private void addInductionProviderRecipes(RecipeOutput consumer, String basePath) {
        //Basic needs to be handled slightly differently
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.LITHIUM, MekanismTags.Items.DUSTS_LITHIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ADVANCED);
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ELITE);
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addTieredInductionProviderRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> provider, BlockRegistryObject<?, ?> previousProvider,
          BlockRegistryObject<?, ?> energyCube, TagKey<Item> circuitTag) {
        String tierName = Attribute.getBaseTier(provider).getLowerName();
        ExtendedShapedRecipeBuilder.shapedRecipe(provider)
              .pattern(INDUCTION_PROVIDER_PATTERN)
              .key(Pattern.PREVIOUS, previousProvider)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.CIRCUIT, circuitTag)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }
}