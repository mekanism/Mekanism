package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class FactoryRecipeProvider extends BaseSubRecipeProvider {

    FactoryRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer) {
        String basePath = "factory/";
        String basicPath = basePath + "basic/";
        String advancedPath = basePath + "advanced/";
        String elitePath = basePath + "elite/";
        String ultimatePath = basePath + "ultimate/";
        TagKey<Item> osmiumIngot = MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM);
        for (FactoryType type : FactoryType.VALUES) {
            BlockRegistryObject<BlockFactory<?>, ?> basicFactory = MekanismBlocks.getFactory(FactoryTier.BASIC, type);
            BlockRegistryObject<BlockFactory<?>, ?> advancedFactory = MekanismBlocks.getFactory(FactoryTier.ADVANCED, type);
            BlockRegistryObject<BlockFactory<?>, ?> eliteFactory = MekanismBlocks.getFactory(FactoryTier.ELITE, type);
            addFactoryRecipe(consumer, basicPath, type, basicFactory, type.getBaseBlock(), Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC);
            addFactoryRecipe(consumer, advancedPath, type, advancedFactory, basicFactory, osmiumIngot, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED);
            addFactoryRecipe(consumer, elitePath, type, eliteFactory, advancedFactory, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE);
            addFactoryRecipe(consumer, ultimatePath, type, MekanismBlocks.getFactory(FactoryTier.ULTIMATE, type), eliteFactory, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
        }
    }

    private void addFactoryRecipe(RecipeOutput consumer, String basePath, FactoryType type, BlockRegistryObject<?, ?> factory, BlockRegistryObject<?, ?> toUpgrade, TagKey<Item> ingotTag,
          TagKey<Item> alloyTag, TagKey<Item> circuitTag) {
        MekDataShapedRecipeBuilder.shaped(factory)
              .pattern(MekanismRecipeProvider.TIER_PATTERN)
              .define(Pattern.PREVIOUS, toUpgrade)
              .define(Pattern.CIRCUIT, this.items, circuitTag)
              .define(Pattern.INGOT, this.items, ingotTag)
              .define(Pattern.ALLOY, this.items, alloyTag)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(type.getSerializedName()))
              .unlockedBy("has_circuit", has(circuitTag))
              .save(consumer, Mekanism.rl(basePath + Attribute.getOrThrow(factory, AttributeFactoryType.class).getFactoryType().getSerializedName()));
    }
}