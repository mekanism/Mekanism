package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.upgrade.Upgrade;
import mekanism.api.upgrade.UpgradeIds;
import mekanism.common.Mekanism;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "upgrade/";
        addUpgradeRecipe(consumer, UpgradeIds.ANCHOR, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(consumer, UpgradeIds.ENERGY, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD), basePath);
        addUpgradeRecipe(consumer, UpgradeIds.FILTER, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.TIN), basePath);
        addUpgradeRecipe(consumer, UpgradeIds.CHEMICAL, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.IRON), basePath);
        ExtendedShapedRecipeBuilder.shapedRecipe(UpgradeUtils.getTemplate(upgrades, UpgradeIds.MUFFLING, 1))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.INGOT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY)))
              .key(Pattern.CONSTANT, this.items, ItemTags.WOOL)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.MUFFLING_CENTER)
              .save(consumer, Mekanism.rl(basePath + getSaveName(UpgradeIds.MUFFLING)));
        addUpgradeRecipe(consumer, UpgradeIds.SPEED, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.OSMIUM), basePath);
        ExtendedShapedRecipeBuilder.shapedRecipe(UpgradeUtils.getTemplate(upgrades, UpgradeIds.STONE_GENERATOR, 1))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.BUCKET),
                    TripleLine.of(Pattern.EMPTY, MekanismRecipeProvider.GLASS_CHAR, Pattern.EMPTY))
              ).key(MekanismRecipeProvider.GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CONSTANT, this.items, Tags.Items.BUCKETS_WATER)
              .key(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_LAVA)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(consumer, Mekanism.rl(basePath + getSaveName(UpgradeIds.STONE_GENERATOR)));
    }

    private void addUpgradeRecipe(RecipeOutput consumer, ResourceKey<Upgrade> upgrade, TagKey<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(UpgradeUtils.getTemplate(upgrades, upgrade, 1))
              .pattern(UPGRADE_PATTERN)
              .key(MekanismRecipeProvider.GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CONSTANT, this.items, dustTag)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(consumer, Mekanism.rl(basePath + getSaveName(upgrade)));
    }

    private String getSaveName(ResourceKey<Upgrade> upgrade) {
        return upgrade.identifier().getPath();
    }
}