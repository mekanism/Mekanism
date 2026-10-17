package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
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
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

class ChemicalTankRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern CHEMICAL_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
          TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY));

    ChemicalTankRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "chemical_tank/";
        TagKey<Item> osmiumIngot = MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM);
        //Note: For the basic chemical tank, we have to handle the empty slot differently than batching it against our chemical tank pattern
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.BASIC_CHEMICAL_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).define(Pattern.OSMIUM, this.items, osmiumIngot)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .category(RecipeCategory.DECORATIONS)//Mirror what vanilla does for chests
              .group(Mekanism.rl("chemical_tank"))
              .unlockedBy("has_osmium_ingot", has(osmiumIngot))
              .save(consumer, Mekanism.rl(basePath + "basic"));
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismBlocks.BASIC_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismBlocks.ADVANCED_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredChemicalTank(consumer, basePath, MekanismBlocks.ULTIMATE_CHEMICAL_TANK, MekanismBlocks.ELITE_CHEMICAL_TANK, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredChemicalTank(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> tank, BlockRegistryObject<?, ?> previousTank, TagKey<Item> alloyTag) {
        String tierName = Attribute.getBaseTierNN(tank).getLowerName();
        MekDataShapedRecipeBuilder.shaped(tank)
              .pattern(CHEMICAL_TANK_PATTERN)
              .define(Pattern.PREVIOUS, previousTank)
              .define(Pattern.OSMIUM, this.items, MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .define(Pattern.ALLOY, this.items, alloyTag)
              .category(RecipeCategory.DECORATIONS)//Mirror what vanilla does for chests
              .group(Mekanism.rl("chemical_tank"))
              .unlockedBy("has_alloy", has(alloyTag))
              .save(consumer, Mekanism.rl(basePath + tierName));
    }
}