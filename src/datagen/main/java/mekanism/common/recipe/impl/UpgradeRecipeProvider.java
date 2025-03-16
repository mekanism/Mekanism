package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

class UpgradeRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "upgrade/";
        addUpgradeRecipe(consumer, MekanismItems.ANCHOR_UPGRADE, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(consumer, MekanismItems.ENERGY_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD), basePath);
        addUpgradeRecipe(consumer, MekanismItems.FILTER_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN), basePath);
        addUpgradeRecipe(consumer, MekanismItems.CHEMICAL_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON), basePath);
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MUFFLING_UPGRADE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.INGOT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY)))
              .key(Pattern.CONSTANT, ItemTags.WOOL)
              .key(Pattern.INGOT, MekanismTags.Items.MUFFLING_CENTER)
              .build(consumer, Mekanism.rl(basePath + getSaveName(MekanismItems.MUFFLING_UPGRADE)));
        addUpgradeRecipe(consumer, MekanismItems.SPEED_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM), basePath);
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STONE_GENERATOR_UPGRADE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.BUCKET),
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY))
              ).key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CONSTANT, Tags.Items.BUCKETS_WATER)
              .key(Pattern.BUCKET, Tags.Items.BUCKETS_LAVA)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, Mekanism.rl(basePath + getSaveName(MekanismItems.STONE_GENERATOR_UPGRADE)));
    }

    private void addUpgradeRecipe(RecipeOutput consumer, ItemRegistryObject<ItemUpgrade> upgrade, TagKey<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(upgrade)
              .pattern(UPGRADE_PATTERN)
              .key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CONSTANT, dustTag)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, Mekanism.rl(basePath + getSaveName(upgrade)));
    }

    private String getSaveName(ItemRegistryObject<ItemUpgrade> upgrade) {
        return upgrade.value().getUpgradeType(upgrade.asStack()).getSerializedName();
    }
}