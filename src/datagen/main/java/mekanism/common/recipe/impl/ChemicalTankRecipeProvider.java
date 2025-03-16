package mekanism.common.recipe.impl;

import java.util.Objects;
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
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

class ChemicalTankRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern CHEMICAL_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
          TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "chemical_tank/";
        //Note: For the basic chemical tank, we have to handle the empty slot differently than batching it against our chemical tank pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_CHEMICAL_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismBlocks.BASIC_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ULTIMATE_CHEMICAL_TANK, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredChemicalTank(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> tank, BlockRegistryObject<?, ?> previousTank, TagKey<Item> alloyTag) {
        String tierName = Attribute.getBaseTier(tank).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(CHEMICAL_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.OSMIUM, Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }
}