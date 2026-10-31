package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class TierInstallerRecipeProvider extends BaseSubRecipeProvider {

    TierInstallerRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "tier_installer/";
        addTierInstallerRecipe(output, basePath, MekanismItems.BASIC_TIER_INSTALLER, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC);
        addTierInstallerRecipe(output, basePath, MekanismItems.ADVANCED_TIER_INSTALLER, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM), MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED);
        addTierInstallerRecipe(output, basePath, MekanismItems.ELITE_TIER_INSTALLER, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE);
        addTierInstallerRecipe(output, basePath, MekanismItems.ULTIMATE_TIER_INSTALLER, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addTierInstallerRecipe(RecipeOutput output, String basePath, ItemRegistryObject<ItemTierInstaller> tierInstaller, TagKey<Item> ingotTag,
          TagKey<Item> alloyTag, TagKey<Item> circuitTag) {
        ExtendedShapedRecipeBuilder.shaped(tierInstaller)
              .pattern(MekanismRecipeProvider.TIER_PATTERN)
              .define(Pattern.PREVIOUS, this.items, ItemTags.PLANKS)
              .define(Pattern.CIRCUIT, this.items, circuitTag)
              .define(Pattern.INGOT, this.items, ingotTag)
              .define(Pattern.ALLOY, this.items, alloyTag)
              .category(RecipeCategory.TOOLS)
              .group(Mekanism.rl("tier_installer"))
              .unlockedBy("has_circuit", has(circuitTag))
              .save(output, basePath + tierInstaller.value().getToTier().getLowerName());
    }
}