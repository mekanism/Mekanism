package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class UpgradeRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY));

    private final HolderGetter<Upgrade> upgrades;

    UpgradeRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals, HolderGetter<Upgrade> upgrades) {
        super(items, fluids, chemicals);
        this.upgrades = upgrades;
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "upgrade/";
        addUpgradeRecipe(output, UpgradeIds.ANCHOR, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(output, UpgradeIds.ENERGY, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD), basePath);
        addUpgradeRecipe(output, UpgradeIds.FILTER, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN), basePath);
        addUpgradeRecipe(output, UpgradeIds.CHEMICAL, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.IRON), basePath);
        ExtendedShapedRecipeBuilder.shaped(IUpgradeHelper.INSTANCE.asTemplate(upgrades, UpgradeIds.MUFFLING))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.INGOT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY)))
              .define(Pattern.CONSTANT, this.items, ItemTags.WOOL)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.MUFFLING_CENTER)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_wool", has(ItemTags.WOOL))
              .save(output, basePath + getSaveName(UpgradeIds.MUFFLING));
        addUpgradeRecipe(output, UpgradeIds.SPEED, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.OSMIUM), basePath);
        ExtendedShapedRecipeBuilder.shaped(IUpgradeHelper.INSTANCE.asTemplate(upgrades, UpgradeIds.STONE_GENERATOR))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.BUCKET),
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY))
              ).define(MekanismRecipeProvider.GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CONSTANT, this.items, Tags.Items.BUCKETS_WATER)
              .define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_LAVA)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_water_bucket", has(Tags.Items.BUCKETS_WATER))
              .unlockedBy("has_lava_bucket", has(Tags.Items.BUCKETS_LAVA))
              .save(output, basePath + getSaveName(UpgradeIds.STONE_GENERATOR));
    }

    private void addUpgradeRecipe(RecipeOutput output, ResourceKey<Upgrade> upgrade, TagKey<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shaped(IUpgradeHelper.INSTANCE.asTemplate(upgrades, upgrade))
              .pattern(UPGRADE_PATTERN)
              .define(MekanismRecipeProvider.GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CONSTANT, this.items, dustTag)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .save(output, basePath + getSaveName(upgrade));
    }

    private String getSaveName(ResourceKey<Upgrade> upgrade) {
        return upgrade.identifier().getPath();
    }
}