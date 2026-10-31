package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
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
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class EnergyCubeRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern ENERGY_CUBE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY));

    EnergyCubeRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "energy_cube/";
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.STEEL_CASING, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC,
              hasCriterion(MekanismItems.ENERGY_TABLET));
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM), MekanismTags.Items.ALLOYS_INFUSED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredEnergyCube(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> energyCube, BlockRegistryObject<?, ?> previousEnergyCube,
          TagKey<Item> ingotTag, TagKey<Item> alloyTag) {
        addTieredEnergyCube(consumer, basePath, energyCube, previousEnergyCube, ingotTag, alloyTag, new RecipeCriterion("has_alloy", has(alloyTag)));
    }

    private void addTieredEnergyCube(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> energyCube, BlockRegistryObject<?, ?> previousEnergyCube,
          TagKey<Item> ingotTag, TagKey<Item> alloyTag, RecipeCriterion recipeCriterion) {
        String tierName = Attribute.getBaseTierNN(energyCube).getLowerName();
        MekDataShapedRecipeBuilder.shaped(energyCube)
              .pattern(ENERGY_CUBE_PATTERN)
              .define(Pattern.PREVIOUS, previousEnergyCube)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.INGOT, this.items, ingotTag)
              .define(Pattern.ALLOY, this.items, alloyTag)
              .category(RecipeCategory.DECORATIONS)//Mirror what vanilla does for chests
              .group(Mekanism.rl("energy_cube"))
              .unlockedBy(recipeCriterion)
              .save(consumer, basePath + tierName);
    }
}