package mekanism.common.recipe.impl;

import java.util.function.Consumer;
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
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

class UpgradeRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY));

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "upgrade/";
        addUpgradeRecipe(consumer, MekanismItems.ANCHOR_UPGRADE, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(consumer, MekanismItems.ENERGY_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD), basePath);
        addUpgradeRecipe(consumer, MekanismItems.FILTER_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN), basePath);
        addUpgradeRecipe(consumer, MekanismItems.GAS_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON), basePath);
        addUpgradeRecipe(consumer, MekanismItems.MUFFLING_UPGRADE, MekanismTags.Items.DUSTS_STEEL, basePath);
        addUpgradeRecipe(consumer, MekanismItems.SPEED_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM), basePath);
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STONE_GENERATOR_UPGRADE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.BUCKET),
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY))
              ).key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CONSTANT, Items.WATER_BUCKET)
              .key(Pattern.BUCKET, Items.LAVA_BUCKET)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, Mekanism.rl(basePath + getSaveName(MekanismItems.STONE_GENERATOR_UPGRADE)));
    }

    private void addUpgradeRecipe(Consumer<FinishedRecipe> consumer, ItemRegistryObject<ItemUpgrade> upgrade, TagKey<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(upgrade)
              .pattern(UPGRADE_PATTERN)
              .key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CONSTANT, dustTag)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, Mekanism.rl(basePath + getSaveName(upgrade)));
    }

    private String getSaveName(ItemRegistryObject<ItemUpgrade> upgrade) {
        return upgrade.asItem().getUpgradeType(upgrade.getItemStack()).getRawName();
    }
}