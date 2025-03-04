package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

class EnergyCubeRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern ENERGY_CUBE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "energy_cube/";
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.STEEL_CASING, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM), MekanismTags.Items.ALLOYS_INFUSED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredEnergyCube(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> energyCube, BlockRegistryObject<?, ?> previousEnergyCube,
          TagKey<Item> ingotTag, TagKey<Item> alloyTag) {
        String tierName = Attribute.getBaseTier(energyCube).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(energyCube)
              .pattern(ENERGY_CUBE_PATTERN)
              .key(Pattern.PREVIOUS, previousEnergyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }
}