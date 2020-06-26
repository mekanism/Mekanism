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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

class UpgradeRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY));

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "upgrade/";
        addUpgradeRecipe(consumer, MekanismItems.ANCHOR_UPGRADE, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(consumer, MekanismItems.ENERGY_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD), basePath);
        addUpgradeRecipe(consumer, MekanismItems.FILTER_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.TIN), basePath);
        addUpgradeRecipe(consumer, MekanismItems.GAS_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON), basePath);
        addUpgradeRecipe(consumer, MekanismItems.MUFFLING_UPGRADE, MekanismTags.Items.DUSTS_STEEL, basePath);
        addUpgradeRecipe(consumer, MekanismItems.SPEED_UPGRADE, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM), basePath);
    }

    private void addUpgradeRecipe(Consumer<IFinishedRecipe> consumer, ItemRegistryObject<ItemUpgrade> upgrade, ITag<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(upgrade)
              .pattern(UPGRADE_PATTERN)
              .key(MekanismRecipeProvider.GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CONSTANT, dustTag)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, Mekanism.rl(basePath + upgrade.getItem().getUpgradeType(upgrade.getItemStack()).getRawName()));
    }
}