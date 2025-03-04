package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;

class FactoryRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "factory/";
        String basicPath = basePath + "basic/";
        String advancedPath = basePath + "advanced/";
        String elitePath = basePath + "elite/";
        String ultimatePath = basePath + "ultimate/";
        TagKey<Item> osmiumIngot = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM);
        for (FactoryType type : EnumUtils.FACTORY_TYPES) {
            BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> basicFactory = MekanismBlocks.getFactory(FactoryTier.BASIC, type);
            BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> advancedFactory = MekanismBlocks.getFactory(FactoryTier.ADVANCED, type);
            BlockRegistryObject<BlockFactory<?>, ItemBlockFactory> eliteFactory = MekanismBlocks.getFactory(FactoryTier.ELITE, type);
            addFactoryRecipe(consumer, basicPath, basicFactory, type.getBaseBlock(), Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC);
            addFactoryRecipe(consumer, advancedPath, advancedFactory, basicFactory, osmiumIngot, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED);
            addFactoryRecipe(consumer, elitePath, eliteFactory, advancedFactory, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE);
            addFactoryRecipe(consumer, ultimatePath, MekanismBlocks.getFactory(FactoryTier.ULTIMATE, type), eliteFactory, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
        }
    }

    private void addFactoryRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> factory, BlockRegistryObject<?, ?> toUpgrade, TagKey<Item> ingotTag,
          TagKey<Item> alloyTag, TagKey<Item> circuitTag) {
        MekDataShapedRecipeBuilder.shapedRecipe(factory)
              .pattern(MekanismRecipeProvider.TIER_PATTERN)
              .key(Pattern.PREVIOUS, toUpgrade)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + Attribute.getOrThrow(factory, AttributeFactoryType.class).getFactoryType().getRegistryNameComponent()));
    }
}