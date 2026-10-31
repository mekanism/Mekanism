package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class InductionRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern INDUCTION_CELL_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY));
    private static final RecipePattern INDUCTION_PROVIDER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT));

    InductionRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "induction/";
        addInductionCellRecipes(output, basePath + "cell/");
        addInductionProviderRecipes(output, basePath + "provider/");
        //Casing
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.INDUCTION_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output, basePath + "casing");
        //Port
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.INDUCTION_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, MekanismBlocks.INDUCTION_CASING)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.INDUCTION_CASING))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ELITE))
              .save(output, basePath + "port");
    }

    private void addInductionCellRecipes(RecipeOutput output, String basePath) {
        //Basic needs to be handled slightly differently
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.BASIC_INDUCTION_CELL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM),
                    TripleLine.of(Pattern.ENERGY, Pattern.CONSTANT, Pattern.ENERGY),
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM))
              ).define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.LITHIUM, this.items, MekanismTags.Items.DUSTS_LITHIUM)
              .define(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .group(Mekanism.rl("induction_cell"))
              .unlockedBy("has_casing", has(MekanismBlocks.INDUCTION_CASING))
              .unlockedBy("has_energy_cube", has(MekanismBlocks.BASIC_ENERGY_CUBE))
              .save(output, basePath + "basic");
        addTieredInductionCellRecipe(output, basePath, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_ENERGY_CUBE);
        addTieredInductionCellRecipe(output, basePath, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_ENERGY_CUBE);
        addTieredInductionCellRecipe(output, basePath, MekanismBlocks.ULTIMATE_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_ENERGY_CUBE);
    }

    private void addTieredInductionCellRecipe(RecipeOutput output, String basePath, BlockRegistryObject<?, ?> cell, BlockRegistryObject<?, ?> previousCell,
          BlockRegistryObject<?, ?> energyCube) {
        String tierName = Attribute.getBaseTierNN(cell).getLowerName();
        MekDataShapedRecipeBuilder.shaped(cell)
              .pattern(INDUCTION_CELL_PATTERN)
              .define(Pattern.PREVIOUS, previousCell)
              .define(Pattern.CONSTANT, energyCube)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .group(Mekanism.rl("induction_cell"))
              .unlockedBy("has_casing", has(MekanismBlocks.INDUCTION_CASING))
              .unlockedBy("has_energy_cube", has(energyCube))
              .save(output, basePath + tierName);
    }

    private void addInductionProviderRecipes(RecipeOutput output, String basePath) {
        //Basic needs to be handled slightly differently
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.LITHIUM, this.items, MekanismTags.Items.DUSTS_LITHIUM)
              .define(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .group(Mekanism.rl("induction_provider"))
              .unlockedBy("has_casing", has(MekanismBlocks.INDUCTION_CASING))
              .unlockedBy("has_energy_cube", has(MekanismBlocks.BASIC_ENERGY_CUBE))
              .save(output, basePath + "basic");
        addTieredInductionProviderRecipe(output, basePath, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ADVANCED);
        addTieredInductionProviderRecipe(output, basePath, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ELITE);
        addTieredInductionProviderRecipe(output, basePath, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addTieredInductionProviderRecipe(RecipeOutput output, String basePath, BlockRegistryObject<?, ?> provider, BlockRegistryObject<?, ?> previousProvider,
          BlockRegistryObject<?, ?> energyCube, TagKey<Item> circuitTag) {
        String tierName = Attribute.getBaseTierNN(provider).getLowerName();
        ExtendedShapedRecipeBuilder.shaped(provider)
              .pattern(INDUCTION_PROVIDER_PATTERN)
              .define(Pattern.PREVIOUS, previousProvider)
              .define(Pattern.CONSTANT, energyCube)
              .define(Pattern.CIRCUIT, this.items, circuitTag)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .group(Mekanism.rl("induction_provider"))
              .unlockedBy("has_casing", has(MekanismBlocks.INDUCTION_CASING))
              .unlockedBy("has_energy_cube", has(energyCube))
              .save(output, basePath + tierName);
    }
}