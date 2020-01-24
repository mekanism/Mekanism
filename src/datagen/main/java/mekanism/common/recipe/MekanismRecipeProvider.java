package mekanism.common.recipe;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackGasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToInfuseTypeRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockInductionCell;
import mekanism.common.block.basic.BlockInductionProvider;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.RecipePattern.DoubleLine;
import mekanism.common.recipe.RecipePattern.TripleLine;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

//TODO: Figure out what we want to do with compat recipes
//TODO: Clean this up some to use methods in places that the code is just copy pasted, example: circuits
@ParametersAreNonnullByDefault
public class MekanismRecipeProvider extends BaseRecipeProvider {

    private static final char GLASS_CHAR = 'G';

    private static final RecipePattern BIN_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.COBBLESTONE, Pattern.CIRCUIT, Pattern.COBBLESTONE),
          TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
          TripleLine.of(Pattern.COBBLESTONE, Pattern.COBBLESTONE, Pattern.COBBLESTONE));
    private static final RecipePattern ENERGY_CUBE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY));
    private static final RecipePattern FLUID_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY));
    //TODO: Do we want to use same pattern for fluid tank and gas tank at some point
    private static final RecipePattern GAS_TANK_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
          TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
          TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY));
    private static final RecipePattern TIER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));
    private static final RecipePattern INDUCTION_CELL_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY));
    private static final RecipePattern INDUCTION_PROVIDER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
          TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
          TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT));
    private static final RecipePattern STORAGE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    private static final RecipePattern BASIC_TRANSMITTER_PATTERN = RecipePattern.createPattern(TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL));
    private static final RecipePattern TRANSMITTER_UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
          TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS));
    private static final RecipePattern UPGRADE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY));

    public MekanismRecipeProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        addSolarNeutronActivatorRecipes(consumer);
        addBinRecipes(consumer);
        addChemicalInfuserRecipes(consumer);
        addCombinerRecipes(consumer);
        addControlCircuitRecipes(consumer);
        addCrusherRecipes(consumer);
        addChemicalCrystallizerRecipes(consumer);
        addEnergyCubeRecipes(consumer);
        addEnrichmentChamberRecipes(consumer);
        addEvaporatingRecipes(consumer);
        addFactoryRecipes(consumer);
        addFluidTankRecipes(consumer);
        addGasConversionRecipes(consumer);
        addGasTankRecipes(consumer);
        addInductionRecipes(consumer);
        addInfusionConversionRecipes(consumer);
        addChemicalInjectorRecipes(consumer);
        addMetallurgicInfuserRecipes(consumer);
        addNuggetRecipes(consumer);
        addChemicalOxidizerRecipes(consumer);
        addOreProcessingRecipes(consumer);
        addPurificationChamberRecipes(consumer);
        addPressurizedReactionChamberRecipes(consumer);
        addRotaryCondensentratorRecipes(consumer);
        addPrecisionSawmillRecipes(consumer);
        addElectrolyticSeparatorRecipes(consumer);
        addStorageBlockRecipes(consumer);
        addThermalEvaporationRecipes(consumer);
        addTierInstallerRecipes(consumer);
        addTransmitterRecipes(consumer);
        addUpgradeRecipes(consumer);
        addMiscRecipes(consumer);
    }

    private void addSolarNeutronActivatorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "activating/";
        GasToGasRecipeBuilder.activating(
              GasStackIngredient.from(MekanismGases.LITHIUM, 1),
              MekanismGases.TRITIUM.getGasStack(1)
        ).addCriterion(Criterion.HAS_SOLAR_NEUTRON_ACTIVATOR)
              .build(consumer, Mekanism.rl(basePath + "tritium"));
    }

    private void addBinRecipes(Consumer<IFinishedRecipe> consumer) {
        //TODO: Also somehow define the adding to bin/removing from bin recipes (maybe a "special recipe" similar to shield patterns)
        String basePath = "bin/";
        //Note: For the basic bin, we have to handle the empty slot differently than batching it against our bin pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_BIN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.CIRCUIT, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.EMPTY, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.COBBLESTONE, Pattern.COBBLESTONE))
              ).key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredBin(consumer, basePath, MekanismBlocks.ADVANCED_BIN, MekanismBlocks.BASIC_BIN, MekanismTags.Items.CIRCUITS_ADVANCED, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_ADVANCED_CIRCUIT);
        addTieredBin(consumer, basePath, MekanismBlocks.ELITE_BIN, MekanismBlocks.ADVANCED_BIN, MekanismTags.Items.CIRCUITS_ELITE, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_ELITE_CIRCUIT);
        addTieredBin(consumer, basePath, MekanismBlocks.ULTIMATE_BIN, MekanismBlocks.ELITE_BIN, MekanismTags.Items.CIRCUITS_ULTIMATE, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ULTIMATE_CIRCUIT);
    }

    private void addTieredBin(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockBin, ?> bin, IItemProvider previousBin, Tag<Item> circuitTag,
          Tag<Item> alloyTag, RecipeCriterion circuitCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(bin)
              .pattern(BIN_PATTERN)
              .key(Pattern.PREVIOUS, previousBin)
              .key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(circuitCriterion)
              .addCriterion(Criterion.has(previousBin))
              .build(consumer, Mekanism.rl(basePath + bin.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addChemicalInfuserRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
        //DT Fuel
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.DEUTERIUM, 1),
              GasStackIngredient.from(MekanismGases.TRITIUM, 1),
              MekanismGases.FUSION_FUEL.getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "fusion_fuel"));
        //Hydrogen Chloride
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.HYDROGEN, 1),
              GasStackIngredient.from(MekanismGases.CHLORINE, 1),
              MekanismGases.HYDROGEN_CHLORIDE.getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "hydrogen_chloride"));
        //Sulfur Trioxide
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.OXYGEN, 1),
              GasStackIngredient.from(MekanismGases.SULFUR_DIOXIDE, 2),
              MekanismGases.SULFUR_TRIOXIDE.getGasStack(2)
        ).addCriterion(Criterion.HAS_CHEMICAL_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "sulfur_trioxide"));
        //Sulfuric Acid
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.SULFUR_TRIOXIDE, 1),
              GasStackIngredient.from(MekanismGases.STEAM, 1),
              MekanismGases.SULFURIC_ACID.getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
    }

    private void addCombinerRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "combining/";
        addCombinerDyeRecipes(consumer, basePath + "dye/");
        //Gravel
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Items.FLINT),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.GRAVEL)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "gravel"));
        //Obsidian
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_OBSIDIAN, 4),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.OBSIDIAN)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "obsidian"));
    }

    private void addCombinerDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black + white -> light gray
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLACK),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE, 2),
              new ItemStack(Items.LIGHT_GRAY_DYE, 6)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "black_to_light_gray"));
        //Blue + green -> cyan
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_GREEN),
              new ItemStack(Items.CYAN_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "cyan"));
        //Gray + white -> light gray
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_GRAY),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_GRAY_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "gray_to_light_gray"));
        //Blue + white -> light blue
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIGHT_GRAY_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Green + white -> lime
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_GREEN),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.LIME_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "lime"));
        //Purple + pink -> magenta
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_PURPLE),
              ItemStackIngredient.from(Tags.Items.DYES_PINK),
              new ItemStack(Items.MAGENTA_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "magenta"));
        //Red + yellow -> orange
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              ItemStackIngredient.from(Tags.Items.DYES_YELLOW),
              new ItemStack(Items.ORANGE_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "orange"));
        //Red + white -> pink
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              ItemStackIngredient.from(Tags.Items.DYES_WHITE),
              new ItemStack(Items.PINK_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "pink"));
        //Blue + red -> purple
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DYES_BLUE),
              ItemStackIngredient.from(Tags.Items.DYES_RED),
              new ItemStack(Items.PURPLE_DYE, 4)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "purple"));
    }

    private void addControlCircuitRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "control_circuit/";
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_OSMIUM),
              InfusionIngredient.from(MekanismTags.InfuseTypes.REDSTONE, 10),
              MekanismItems.BASIC_CONTROL_CIRCUIT.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        RecipePattern circuitPattern = RecipePattern.createPattern(TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ADVANCED_CONTROL_CIRCUIT)
              .pattern(circuitPattern)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer, Mekanism.rl(basePath + "advanced"));
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ELITE_CONTROL_CIRCUIT)
              .pattern(circuitPattern)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .addCriterion(Criterion.HAS_ADVANCED_CIRCUIT)
              .build(consumer, Mekanism.rl(basePath + "elite"));
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
              .pattern(circuitPattern)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .addCriterion(Criterion.HAS_ELITE_CIRCUIT)
              .build(consumer, Mekanism.rl(basePath + "ultimate"));
    }

    private void addCrusherRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "crushing/";
        addCrusherBioFuelRecipes(consumer, basePath + "biofuel/");
        //Charcoal -> Charcoal Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHARCOAL),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Chiseled Stone Bricks -> Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CHISELED_STONE_BRICKS),
              new ItemStack(Items.STONE_BRICKS)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stone_bricks_to_stone_bricks"));
        //Cobblestone -> Gravel
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.GRAVEL)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "cobblestone_to_gravel"));
        //Cracked Stone Bricks -> Stone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CRACKED_STONE_BRICKS),
              new ItemStack(Items.STONE)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "cracked_stone_bricks_to_stone"));
        //Flint -> Gunpowder
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.FLINT),
              new ItemStack(Items.GUNPOWDER)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "flint_to_gunpowder"));
        //Gravel -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              new ItemStack(Items.SAND)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "gravel_to_sand"));
        //TODO: Do we just want to make a clear and red tag for sandstone?
        //Red Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.RED_SANDSTONE),
                    ItemStackIngredient.from(Items.CHISELED_RED_SANDSTONE),
                    ItemStackIngredient.from(Items.CUT_RED_SANDSTONE),
                    ItemStackIngredient.from(Items.SMOOTH_RED_SANDSTONE)
              ),
              new ItemStack(Items.RED_SAND, 2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "red_sandstone_to_sand"));
        //Sandstone -> Sand
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.SANDSTONE),
                    ItemStackIngredient.from(Items.CHISELED_SANDSTONE),
                    ItemStackIngredient.from(Items.CUT_SANDSTONE),
                    ItemStackIngredient.from(Items.SMOOTH_SANDSTONE)
              ),
              new ItemStack(Items.SAND, 2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "sandstone_to_sand"));
        //Stone Bricks -> Cracked Stone Bricks
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.STONE_BRICKS),
              new ItemStack(Items.CRACKED_STONE_BRICKS)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "stone_bricks_to_cracked_stone_bricks"));
        //Stone -> Cobblestone
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.STONE),
              new ItemStack(Items.COBBLESTONE)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "stone_to_cobblestone"));
        //Wool -> String
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.WOOL),
              new ItemStack(Items.STRING, 4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "wool_to_string"));
    }

    //TODO: Re-balance these recipes as the amounts of bio fuel various items produces does not make sense
    // in relation to the amounts other things give
    //TODO: Evaluate the bio fuel recipes, and maybe add sweet berry bush/coral to it (and maybe bamboo sapling, and honey).
    // Also add missing flowers that don't get caught by the small flowers tag
    private void addCrusherBioFuelRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Apple
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.APPLE),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "apple"));
        //Baked Potato
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.BAKED_POTATO),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "baked_potato"));
        //Bamboo
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.BAMBOO),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "bamboo"));
        //Bread
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.BREAD),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "bread"));
        //Cactus
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.CACTUS),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "cactus"));
        //Crops
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.CROPS),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "crops"));
        //Grass
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.GRASS),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "grass"));
        //Kelp
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.KELP),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "kelp"));
        //Leaves
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.LEAVES, 10),
              MekanismItems.BIO_FUEL.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "leaves"));
        //Melon
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.MELON),
              MekanismItems.BIO_FUEL.getItemStack(16)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "melon"));
        //Melon Slice
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.MELON_SLICE),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "melon_slice"));
        //Mushrooms
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.MUSHROOMS),
              MekanismItems.BIO_FUEL.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "mushrooms"));
        //Poisonous Potato
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.POISONOUS_POTATO),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "poisonous_potato"));
        //Pumpkin
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.PUMPKIN),
              MekanismItems.BIO_FUEL.getItemStack(6)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "pumpkin"));
        //Rotten Flesh
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.ROTTEN_FLESH),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "rotten_flesh"));
        //Saplings
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.SAPLINGS),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "saplings"));
        //Seeds
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.SEEDS),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "seeds"));
        //Small Flowers
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ItemTags.SMALL_FLOWERS),
              MekanismItems.BIO_FUEL.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "small_flowers"));
        //Sugar Cane
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.SUGAR_CANE),
              MekanismItems.BIO_FUEL.getItemStack(2)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "sugar_cane"));
        //Sweet Berries
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.SWEET_BERRIES),
              MekanismItems.BIO_FUEL.getItemStack(4)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "sweet_berries"));
    }

    private void addChemicalCrystallizerRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "crystallizing/";
        //Salt
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.BRINE, 15),
              MekanismItems.SALT.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_CRYSTALLIZER)
              .build(consumer, Mekanism.rl(basePath + "salt"));
        //Lithium
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.LITHIUM, 100),
              MekanismItems.LITHIUM_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_CRYSTALLIZER)
              .build(consumer, Mekanism.rl(basePath + "lithium"));
    }

    private void addEnergyCubeRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "energy_cube/";
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.STEEL_CASING, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.BASIC_ENERGY_CUBE, MekanismTags.Items.INGOTS_OSMIUM, MekanismTags.Items.ALLOYS_INFUSED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED);
        addTieredEnergyCube(consumer, basePath, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC);
    }

    private void addTieredEnergyCube(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockEnergyCube, ?> energyCube,
          IItemProvider previousEnergyCube, Tag<Item> ingotTag, Tag<Item> alloyTag) {
        ExtendedShapedRecipeBuilder.shapedRecipe(energyCube)
              .pattern(ENERGY_CUBE_PATTERN)
              .key(Pattern.PREVIOUS, previousEnergyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .addCriterion(Criterion.has(previousEnergyCube))
              .build(consumer, Mekanism.rl(basePath + energyCube.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addEnrichmentChamberRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "enriching/";
        addEnrichingConversionRecipes(consumer, basePath + "conversion/");
        addEnrichingDyeRecipes(consumer, basePath + "dye/");
        addEnrichingEnrichedRecipes(consumer, basePath + "enriched/");
        //Charcoal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL),
              new ItemStack(Items.CHARCOAL)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "charcoal"));
        //Charcoal dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_WOOD, 8),
              MekanismItems.CHARCOAL_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "charcoal_dust"));
        //Clay ball
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.CLAY),
              new ItemStack(Items.CLAY_BALL, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "clay_ball"));
        //Glowstone dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "glowstone_dust"));
        //HDPE Sheet
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismItems.HDPE_PELLET, 3),
              MekanismItems.HDPE_SHEET.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "hdpe_sheet"));
        //Salt
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismBlocks.SALT_BLOCK),
              MekanismItems.SALT.getItemStack(4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "salt"));
    }

    private void addEnrichingConversionRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Cracked stone bricks -> stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.CRACKED_STONE_BRICKS),
              new ItemStack(Items.STONE_BRICKS)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "cracked_stone_bricks_to_stone_bricks"));
        //Gravel -> cobblestone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              new ItemStack(Items.COBBLESTONE)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "gravel_to_cobblestone"));
        //Gunpowder -> flint
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GUNPOWDER),
              new ItemStack(Items.FLINT)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "gunpowder_to_flint"));
        //Mossy stone bricks -> stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.MOSSY_STONE_BRICKS),
              new ItemStack(Items.STONE_BRICKS)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "mossy_stone_bricks_to_stone_bricks"));
        //Mossy -> cobblestone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.MOSSY_COBBLESTONE),
              new ItemStack(Items.COBBLESTONE)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "mossy_to_cobblestone"));
        //Sand -> gravel
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.SAND),
              new ItemStack(Items.GRAVEL)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "sand_to_gravel"));
        //Stone bricks -> chiseled stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.STONE_BRICKS),
              new ItemStack(Items.CHISELED_STONE_BRICKS)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "stone_bricks_to_chiseled_stone_bricks"));
        //Stone -> cracked stone bricks
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.STONE),
              new ItemStack(Items.CRACKED_STONE_BRICKS)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "stone_to_cracked_stone_bricks"));
        //Sulfur -> gunpowder
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              new ItemStack(Items.GUNPOWDER)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "sulfur_to_gunpowder"));
        //Obsidian -> obsidian dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.OBSIDIAN),
              MekanismItems.OBSIDIAN_DUST.getItemStack(4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "obsidian_to_obsidian_dust"));
    }

    private void addEnrichingDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.WITHER_ROSE),
              new ItemStack(Items.BLACK_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "black"));
        //Blue
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.CORNFLOWER),
              new ItemStack(Items.BLUE_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "blue"));
        //Green
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.CACTUS),
              new ItemStack(Items.GREEN_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "green"));
        //Magenta
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.LILAC),
              new ItemStack(Items.MAGENTA_DYE, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "large_magenta"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.ALLIUM),
              new ItemStack(Items.MAGENTA_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "small_magenta"));
        //Pink
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.PEONY),
              new ItemStack(Items.PINK_DYE, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "large_pink"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.PINK_TULIP),
              new ItemStack(Items.PINK_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "small_pink"));
        //Red
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.ROSE_BUSH),
              new ItemStack(Items.RED_DYE, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "large_red"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.RED_TULIP),
                    ItemStackIngredient.from(Items.POPPY)
              ),
              new ItemStack(Items.RED_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "small_red"));
        //Yellow
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.SUNFLOWER),
              new ItemStack(Items.YELLOW_DYE, 4)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "large_yellow"));
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.DANDELION),
              new ItemStack(Items.YELLOW_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "small_yellow"));
        //Light blue
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.BLUE_ORCHID),
              new ItemStack(Items.LIGHT_BLUE_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "light_blue"));
        //Light gray
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.OXEYE_DAISY),
                    ItemStackIngredient.from(Items.AZURE_BLUET),
                    ItemStackIngredient.from(Items.WHITE_TULIP)
              ),
              new ItemStack(Items.LIGHT_GRAY_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "light_gray"));
        //Orange
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.ORANGE_TULIP),
              new ItemStack(Items.ORANGE_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "orange"));
        //White
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Items.LILY_OF_THE_VALLEY),
              new ItemStack(Items.WHITE_DYE, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "white"));
    }

    private void addEnrichingEnrichedRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Carbon
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(ItemTags.COALS),
              MekanismItems.ENRICHED_CARBON.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "carbon"));
        //Diamond
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.GEMS_DIAMOND),
              MekanismItems.ENRICHED_DIAMOND.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "diamond"));
        //Redstone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE),
              MekanismItems.ENRICHED_REDSTONE.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "redstone"));
        //Refined Obsidian
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismItems.ENRICHED_OBSIDIAN.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "refined_obsidian"));
        //Tin
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_TIN),
              MekanismItems.ENRICHED_TIN.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "tin"));
    }

    private void addEvaporatingRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "evaporating/";
        //Brine
        FluidToFluidRecipeBuilder.evaporating(
              FluidStackIngredient.from(FluidTags.WATER, 10),
              MekanismFluids.BRINE.getFluidStack(1)
        ).addCriterion(Criterion.HAS_THERMAL_EVAPORATION_CONTROLLER)
              .build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        FluidToFluidRecipeBuilder.evaporating(
              FluidStackIngredient.from(MekanismTags.Fluids.BRINE, 10),
              MekanismFluids.LITHIUM.getFluidStack(1)
        ).addCriterion(Criterion.HAS_THERMAL_EVAPORATION_CONTROLLER)
              .build(consumer, Mekanism.rl(basePath + "lithium"));
    }

    private void addFactoryRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "factory/";
        addBasicFactoryRecipes(consumer, basePath + "basic/");
        addAdvancedFactoryRecipes(consumer, basePath + "advanced/");
        addEliteFactoryRecipes(consumer, basePath + "elite/");
        addUltimateFactoryRecipes(consumer, basePath + "ultimate/");
    }

    private void addBasicFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_COMBINING_FACTORY, MekanismBlocks.COMBINER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_COMPRESSING_FACTORY, MekanismBlocks.OSMIUM_COMPRESSOR);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_CRUSHING_FACTORY, MekanismBlocks.CRUSHER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_ENRICHING_FACTORY, MekanismBlocks.ENRICHMENT_CHAMBER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_INFUSING_FACTORY, MekanismBlocks.METALLURGIC_INFUSER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_INJECTING_FACTORY, MekanismBlocks.CHEMICAL_INJECTION_CHAMBER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_PURIFYING_FACTORY, MekanismBlocks.PURIFICATION_CHAMBER);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_SAWING_FACTORY, MekanismBlocks.PRECISION_SAWMILL);
        addBasicFactoryRecipe(consumer, basePath, MekanismBlocks.BASIC_SMELTING_FACTORY, MekanismBlocks.ENERGIZED_SMELTER);
    }

    private void addBasicFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ?> factory, IItemProvider toUpgrade) {
        addFactoryRecipe(consumer, basePath, factory, toUpgrade, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC);
    }

    private void addAdvancedFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_COMBINING_FACTORY, MekanismBlocks.BASIC_COMBINING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_COMPRESSING_FACTORY, MekanismBlocks.BASIC_COMPRESSING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_CRUSHING_FACTORY, MekanismBlocks.BASIC_CRUSHING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_ENRICHING_FACTORY, MekanismBlocks.BASIC_ENRICHING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INFUSING_FACTORY, MekanismBlocks.BASIC_INFUSING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INJECTING_FACTORY, MekanismBlocks.BASIC_INJECTING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_PURIFYING_FACTORY, MekanismBlocks.BASIC_PURIFYING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_SAWING_FACTORY, MekanismBlocks.BASIC_SAWING_FACTORY);
        addAdvancedFactoryRecipe(consumer, basePath, MekanismBlocks.ADVANCED_SMELTING_FACTORY, MekanismBlocks.BASIC_SMELTING_FACTORY);
    }

    private void addAdvancedFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ?> factory, IItemProvider toUpgrade) {
        addFactoryRecipe(consumer, basePath, factory, toUpgrade, MekanismTags.Items.INGOTS_OSMIUM, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED);
    }

    private void addEliteFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_COMBINING_FACTORY, MekanismBlocks.ADVANCED_COMBINING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_COMPRESSING_FACTORY, MekanismBlocks.ADVANCED_COMPRESSING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_CRUSHING_FACTORY, MekanismBlocks.ADVANCED_CRUSHING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_ENRICHING_FACTORY, MekanismBlocks.ADVANCED_ENRICHING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_INFUSING_FACTORY, MekanismBlocks.ADVANCED_INFUSING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_INJECTING_FACTORY, MekanismBlocks.ADVANCED_INJECTING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_PURIFYING_FACTORY, MekanismBlocks.ADVANCED_PURIFYING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_SAWING_FACTORY, MekanismBlocks.ADVANCED_SAWING_FACTORY);
        addEliteFactoryRecipe(consumer, basePath, MekanismBlocks.ELITE_SMELTING_FACTORY, MekanismBlocks.ADVANCED_SMELTING_FACTORY);
    }

    private void addEliteFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ?> factory, IItemProvider toUpgrade) {
        addFactoryRecipe(consumer, basePath, factory, toUpgrade, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE);
    }

    private void addUltimateFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_COMBINING_FACTORY, MekanismBlocks.ELITE_COMBINING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_COMPRESSING_FACTORY, MekanismBlocks.ELITE_COMPRESSING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_CRUSHING_FACTORY, MekanismBlocks.ELITE_CRUSHING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_ENRICHING_FACTORY, MekanismBlocks.ELITE_ENRICHING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INFUSING_FACTORY, MekanismBlocks.ELITE_INFUSING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INJECTING_FACTORY, MekanismBlocks.ELITE_INJECTING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_PURIFYING_FACTORY, MekanismBlocks.ELITE_PURIFYING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_SAWING_FACTORY, MekanismBlocks.ELITE_SAWING_FACTORY);
        addUltimateFactoryRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_SMELTING_FACTORY, MekanismBlocks.ELITE_SMELTING_FACTORY);
    }

    private void addUltimateFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ?> factory, IItemProvider toUpgrade) {
        addFactoryRecipe(consumer, basePath, factory, toUpgrade, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ?> factory,
          IItemProvider toUpgrade, Tag<Item> ingotTag, Tag<Item> alloyTag, Tag<Item> circuitTag) {
        ExtendedShapedRecipeBuilder.shapedRecipe(factory)
              .pattern(TIER_PATTERN)
              .key(Pattern.PREVIOUS, toUpgrade)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.has(toUpgrade))
              .build(consumer, Mekanism.rl(basePath + factory.getBlock().getFactoryType().getRegistryNameComponent()));
    }

    private void addFluidTankRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "fluid_tank/";
        //Note: For the basic fluid tank, we have to handle the empty slot differently than batching it against our fluid tank pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_FLUID_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .addCriterion(Criterion.HAS_BASIC_ALLOY)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.BASIC_FLUID_TANK, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ELITE_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTieredFluidTank(consumer, basePath, MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addTieredFluidTank(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFluidTank, ?> tank, IItemProvider previousTank,
          Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(FLUID_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(alloyCriterion)
              .addCriterion(Criterion.has(previousTank))
              .build(consumer, Mekanism.rl(basePath + tank.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addGasConversionRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "gas_conversion/";
        //Flint -> oxygen
        ItemStackToGasRecipeBuilder.gasConversion(
              ItemStackIngredient.from(Items.FLINT),
              MekanismGases.OXYGEN.getGasStack(10)
        ).addCriterion(Criterion.has(Items.FLINT))
              .build(consumer, Mekanism.rl(basePath + "flint_to_oxygen"));
        //Osmium block -> osmium
        ItemStackToGasRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_OSMIUM),
              MekanismGases.LIQUID_OSMIUM.getGasStack(1_800)
        ).addCriterion(Criterion.has("osmium_block", MekanismTags.Items.STORAGE_BLOCKS_OSMIUM))
              .build(consumer, Mekanism.rl(basePath + "osmium_from_block"));
        //Osmium ingot -> osmium
        ItemStackToGasRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_OSMIUM),
              MekanismGases.LIQUID_OSMIUM.getGasStack(200)
        ).addCriterion(Criterion.HAS_OSMIUM)
              .build(consumer, Mekanism.rl(basePath + "osmium_from_ingot"));
        //Salt -> hydrogen chloride
        ItemStackToGasRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.HYDROGEN_CHLORIDE.getGasStack(2)
        ).addCriterion(Criterion.has("salt", MekanismTags.Items.DUSTS_SALT))
              .build(consumer, Mekanism.rl(basePath + "salt_to_hydrogen_chloride"));
        //Sulfur -> sulfuric acid
        ItemStackToGasRecipeBuilder.gasConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFURIC_ACID.getGasStack(2)
        ).addCriterion(Criterion.has("sulfur", MekanismTags.Items.DUSTS_SULFUR))
              .build(consumer, Mekanism.rl(basePath + "sulfur_to_sulfuric_acid"));
    }

    private void addGasTankRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "gas_tank/";
        //Note: For the basic gas tank, we have to handle the empty slot differently than batching it against our gas tank pattern
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_GAS_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .addCriterion(Criterion.HAS_BASIC_ALLOY)
              .addCriterion(Criterion.HAS_OSMIUM)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredGasTank(consumer, basePath, MekanismBlocks.ADVANCED_GAS_TANK, MekanismBlocks.BASIC_GAS_TANK, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTieredGasTank(consumer, basePath, MekanismBlocks.ELITE_GAS_TANK, MekanismBlocks.ADVANCED_GAS_TANK, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTieredGasTank(consumer, basePath, MekanismBlocks.ULTIMATE_GAS_TANK, MekanismBlocks.ELITE_GAS_TANK, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addTieredGasTank(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockGasTank, ?> tank, IItemProvider previousTank,
          Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(GAS_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(alloyCriterion)
              .addCriterion(Criterion.has(previousTank))
              .build(consumer, Mekanism.rl(basePath + tank.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addInductionRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "induction/";
        addInductionCellRecipes(consumer, basePath + "cell/");
        addInductionProviderRecipes(consumer, basePath + "provider/");
        //Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUCTION_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_STEEL)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer, Mekanism.rl(basePath + "casing"));
        //Port
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUCTION_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.INDUCTION_CASING)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .addCriterion(Criterion.HAS_ELITE_CIRCUIT)
              .addCriterion(Criterion.has(MekanismBlocks.INDUCTION_CASING))
              .build(consumer, Mekanism.rl(basePath + "port"));
    }

    private void addInductionCellRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Basic needs to be handled slightly differently
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_INDUCTION_CELL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM),
                    TripleLine.of(Pattern.ENERGY, Pattern.CONSTANT, Pattern.ENERGY),
                    TripleLine.of(Pattern.LITHIUM, Pattern.ENERGY, Pattern.LITHIUM))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.LITHIUM, MekanismTags.Items.DUSTS_LITHIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .addCriterion(Criterion.has(MekanismBlocks.BASIC_ENERGY_CUBE))
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.BASIC_INDUCTION_CELL, MekanismBlocks.ADVANCED_ENERGY_CUBE);
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ADVANCED_INDUCTION_CELL, MekanismBlocks.ELITE_ENERGY_CUBE);
        addTieredInductionCellRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INDUCTION_CELL, MekanismBlocks.ELITE_INDUCTION_CELL, MekanismBlocks.ULTIMATE_ENERGY_CUBE);
    }

    private void addTieredInductionCellRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockInductionCell, ?> cell,
          IItemProvider previousCell, IItemProvider energyCube) {
        ExtendedShapedRecipeBuilder.shapedRecipe(cell)
              .pattern(INDUCTION_CELL_PATTERN)
              .key(Pattern.PREVIOUS, previousCell)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.has(previousCell))
              .addCriterion(Criterion.has(energyCube))
              .build(consumer, Mekanism.rl(basePath + cell.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addInductionProviderRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Basic needs to be handled slightly differently
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.LITHIUM, Pattern.CIRCUIT, Pattern.LITHIUM))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.LITHIUM, MekanismTags.Items.DUSTS_LITHIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_ENERGY_CUBE)
              .addCriterion(Criterion.has(MekanismBlocks.BASIC_ENERGY_CUBE))
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer, Mekanism.rl(basePath + "basic"));
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.BASIC_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ADVANCED, Criterion.HAS_ADVANCED_CIRCUIT);
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ADVANCED_INDUCTION_PROVIDER, MekanismBlocks.ELITE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ELITE, Criterion.HAS_ELITE_CIRCUIT);
        addTieredInductionProviderRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER, MekanismBlocks.ELITE_INDUCTION_PROVIDER, MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismTags.Items.CIRCUITS_ULTIMATE, Criterion.HAS_ULTIMATE_CIRCUIT);
    }

    private void addTieredInductionProviderRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockInductionProvider, ?> provider,
          IItemProvider previousProvider, IItemProvider energyCube, Tag<Item> circuitTag, RecipeCriterion circuitCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(provider)
              .pattern(INDUCTION_PROVIDER_PATTERN)
              .key(Pattern.PREVIOUS, previousProvider)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.CIRCUIT, circuitTag)
              .addCriterion(Criterion.has(previousProvider))
              .addCriterion(Criterion.has(energyCube))
              .addCriterion(circuitCriterion)
              .build(consumer, Mekanism.rl(basePath + provider.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addInfusionConversionRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "infusion_conversion/";
        addInfusionConversionBioRecipes(consumer, basePath + "bio/");
        addInfusionConversionCarbonRecipes(consumer, basePath + "carbon/");
        addInfusionConversionDiamondRecipes(consumer, basePath + "diamond/");
        addInfusionConversionFungiRecipes(consumer, basePath + "fungi/");
        addInfusionConversionRedstoneRecipes(consumer, basePath + "redstone/");
        addInfusionConversionRefinedObsidianRecipes(consumer, basePath + "refined_obsidian/");
        addInfusionConversionTinRecipes(consumer, basePath + "tin/");
    }

    private void addInfusionConversionBioRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Bio fuel
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.FUELS_BIO),
              MekanismInfuseTypes.BIO.getInfusionStack(5)
        ).addCriterion(Criterion.has("bio_fuel", MekanismTags.Items.FUELS_BIO))
              .build(consumer, Mekanism.rl(basePath + "from_bio_fuel"));
    }

    private void addInfusionConversionCarbonRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Charcoal Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL),
              MekanismInfuseTypes.CARBON.getInfusionStack(180)
        ).addCriterion(Criterion.has("charcoal_block", MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL))
              .build(consumer, Mekanism.rl(basePath + "from_charcoal_block"));
        //Charcoal
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.CHARCOAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL)
              ),
              MekanismInfuseTypes.CARBON.getInfusionStack(20)
        ).addCriterion(Criterion.has("charcoal_dust", MekanismTags.Items.DUSTS_CHARCOAL))
              .addCriterion(Criterion.has(Items.CHARCOAL))
              .build(consumer, Mekanism.rl(basePath + "from_charcoal"));

        //Coal Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL),
              MekanismInfuseTypes.CARBON.getInfusionStack(90)
        ).addCriterion(Criterion.has("coal_block", Tags.Items.STORAGE_BLOCKS_COAL))
              .build(consumer, Mekanism.rl(basePath + "from_coal_block"));
        //Coal
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Items.COAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL)
              ),
              MekanismInfuseTypes.CARBON.getInfusionStack(10)
        ).addCriterion(Criterion.has("coal_dust", MekanismTags.Items.DUSTS_COAL))
              .addCriterion(Criterion.has(Items.COAL))
              .build(consumer, Mekanism.rl(basePath + "from_coal"));

        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_CARBON),
              MekanismInfuseTypes.CARBON.getInfusionStack(80)
        ).addCriterion(Criterion.has("enriched_carbon", MekanismTags.Items.ENRICHED_CARBON))
              .build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionDiamondRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getInfusionStack(10)
        ).addCriterion(Criterion.has("diamond_dust", MekanismTags.Items.DUSTS_DIAMOND))
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_DIAMOND),
              MekanismInfuseTypes.DIAMOND.getInfusionStack(80)
        ).addCriterion(Criterion.has("enriched_diamond", MekanismTags.Items.ENRICHED_DIAMOND))
              .build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionFungiRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Mushrooms
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.MUSHROOMS),
              MekanismInfuseTypes.FUNGI.getInfusionStack(10)
        ).addCriterion(Criterion.has("mushrooms", Tags.Items.MUSHROOMS))
              .build(consumer, Mekanism.rl(basePath + "from_mushrooms"));
    }

    private void addInfusionConversionRedstoneRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Block
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(90)
        ).addCriterion(Criterion.has("redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE))
              .build(consumer, Mekanism.rl(basePath + "from_block"));
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(10)
        ).addCriterion(Criterion.has("redstone_dust", Tags.Items.DUSTS_REDSTONE))
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_REDSTONE),
              MekanismInfuseTypes.REDSTONE.getInfusionStack(80)
        ).addCriterion(Criterion.has("enriched_redstone", MekanismTags.Items.ENRICHED_REDSTONE))
              .build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionRefinedObsidianRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getInfusionStack(10)
        ).addCriterion(Criterion.has("refined_obsidian_dust", MekanismTags.Items.DUSTS_REFINED_OBSIDIAN))
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_OBSIDIAN),
              MekanismInfuseTypes.REFINED_OBSIDIAN.getInfusionStack(80)
        ).addCriterion(Criterion.has("enriched_refined_obsidian", MekanismTags.Items.ENRICHED_OBSIDIAN))
              .build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addInfusionConversionTinRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_TIN),
              MekanismInfuseTypes.TIN.getInfusionStack(10)
        ).addCriterion(Criterion.has("tin_dust", MekanismTags.Items.DUSTS_TIN))
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //Enriched
        ItemStackToInfuseTypeRecipeBuilder.infusionConversion(
              ItemStackIngredient.from(MekanismTags.Items.ENRICHED_TIN),
              MekanismInfuseTypes.TIN.getInfusionStack(80)
        ).addCriterion(Criterion.has("enriched_tin", MekanismTags.Items.ENRICHED_TIN))
              .build(consumer, Mekanism.rl(basePath + "from_enriched"));
    }

    private void addChemicalInjectorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "injecting/";
        //Brick -> clay ball
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Tags.Items.INGOTS_BRICK),
              GasStackIngredient.from(MekanismGases.STEAM, 1),
              new ItemStack(Items.CLAY_BALL)
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "brick_to_clay_ball"));
        //Dirt -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Items.DIRT),
              GasStackIngredient.from(MekanismGases.STEAM, 1),
              new ItemStack(Items.CLAY)
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "dirt_to_clay"));
        //Gunpowder -> sulfur
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Tags.Items.GUNPOWDER),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Items.TERRACOTTA),
              GasStackIngredient.from(MekanismGases.STEAM, 1),
              new ItemStack(Items.CLAY)
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
    }

    private void addMetallurgicInfuserRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "metallurgic_infusing/";
        addMetallurgicInfuserAlloyRecipes(consumer, basePath + "alloy/");
        addMetallurgicInfuserMossyRecipes(consumer, basePath + "mossy/");
        //Dirt -> mycelium
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.DIRT),
              InfusionIngredient.from(MekanismTags.InfuseTypes.FUNGI, 10),
              new ItemStack(Items.MYCELIUM)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "dirt_to_mycelium"));
        //Dirt -> podzol
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.DIRT),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.PODZOL)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "dirt_to_podzol"));
        //Sand -> dirt
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.SAND),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.DIRT)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "sand_to_dirt"));
    }

    private void addMetallurgicInfuserAlloyRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Infused
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.INGOTS_IRON),
              InfusionIngredient.from(MekanismTags.InfuseTypes.REDSTONE, 10),
              MekanismItems.INFUSED_ALLOY.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "infused"));
        //Reinforced
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.ALLOYS_INFUSED),
              InfusionIngredient.from(MekanismTags.InfuseTypes.DIAMOND, 10),
              MekanismItems.REINFORCED_ALLOY.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer, Mekanism.rl(basePath + "reinforced"));
        //Atomic
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.ALLOYS_REINFORCED),
              InfusionIngredient.from(MekanismTags.InfuseTypes.REFINED_OBSIDIAN, 10),
              MekanismItems.ATOMIC_ALLOY.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .addCriterion(Criterion.HAS_REINFORCED_ALLOY)
              .build(consumer, Mekanism.rl(basePath + "atomic"));
    }

    private void addMetallurgicInfuserMossyRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Cobblestone
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.COBBLESTONE),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "cobblestone"));
        //Cobblestone slab
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.COBBLESTONE_SLAB),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_SLAB)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "cobblestone_slab"));
        //Cobblestone stairs
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.COBBLESTONE_STAIRS),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_STAIRS)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "cobblestone_stairs"));
        //Cobblestone wall
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.COBBLESTONE_WALL),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_COBBLESTONE_WALL)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "cobblestone_wall"));

        //Stone brick
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.STONE_BRICKS),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICKS)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "stone_brick"));
        //Stone brick slab
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.STONE_BRICK_SLAB),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_SLAB)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "stone_brick_slab"));
        //Stone brick stairs
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.STONE_BRICK_STAIRS),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_STAIRS)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "stone_brick_stairs"));
        //Stone brick wall
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.STONE_BRICK_WALL),
              InfusionIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
              new ItemStack(Items.MOSSY_STONE_BRICK_WALL)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "stone_brick_wall"));
    }

    private void addNuggetRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "nuggets/";
        addNuggetRecipe(consumer, MekanismItems.BRONZE_NUGGET, MekanismTags.Items.INGOTS_BRONZE, Criterion.HAS_BRONZE, basePath, "bronze");
        addNuggetRecipe(consumer, MekanismItems.COPPER_NUGGET, MekanismTags.Items.INGOTS_COPPER, Criterion.HAS_COPPER, basePath, "copper");
        addNuggetRecipe(consumer, MekanismItems.OSMIUM_NUGGET, MekanismTags.Items.INGOTS_OSMIUM, Criterion.HAS_OSMIUM, basePath, "osmium");
        addNuggetRecipe(consumer, MekanismItems.REFINED_GLOWSTONE_NUGGET, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, Criterion.HAS_REFINED_GLOWSTONE, basePath, "refined_glowstone");
        addNuggetRecipe(consumer, MekanismItems.REFINED_OBSIDIAN_NUGGET, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, Criterion.HAS_REFINED_OBSIDIAN, basePath, "refined_obsidian");
        addNuggetRecipe(consumer, MekanismItems.STEEL_NUGGET, MekanismTags.Items.INGOTS_STEEL, Criterion.HAS_STEEL, basePath, "steel");
        addNuggetRecipe(consumer, MekanismItems.TIN_NUGGET, MekanismTags.Items.INGOTS_TIN, Criterion.HAS_TIN, basePath, "tin");
    }

    private void addNuggetRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider nugget, Tag<Item> ingotTag, RecipeCriterion ingotCriterion, String basePath, String name) {
        ExtendedShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
              .addIngredient(ingotTag)
              .addCriterion(ingotCriterion)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addChemicalOxidizerRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "oxidizing/";
        //Brine
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SALT),
              MekanismGases.BRINE.getGasStack(15)
        ).addCriterion(Criterion.HAS_CHEMICAL_OXIDIZER)
              .build(consumer, Mekanism.rl(basePath + "brine"));
        //Lithium
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_LITHIUM),
              MekanismGases.LITHIUM.getGasStack(100)
        ).addCriterion(Criterion.HAS_CHEMICAL_OXIDIZER)
              .build(consumer, Mekanism.rl(basePath + "lithium"));
        //Sulfur dioxide
        ItemStackToGasRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_SULFUR),
              MekanismGases.SULFUR_DIOXIDE.getGasStack(100)
        ).addCriterion(Criterion.HAS_CHEMICAL_OXIDIZER)
              .build(consumer, Mekanism.rl(basePath + "sulfur_dioxide"));
    }

    private void addOreProcessingRecipes(Consumer<IFinishedRecipe> consumer) {
        //TODO: IMPLEMENT
    }

    private void addPurificationChamberRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "purifying/";
        //Gravel -> flint
        ItemStackGasToItemStackRecipeBuilder.purifying(
              ItemStackIngredient.from(Tags.Items.GRAVEL),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1),
              new ItemStack(Items.FLINT)
        ).addCriterion(Criterion.HAS_PURIFICATION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "gravel_to_flint"));
    }

    private void addPressurizedReactionChamberRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "reaction/";
        addWoodGasificationRecipes(consumer, basePath + "wood_gasification/");
        addSubstrateRecipes(consumer, basePath + "substrate/");
    }

    private void addWoodGasificationRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Blocks coal
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 1_000),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1_000),
              900,
              MekanismItems.SULFUR_DUST.getItemStack(9),
              MekanismGases.HYDROGEN.getGasStack(1_000)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "blocks_coals"));
        //Coals
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.COALS),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getGasStack(100)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "coals"));
        //Dusts coal
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL),
                    ItemStackIngredient.from(MekanismTags.Items.DUSTS_CHARCOAL)
              ),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              100,
              MekanismItems.SULFUR_DUST.getItemStack(),
              MekanismGases.HYDROGEN.getGasStack(100)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "dusts_coal"));
        //Dusts wood
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_WOOD),
              FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismGases.OXYGEN, 20),
              30,
              MekanismGases.HYDROGEN.getGasStack(20)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "dusts_wood"));
        //Logs
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.LOGS),
              FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismGases.OXYGEN, 100),
              150,
              MekanismGases.HYDROGEN.getGasStack(100)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "logs"));
        //Planks
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.PLANKS),
              FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismGases.OXYGEN, 20),
              30,
              MekanismGases.HYDROGEN.getGasStack(20)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "planks"));
        //Rods wooden
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(Tags.Items.RODS_WOODEN),
              FluidStackIngredient.from(FluidTags.WATER, 4),
              GasStackIngredient.from(MekanismGases.OXYGEN, 4),
              6,
              MekanismGases.HYDROGEN.getGasStack(4)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "rods_wooden"));
        //Slabs wooden
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(ItemTags.WOODEN_SLABS),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              15,
              MekanismGases.HYDROGEN.getGasStack(10)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "slabs_wooden"));
    }

    private void addSubstrateRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ethene + oxygen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(MekanismTags.Fluids.ETHENE, 50),
              GasStackIngredient.from(MekanismGases.OXYGEN, 10),
              60,
              MekanismItems.HDPE_PELLET.getItemStack(),
              MekanismGases.OXYGEN.getGasStack(5)
        ).energyRequired(1_000)
              .addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "ethene_oxygen"));
        //Water + ethene
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismItems.SUBSTRATE),
              FluidStackIngredient.from(FluidTags.WATER, 200),
              GasStackIngredient.from(MekanismGases.ETHENE, 100),
              400,
              MekanismItems.SUBSTRATE.getItemStack(8),
              MekanismGases.OXYGEN.getGasStack(10)
        ).energyRequired(200)
              .addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "water_ethene"));
        //Water + hydrogen
        PressurizedReactionRecipeBuilder.reaction(
              ItemStackIngredient.from(MekanismTags.Items.FUELS_BIO, 2),
              FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismGases.HYDROGEN, 100),
              100,
              MekanismItems.SUBSTRATE.getItemStack(),
              MekanismGases.ETHENE.getGasStack(100)
        ).addCriterion(Criterion.HAS_PRESSURIZED_REACTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "water_hydrogen"));
    }

    private void addRotaryCondensentratorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.BRINE, MekanismFluids.BRINE, MekanismTags.Fluids.BRINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.CHLORINE, MekanismFluids.CHLORINE, MekanismTags.Fluids.CHLORINE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.DEUTERIUM, MekanismFluids.DEUTERIUM, MekanismTags.Fluids.DEUTERIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.ETHENE, MekanismFluids.ETHENE, MekanismTags.Fluids.ETHENE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.FUSION_FUEL, MekanismFluids.FUSION_FUEL, MekanismTags.Fluids.FUSION_FUEL);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN, MekanismFluids.HYDROGEN, MekanismTags.Fluids.HYDROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE, MekanismTags.Fluids.HYDROGEN_CHLORIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.LITHIUM, MekanismFluids.LITHIUM, MekanismTags.Fluids.LITHIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.OXYGEN, MekanismFluids.OXYGEN, MekanismTags.Fluids.OXYGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SODIUM, MekanismFluids.SODIUM, MekanismTags.Fluids.SODIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.STEAM, MekanismFluids.STEAM, MekanismTags.Fluids.STEAM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE, MekanismTags.Fluids.SULFUR_DIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE, MekanismTags.Fluids.SULFUR_TRIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID, MekanismTags.Fluids.SULFURIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.TRITIUM, MekanismFluids.TRITIUM, MekanismTags.Fluids.TRITIUM);
    }

    private void addRotaryCondensentratorRecipe(Consumer<IFinishedRecipe> consumer, String basePath, IGasProvider gas, IFluidProvider fluidOutput, Tag<Fluid> fluidInput) {
        RotaryRecipeBuilder.rotary(
              FluidStackIngredient.from(fluidInput, 1),
              GasStackIngredient.from(gas, 1),
              gas.getGasStack(1),
              fluidOutput.getFluidStack(1)
        ).addCriterion(Criterion.HAS_ROTARY_CONDENSENTRATOR)
              .build(consumer, Mekanism.rl(basePath + gas.getName()));
    }

    private void addPrecisionSawmillRecipes(Consumer<IFinishedRecipe> consumer) {
        //TODO: Decide if we want to move fences, slabs, and stairs to individual wood type ones
        // or maybe move some from the wood types out like pressure plates
        String basePath = "sawing/";
        addPrecisionSawmillBedRecipes(consumer, basePath + "bed/");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.ACACIA_PLANKS, Items.ACACIA_BOAT, Items.ACACIA_DOOR, Items.ACACIA_FENCE_GATE, ItemTags.ACACIA_LOGS, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, "acacia");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.BIRCH_PLANKS, Items.BIRCH_BOAT, Items.BIRCH_DOOR, Items.BIRCH_FENCE_GATE, ItemTags.BIRCH_LOGS, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, "birch");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.DARK_OAK_PLANKS, Items.DARK_OAK_BOAT, Items.DARK_OAK_DOOR, Items.DARK_OAK_FENCE_GATE, ItemTags.DARK_OAK_LOGS, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR, "dark_oak");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.JUNGLE_PLANKS, Items.JUNGLE_BOAT, Items.JUNGLE_DOOR, Items.JUNGLE_FENCE_GATE, ItemTags.JUNGLE_LOGS, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, "jungle");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.OAK_PLANKS, Items.OAK_BOAT, Items.OAK_DOOR, Items.OAK_FENCE_GATE, ItemTags.OAK_LOGS, Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, "oak");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.SPRUCE_PLANKS, Items.SPRUCE_BOAT, Items.SPRUCE_DOOR, Items.SPRUCE_FENCE_GATE, ItemTags.SPRUCE_LOGS, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, "spruce");
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.BARREL),
              new ItemStack(Items.OAK_PLANKS, 7)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "barrel"));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.BOOKSHELF),
              new ItemStack(Items.OAK_PLANKS, 6),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "bookshelf"));
        //Chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.CHEST),
              new ItemStack(Items.OAK_PLANKS, 8)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "chest"));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.CRAFTING_TABLE),
              new ItemStack(Items.OAK_PLANKS, 4)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "crafting_table"));
        //Fences
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Tags.Items.FENCES_WOODEN),
              new ItemStack(Items.STICK, 3)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "fences"));
        //Jukebox
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.JUKEBOX),
              new ItemStack(Items.OAK_PLANKS, 8),
              new ItemStack(Items.DIAMOND),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "jukebox"));
        //Ladder
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.LADDER, 3),
              new ItemStack(Items.STICK, 7)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "ladder"));
        //Lectern
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.LECTERN),
              new ItemStack(Items.OAK_PLANKS, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "lectern"));
        //Note block
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.NOTE_BLOCK),
              new ItemStack(Items.OAK_PLANKS, 8),
              new ItemStack(Items.REDSTONE),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "note_block"));
        //Planks
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.PLANKS),
              new ItemStack(Items.STICK, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "planks"));
        //Redstone torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.REDSTONE_TORCH),
              new ItemStack(Items.STICK),
              new ItemStack(Items.REDSTONE),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "redstone_torch"));
        //Slabs
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.WOODEN_SLABS),
              new ItemStack(Items.STICK, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.125
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "slabs"));
        //Stairs
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(ItemTags.WOODEN_STAIRS),
              new ItemStack(Items.STICK, 9),
              MekanismItems.SAWDUST.getItemStack(),
              0.375
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "stairs"));
        //Stick
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Tags.Items.RODS_WOODEN),
              MekanismItems.SAWDUST.getItemStack()
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "stick"));
        //Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.TORCH, 4),
              new ItemStack(Items.STICK),
              new ItemStack(Items.COAL),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "torch"));
        //Trapped chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(Items.TRAPPED_CHEST),
              new ItemStack(Items.OAK_PLANKS, 8),
              new ItemStack(Items.TRIPWIRE_HOOK),
              0.75
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "trapped_chest"));
    }

    private void addPrecisionSawmillBedRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, Items.BLACK_WOOL, "black");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, Items.BLUE_WOOL, "blue");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, Items.BROWN_WOOL, "brown");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, Items.CYAN_WOOL, "cyan");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, Items.GRAY_WOOL, "gray");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, Items.GREEN_WOOL, "green");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_WOOL, "light_blue");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_WOOL, "light_gray");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, Items.LIME_WOOL, "lime");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, Items.MAGENTA_WOOL, "magenta");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, Items.ORANGE_WOOL, "orange");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, Items.PINK_WOOL, "pink");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, Items.PURPLE_WOOL, "purple");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, Items.RED_WOOL, "red");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, Items.WHITE_WOOL, "white");
        addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, Items.YELLOW_WOOL, "yellow");
    }

    private void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, String basePath, Item bed, Item wool, String name) {
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(bed),
              new ItemStack(Items.OAK_PLANKS, 3),
              new ItemStack(wool, 3),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, Item planks, Item boat, Item door, Item fenceGate, Tag<Item> log,
          Item pressurePlate, Item trapdoor, String name) {
        //Boat
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(boat),
              new ItemStack(planks, 5)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "boat/" + name));
        //Door
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(door),
              new ItemStack(planks, 2)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "door/" + name));
        //Fence Gate
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(fenceGate),
              new ItemStack(planks, 2),
              new ItemStack(Items.STICK, 4),
              1
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "fence_gate/" + name));
        //Log
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(log),
              new ItemStack(planks, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "log/" + name));
        //Pressure plate
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(pressurePlate),
              new ItemStack(planks, 2)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "pressure_plate/" + name));
        //Trapdoor
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(trapdoor),
              new ItemStack(planks, 3)
        ).addCriterion(Criterion.HAS_PRECISION_SAWMILL)
              .build(consumer, Mekanism.rl(basePath + "trapdoor/" + name));
    }

    private void addElectrolyticSeparatorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(MekanismTags.Fluids.BRINE, 10),
              MekanismGases.SODIUM.getGasStack(1),
              MekanismGases.CHLORINE.getGasStack(1)
        ).energyUsage(400)
              .addCriterion(Criterion.HAS_ELECTROLYTIC_SEPARATOR)
              .build(consumer, Mekanism.rl(basePath + "brine"));
        //Heavy water
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(MekanismTags.Fluids.HEAVY_WATER, 2),
              MekanismGases.DEUTERIUM.getGasStack(2),
              MekanismGases.OXYGEN.getGasStack(1)
        ).energyUsage(800)
              .addCriterion(Criterion.HAS_ELECTROLYTIC_SEPARATOR)
              .build(consumer, Mekanism.rl(basePath + "heavy_water"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(FluidTags.WATER, 2),
              MekanismGases.HYDROGEN.getGasStack(2),
              MekanismGases.OXYGEN.getGasStack(1)
        ).energyUsage(400)
              .addCriterion(Criterion.HAS_ELECTROLYTIC_SEPARATOR)
              .build(consumer, Mekanism.rl(basePath + "water"));
    }

    private void addStorageBlockRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "storage_blocks/";
        addStorageBlockRecipe(consumer, MekanismBlocks.BRONZE_BLOCK, MekanismTags.Items.INGOTS_BRONZE, Criterion.HAS_BRONZE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.COPPER_BLOCK, MekanismTags.Items.INGOTS_COPPER, Criterion.HAS_COPPER, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.OSMIUM_BLOCK, MekanismTags.Items.INGOTS_OSMIUM, Criterion.HAS_OSMIUM, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE, Criterion.HAS_REFINED_GLOWSTONE, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN, Criterion.HAS_REFINED_OBSIDIAN, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.STEEL_BLOCK, MekanismTags.Items.INGOTS_STEEL, Criterion.HAS_STEEL, basePath);
        addStorageBlockRecipe(consumer, MekanismBlocks.TIN_BLOCK, MekanismTags.Items.INGOTS_TIN, Criterion.HAS_TIN, basePath);
        //Charcoal
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARCOAL_BLOCK)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, Items.CHARCOAL)
              .addCriterion(Criterion.has(Items.CHARCOAL))
              .build(consumer, Mekanism.rl(basePath + MekanismBlocks.CHARCOAL_BLOCK.getBlock().getResourceInfo().getRegistrySuffix()));
        //Salt
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SALT_BLOCK)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.DUSTS_SALT)
              .addCriterion(Criterion.has("salt", MekanismTags.Items.DUSTS_SALT))
              .build(consumer, Mekanism.rl(basePath + "salt"));
    }

    private void addStorageBlockRecipe(Consumer<IFinishedRecipe> consumer, BlockRegistryObject<BlockResource, ?> block, Tag<Item> ingotTag, RecipeCriterion ingotCriterion,
          String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(block)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, ingotTag)
              .addCriterion(ingotCriterion)
              .build(consumer, Mekanism.rl(basePath + block.getBlock().getResourceInfo().getRegistrySuffix()));
    }

    private void addThermalEvaporationRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "thermal_evaporation/";
        //Block
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_BLOCK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_COPPER)
              .addCriterion(Criterion.HAS_STEEL)
              .addCriterion(Criterion.HAS_COPPER)
              .build(consumer, Mekanism.rl(basePath + "block"));
        RecipeCriterion hasBlock = Criterion.has(MekanismBlocks.THERMAL_EVAPORATION_BLOCK);
        //Controller
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, GLASS_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.BUCKET, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.BUCKET, Items.BUCKET)
              .key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .addCriterion(Criterion.HAS_ADVANCED_CIRCUIT)
              .addCriterion(hasBlock)
              .build(consumer, Mekanism.rl(basePath + "controller"));
        //Valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.THERMAL_EVAPORATION_VALVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.THERMAL_EVAPORATION_BLOCK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .addCriterion(Criterion.HAS_ADVANCED_CIRCUIT)
              .addCriterion(hasBlock)
              .build(consumer, Mekanism.rl(basePath + "valve"));
    }

    private void addTierInstallerRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "tier_installer/";
        addTierInstallerRecipe(consumer, basePath, MekanismItems.BASIC_TIER_INSTALLER, Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC, Criterion.HAS_BASIC_CIRCUIT);
        addTierInstallerRecipe(consumer, basePath, MekanismItems.ADVANCED_TIER_INSTALLER, MekanismTags.Items.INGOTS_OSMIUM, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED, Criterion.HAS_ADVANCED_CIRCUIT);
        addTierInstallerRecipe(consumer, basePath, MekanismItems.ELITE_TIER_INSTALLER, Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE, Criterion.HAS_ELITE_CIRCUIT);
        addTierInstallerRecipe(consumer, basePath, MekanismItems.ULTIMATE_TIER_INSTALLER, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE, Criterion.HAS_ULTIMATE_CIRCUIT);
    }

    private void addTierInstallerRecipe(Consumer<IFinishedRecipe> consumer, String basePath, ItemRegistryObject<ItemTierInstaller> tierInstaller, Tag<Item> ingotTag,
          Tag<Item> alloyTag, Tag<Item> circuitTag, RecipeCriterion circuitCriterion) {
        ItemTierInstaller tierInstallerItem = tierInstaller.getItem();
        BaseTier fromTier = tierInstallerItem.getFromTier();
        String toTierName = tierInstallerItem.getToTier().getLowerName();
        String name = toTierName;//fromTier == null ? toTierName : fromTier.getLowerName() + "_to_" + toTierName;
        //TODO: Support the previous being things like a previous tier's installer based on fromTier, and when doing so also uncomment the name calculation above
        ExtendedShapedRecipeBuilder.shapedRecipe(tierInstaller)
              .pattern(TIER_PATTERN)
              .key(Pattern.PREVIOUS, ItemTags.PLANKS)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(circuitCriterion)
              .build(consumer, Mekanism.rl(basePath + name));
    }

    private void addTransmitterRecipes(Consumer<IFinishedRecipe> consumer) {
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
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "diversion_transporter"));
        //Restrictive
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESTRICTIVE_TRANSPORTER)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, Items.IRON_BARS)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "restrictive_transporter"));
    }

    private void addLogisticalTransporterRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, Tags.Items.DUSTS_REDSTONE);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER, MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addMechanicalPipeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_MECHANICAL_PIPE, Items.BUCKET);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismBlocks.BASIC_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismBlocks.ADVANCED_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_MECHANICAL_PIPE, MekanismBlocks.ELITE_MECHANICAL_PIPE, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addPressurizedTubeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_PRESSURIZED_TUBE, Tags.Items.GLASS);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismBlocks.BASIC_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismBlocks.ADVANCED_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE, MekanismBlocks.ELITE_PRESSURIZED_TUBE, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addThermodynamicConductorRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.INGOTS_COPPER);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR, MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addUniversalCableRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismTags.Items.INGOTS_STEEL);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addBasicTransmitterRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITieredBlock<?>, ?> transmitter,
          Tag<Item> itemTag) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, itemTag)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + transmitter.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addBasicTransmitterRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITieredBlock<?>, ?> transmitter,
          Item item) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, item)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + transmitter.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addTransmitterUpgradeRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITieredBlock<?>, ?> transmitter,
          IItemProvider previousTransmitter, Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter)
              .pattern(TRANSMITTER_UPGRADE_PATTERN)
              .key(Pattern.PREVIOUS, previousTransmitter)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.has(previousTransmitter))
              .addCriterion(alloyCriterion)
              .build(consumer, Mekanism.rl(basePath + transmitter.getBlock().getTier().getBaseTier().getLowerName()));
    }

    private void addUpgradeRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "upgrade/";
        addUpgradeRecipe(consumer, MekanismItems.ANCHOR_UPGRADE, MekanismTags.Items.DUSTS_DIAMOND, basePath);
        addUpgradeRecipe(consumer, MekanismItems.ENERGY_UPGRADE, MekanismTags.Items.DUSTS_GOLD, basePath);
        addUpgradeRecipe(consumer, MekanismItems.FILTER_UPGRADE, MekanismTags.Items.DUSTS_TIN, basePath);
        addUpgradeRecipe(consumer, MekanismItems.GAS_UPGRADE, MekanismTags.Items.DUSTS_IRON, basePath);
        addUpgradeRecipe(consumer, MekanismItems.MUFFLING_UPGRADE, MekanismTags.Items.DUSTS_STEEL, basePath);
        addUpgradeRecipe(consumer, MekanismItems.SPEED_UPGRADE, MekanismTags.Items.DUSTS_OSMIUM, basePath);
    }

    private void addUpgradeRecipe(Consumer<IFinishedRecipe> consumer, ItemRegistryObject<ItemUpgrade> upgrade, Tag<Item> dustTag, String basePath) {
        ExtendedShapedRecipeBuilder.shapedRecipe(upgrade)
              .pattern(UPGRADE_PATTERN)
              .key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CONSTANT, dustTag)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer, Mekanism.rl(basePath + upgrade.getItem().getUpgradeType(upgrade.getItemStack()).getRawName()));
    }

    private void addMiscRecipes(Consumer<IFinishedRecipe> consumer) {
        //TODO: IMPLEMENT
    }
}