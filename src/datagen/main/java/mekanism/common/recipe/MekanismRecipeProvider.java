package mekanism.common.recipe;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.machine.BlockFluidTank;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.recipe.RecipePattern.TripleLine;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
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
    private static final RecipePattern TIER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));

    public MekanismRecipeProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    //TODO: Implement all the recipes using data generators
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

    private void addTieredBin(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockBin, ItemBlockBin> bin, IItemProvider previousBin,
          Tag<Item> circuitTag, Tag<Item> alloyTag, RecipeCriterion circuitCriterion) {
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

    private void addTieredEnergyCube(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> energyCube,
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

    private void addBasicFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory, IItemProvider toUpgrade) {
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

    private void addAdvancedFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory, IItemProvider toUpgrade) {
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

    private void addEliteFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory, IItemProvider toUpgrade) {
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

    private void addUltimateFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory, IItemProvider toUpgrade) {
        addFactoryRecipe(consumer, basePath, factory, toUpgrade, Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
    }

    private void addFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory, ItemBlockFactory> factory,
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

    private void addTieredFluidTank(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> bin, IItemProvider previousTank,
          Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        ExtendedShapedRecipeBuilder.shapedRecipe(bin)
              .pattern(FLUID_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(alloyCriterion)
              .addCriterion(Criterion.has(previousTank))
              .build(consumer, Mekanism.rl(basePath + bin.getBlock().getTier().getBaseTier().getLowerName()));
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

    }

    private void addInductionRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addInfusionConversionRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addChemicalInjectorRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addMetallurgicInfuserRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addNuggetRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addChemicalOxidizerRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addOreProcessingRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addPurificationChamberRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addPressurizedReactionChamberRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addRotaryCondensentratorRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addPrecisionSawmillRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addElectrolyticSeparatorRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addStorageBlockRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addThermalEvaporationRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addTierInstallerRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addTransmitterRecipes(Consumer<IFinishedRecipe> consumer) {

    }

    private void addUpgradeRecipes(Consumer<IFinishedRecipe> consumer) {

    }
}