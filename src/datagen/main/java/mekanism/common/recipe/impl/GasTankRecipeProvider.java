package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.providers.IItemProvider;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.interfaces.ITypeBlock;
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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;

class GasTankRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern GAS_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
          TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "gas_tank/";
        //Note: For the basic gas tank, we have to handle the empty slot differently than batching it against our gas tank pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_GAS_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredGasTank(consumer, basePath, MekanismBlocks.ADVANCED_GAS_TANK, MekanismBlocks.BASIC_GAS_TANK, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredGasTank(consumer, basePath, MekanismBlocks.ELITE_GAS_TANK, MekanismBlocks.ADVANCED_GAS_TANK, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredGasTank(consumer, basePath, MekanismBlocks.ULTIMATE_GAS_TANK, MekanismBlocks.ELITE_GAS_TANK, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredGasTank(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> tank, IItemProvider previousTank,
          Tag<Item> alloyTag) {
        String tierName = Attribute.getBaseTier(tank.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(GAS_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }
}