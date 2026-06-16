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
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class FluidTankRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern FLUID_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY));

    FluidTankRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "fluid_tank/";
        //Note: For the basic fluid tank, we have to handle the empty slot differently than batching it against our fluid tank pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_FLUID_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .save(consumer, Mekanism.rl(basePath + "basic"));
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.BASIC_FLUID_TANK, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredFluidTank(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> tank, BlockRegistryObject<?, ?> previousTank, TagKey<Item> alloyTag) {
        String tierName = Attribute.getBaseTierNN(tank).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(FLUID_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, this.items, alloyTag)
              .save(consumer, Mekanism.rl(basePath + tierName));
    }
}