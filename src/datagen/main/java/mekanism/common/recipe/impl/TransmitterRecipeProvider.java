package mekanism.common.recipe.impl;

import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class TransmitterRecipeProvider extends BaseSubRecipeProvider {

    private static final RecipePattern BASIC_TRANSMITTER_PATTERN = RecipePattern.createPattern(TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL));
    private static final RecipePattern TRANSMITTER_UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS));

    TransmitterRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "transmitter/";
        addLogisticalTransporterRecipes(output, basePath + "logistical_transporter/", "logistical_transporter");
        addMechanicalPipeRecipes(output, basePath + "mechanical_pipe/", "mechanical_pipe");
        addPressurizedTubeRecipes(output, basePath + "pressurized_tube/", "pressurized_tube");
        addThermodynamicConductorRecipes(output, basePath + "thermodynamic_conductor/", "thermodynamic_conductor");
        addUniversalCableRecipes(output, basePath + "universal_cable/", "universal_cable");
        //Diversion
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.DIVERSION_TRANSPORTER, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.REDSTONE, Pattern.REDSTONE),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.REDSTONE, Pattern.REDSTONE, Pattern.REDSTONE))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.BARS)
              .category(RecipeCategory.REDSTONE)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, basePath + "diversion_transporter");
        //Restrictive
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.RESTRICTIVE_TRANSPORTER, 2)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.BARS)
              .category(RecipeCategory.REDSTONE)//Use the redstone category so it shows up with hoppers
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, basePath + "restrictive_transporter");
    }

    private void addLogisticalTransporterRecipes(RecipeOutput output, String basePath, String group) {
        addBasicTransmitterRecipe(output, basePath, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.CIRCUITS_BASIC, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_INFUSED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_REINFORCED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_ATOMIC, group);
    }

    private void addMechanicalPipeRecipes(RecipeOutput output, String basePath, String group) {
        addBasicTransmitterRecipe(output, basePath, MekanismBlocks.BASIC_MECHANICAL_PIPE, Tags.Items.BUCKETS_EMPTY, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismBlocks.BASIC_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_INFUSED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_REINFORCED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_ATOMIC, group);
    }

    private void addPressurizedTubeRecipes(RecipeOutput output, String basePath, String group) {
        addBasicTransmitterRecipe(output, basePath, MekanismBlocks.BASIC_PRESSURIZED_TUBE, Tags.BlockItems.GLASS_BLOCKS_CHEAP.item(), group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_INFUSED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_REINFORCED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_ATOMIC, group);
    }

    private void addThermodynamicConductorRecipes(RecipeOutput output, String basePath, String group) {
        addBasicTransmitterRecipe(output, basePath, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, Tags.Items.INGOTS_COPPER, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_INFUSED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_REINFORCED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_ATOMIC, group);
    }

    private void addUniversalCableRecipes(RecipeOutput output, String basePath, String group) {
        addBasicTransmitterRecipe(output, basePath, MekanismBlocks.BASIC_UNIVERSAL_CABLE, Tags.Items.DUSTS_REDSTONE, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_INFUSED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_REINFORCED, group);
        addTransmitterUpgradeRecipe(output, basePath, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_ATOMIC, group);
    }

    private void addBasicTransmitterRecipe(RecipeOutput output, String basePath, BlockRegistryObject<?, ?> transmitter, TagKey<Item> itemTag, String group) {
        ExtendedShapedRecipeBuilder.shaped(transmitter, 8)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.CONSTANT, this.items, itemTag)
              .category(RecipeCategory.REDSTONE)//Use the redstone category so it shows up with hoppers
              .group(Mekanism.rl(group))
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, basePath + Attribute.getBaseTierNN(transmitter).getLowerName());
    }

    private void addTransmitterUpgradeRecipe(RecipeOutput output, String basePath, BlockRegistryObject<?, ?> transmitter, BlockRegistryObject<?, ?> previousTransmitter,
          TagKey<Item> alloyTag, String group) {
        ExtendedShapedRecipeBuilder.shaped(transmitter, 8)
              .pattern(TRANSMITTER_UPGRADE_PATTERN)
              .define(Pattern.PREVIOUS, previousTransmitter)
              .define(Pattern.ALLOY, this.items, alloyTag)
              .category(RecipeCategory.REDSTONE)//Use the redstone category so it shows up with hoppers
              .group(Mekanism.rl(group))
              .unlockedBy("has_alloy", has(alloyTag))
              .save(output, basePath + Attribute.getBaseTierNN(transmitter).getLowerName());
    }
}