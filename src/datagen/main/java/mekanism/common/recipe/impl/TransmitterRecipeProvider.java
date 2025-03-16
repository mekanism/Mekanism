package mekanism.common.recipe.impl;

import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

class TransmitterRecipeProvider implements ISubRecipeProvider {

    private static final RecipePattern BASIC_TRANSMITTER_PATTERN = RecipePattern.createPattern(TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL));
    private static final RecipePattern TRANSMITTER_UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS));

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "transmitter/";
        addLogisticalTransporterRecipes(consumer, basePath + "logistical_transporter/");
        addMechanicalPipeRecipes(consumer, basePath + "mechanical_pipe/");
        addPressurizedTubeRecipes(consumer, basePath + "pressurized_tube/");
        addThermodynamicConductorRecipes(consumer, basePath + "thermodynamic_conductor/");
        addUniversalCableRecipes(consumer, basePath + "universal_cable/");
        //Diversion
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DIVERSION_TRANSPORTER, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.REDSTONE, Pattern.REDSTONE),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.REDSTONE, Pattern.REDSTONE, Pattern.REDSTONE))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.IRON_BARS)
              .build(consumer, Mekanism.rl(basePath + "diversion_transporter"));
        //Restrictive
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESTRICTIVE_TRANSPORTER, 2)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, Items.IRON_BARS)
              .build(consumer, Mekanism.rl(basePath + "restrictive_transporter"));
    }

    private void addLogisticalTransporterRecipes(RecipeOutput consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.CIRCUITS_BASIC);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_INFUSED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_REINFORCED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addMechanicalPipeRecipes(RecipeOutput consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_MECHANICAL_PIPE, Items.BUCKET);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismBlocks.BASIC_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_INFUSED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_REINFORCED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addPressurizedTubeRecipes(RecipeOutput consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_PRESSURIZED_TUBE, Tags.Items.GLASS_BLOCKS_CHEAP);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_INFUSED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_REINFORCED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addThermodynamicConductorRecipes(RecipeOutput consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, Tags.Items.INGOTS_COPPER);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_INFUSED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_REINFORCED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addUniversalCableRecipes(RecipeOutput consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_UNIVERSAL_CABLE, Tags.Items.DUSTS_REDSTONE);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_INFUSED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_REINFORCED);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addBasicTransmitterRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> transmitter, TagKey<Item> itemTag) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, itemTag)
              .build(consumer, Mekanism.rl(basePath + Attribute.getBaseTier(transmitter).getLowerName()));
    }

    private void addBasicTransmitterRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> transmitter, Item item) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, item)
              .build(consumer, Mekanism.rl(basePath + Attribute.getBaseTier(transmitter).getLowerName()));
    }

    private void addTransmitterUpgradeRecipe(RecipeOutput consumer, String basePath, BlockRegistryObject<?, ?> transmitter, BlockRegistryObject<?, ?> previousTransmitter,
          TagKey<Item> alloyTag) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(TRANSMITTER_UPGRADE_PATTERN)
              .key(Pattern.PREVIOUS, previousTransmitter)
              .key(Pattern.ALLOY, alloyTag)
              .build(consumer, Mekanism.rl(basePath + Attribute.getBaseTier(transmitter).getLowerName()));
    }
}