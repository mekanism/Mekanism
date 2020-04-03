package mekanism.common.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidGasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidToFluidRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackGasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackGasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToEnergyRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToInfuseTypeRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.machine.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.recipe.RecipePattern.DoubleLine;
import mekanism.common.recipe.RecipePattern.TripleLine;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.builder.SpecialRecipeBuilder;
import mekanism.common.recipe.compat.BiomesOPlentyRecipeProvider;
import mekanism.common.recipe.compat.CompatRecipeProvider;
import mekanism.common.recipe.compat.ILikeWoodRecipeProvider;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tier.FactoryTier;
import net.minecraft.block.ComposterBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.Tags;

//TODO: Figure out what we want to do with compat recipes
//TODO: Clean this up some to use methods in places that the code is just copy pasted, example: circuits
// and some of the different ore processing code
@ParametersAreNonnullByDefault
public class MekanismRecipeProvider extends BaseRecipeProvider {

    private static final char DIAMOND_CHAR = 'D';
    private static final char GLASS_CHAR = 'G';
    private static final char PERSONAL_CHEST_CHAR = 'P';
    private static final char ROBIT_CHAR = 'R';
    private static final char SORTER_CHAR = 'S';
    private static final char TELEPORTATION_CORE_CHAR = 'T';

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

    private static List<CompatRecipeProvider> compatRecipeProviders = Arrays.asList(
          new BiomesOPlentyRecipeProvider(),
          new ILikeWoodRecipeProvider()
    );

    public MekanismRecipeProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        //Special recipes (bins)
        SpecialRecipeBuilder.build(consumer, MekanismRecipeSerializers.BIN_INSERT);
        SpecialRecipeBuilder.build(consumer, MekanismRecipeSerializers.BIN_EXTRACT);
        //Mod compat recipes
        compatRecipeProviders.forEach(compatRecipeProvider -> compatRecipeProvider.registerRecipes(consumer));
        addBinRecipes(consumer);
        addChemicalInfuserRecipes(consumer);
        addCombinerRecipes(consumer);
        addControlCircuitRecipes(consumer);
        addCrusherRecipes(consumer);
        addChemicalCrystallizerRecipes(consumer);
        addEnergyConversionRecipes(consumer);
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
        addUraniumRecipes(consumer);
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
        String tierName = Attribute.getBaseTier(bin.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(bin)
              .pattern(BIN_PATTERN)
              .key(Pattern.PREVIOUS, previousBin)
              .key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(circuitCriterion)
              .addCriterion(Criterion.has(previousBin))
              .build(consumer, Mekanism.rl(basePath + tierName));
    }

    private void addChemicalInfuserRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
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
              GasStackIngredient.from(MekanismGases.WATER_VAPOR, 1),
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

    private void addCrusherBioFuelRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Generate baseline recipes from Composter recipe set
        for (Entry<net.minecraft.util.IItemProvider> chance : ComposterBlock.CHANCES.object2FloatEntrySet()) {
            ItemStackToItemStackRecipeBuilder.crushing(
                  ItemStackIngredient.from(chance.getKey().asItem()),
                  MekanismItems.BIO_FUEL.getItemStack(Math.round(chance.getFloatValue() * 8))
            ).addCriterion(Criterion.HAS_CRUSHER)
                  .build(consumer, Mekanism.rl(basePath + chance.getKey().asItem().toString()));
        }
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

    private void addEnergyConversionRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "energy_conversion/";
        FloatingLong redstoneEnergy = FloatingLong.createConst(10_000);
        addEnergyConversionRecipe(consumer, basePath, "redstone", Tags.Items.DUSTS_REDSTONE, redstoneEnergy);
        addEnergyConversionRecipe(consumer, basePath, "redstone_block", Tags.Items.STORAGE_BLOCKS_REDSTONE, redstoneEnergy.multiply(9));
    }

    private void addEnergyConversionRecipe(Consumer<IFinishedRecipe> consumer, String basePath, String name, Tag<Item> inputTag, FloatingLong output) {
        ItemStackToEnergyRecipeBuilder.energyConversion(
              ItemStackIngredient.from(inputTag),
              output
        ).addCriterion(Criterion.has(name, inputTag))
              .build(consumer, Mekanism.rl(basePath + name));
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
        String tierName = Attribute.getBaseTier(energyCube.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(energyCube)
              .pattern(ENERGY_CUBE_PATTERN)
              .key(Pattern.PREVIOUS, previousEnergyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .addCriterion(Criterion.has(previousEnergyCube))
              .build(consumer, Mekanism.rl(basePath + tierName));
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
        for (FactoryType type : FactoryType.values()) {
            addFactoryRecipe(consumer, basePath, MekanismBlocks.getFactory(FactoryTier.BASIC, type), type.getBaseBlock(), Tags.Items.INGOTS_IRON, MekanismTags.Items.ALLOYS_BASIC, MekanismTags.Items.CIRCUITS_BASIC);
        }
    }

    private void addAdvancedFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        for (FactoryType type : FactoryType.values()) {
            addFactoryRecipe(consumer, basePath, MekanismBlocks.getFactory(FactoryTier.ADVANCED, type), MekanismBlocks.getFactory(FactoryTier.BASIC, type), MekanismTags.Items.INGOTS_OSMIUM, MekanismTags.Items.ALLOYS_INFUSED, MekanismTags.Items.CIRCUITS_ADVANCED);
        }
    }

    private void addEliteFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        for (FactoryType type : FactoryType.values()) {
            addFactoryRecipe(consumer, basePath, MekanismBlocks.getFactory(FactoryTier.ELITE, type), MekanismBlocks.getFactory(FactoryTier.ADVANCED, type), Tags.Items.INGOTS_GOLD, MekanismTags.Items.ALLOYS_REINFORCED, MekanismTags.Items.CIRCUITS_ELITE);
        }
    }

    private void addUltimateFactoryRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        for (FactoryType type : FactoryType.values()) {
            addFactoryRecipe(consumer, basePath, MekanismBlocks.getFactory(FactoryTier.ULTIMATE, type), MekanismBlocks.getFactory(FactoryTier.ELITE, type), Tags.Items.GEMS_DIAMOND, MekanismTags.Items.ALLOYS_ATOMIC, MekanismTags.Items.CIRCUITS_ULTIMATE);
        }
    }

    private void addFactoryRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<BlockFactory<?>, ?> factory,
          IItemProvider toUpgrade, Tag<Item> ingotTag, Tag<Item> alloyTag, Tag<Item> circuitTag) {
        MekDataShapedRecipeBuilder.shapedRecipe(factory)
              .pattern(TIER_PATTERN)
              .key(Pattern.PREVIOUS, toUpgrade)
              .key(Pattern.CIRCUIT, circuitTag)
              .key(Pattern.INGOT, ingotTag)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.has(toUpgrade))
              .build(consumer, Mekanism.rl(basePath + Attribute.get(factory.getBlock(), AttributeFactoryType.class).getFactoryType().getRegistryNameComponent()));
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
        String tierName = Attribute.getBaseTier(tank.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(FLUID_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(alloyCriterion)
              .addCriterion(Criterion.has(previousTank))
              .build(consumer, Mekanism.rl(basePath + tierName));
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

    private void addTieredGasTank(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> tank, IItemProvider previousTank,
          Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        String tierName = Attribute.getBaseTier(tank.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(tank)
              .pattern(GAS_TANK_PATTERN)
              .key(Pattern.PREVIOUS, previousTank)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(alloyCriterion)
              .addCriterion(Criterion.has(previousTank))
              .build(consumer, Mekanism.rl(basePath + tierName));
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
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BASIC_INDUCTION_CELL)
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

    private void addTieredInductionCellRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> cell,
          IItemProvider previousCell, IItemProvider energyCube) {
        String tierName = Attribute.getBaseTier(cell.getBlock()).getLowerName();
        MekDataShapedRecipeBuilder.shapedRecipe(cell)
              .pattern(INDUCTION_CELL_PATTERN)
              .key(Pattern.PREVIOUS, previousCell)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.has(previousCell))
              .addCriterion(Criterion.has(energyCube))
              .build(consumer, Mekanism.rl(basePath + tierName));
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

    private void addTieredInductionProviderRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> provider,
          IItemProvider previousProvider, IItemProvider energyCube, Tag<Item> circuitTag, RecipeCriterion circuitCriterion) {
        String tierName = Attribute.getBaseTier(provider.getBlock()).getLowerName();
        ExtendedShapedRecipeBuilder.shapedRecipe(provider)
              .pattern(INDUCTION_PROVIDER_PATTERN)
              .key(Pattern.PREVIOUS, previousProvider)
              .key(Pattern.CONSTANT, energyCube)
              .key(Pattern.CIRCUIT, circuitTag)
              .addCriterion(Criterion.has(previousProvider))
              .addCriterion(Criterion.has(energyCube))
              .addCriterion(circuitCriterion)
              .build(consumer, Mekanism.rl(basePath + tierName));
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
              GasStackIngredient.from(MekanismGases.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY_BALL)
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "brick_to_clay_ball"));
        //Dirt -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Items.DIRT),
              GasStackIngredient.from(MekanismGases.WATER_VAPOR, 1),
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
              GasStackIngredient.from(MekanismGases.WATER_VAPOR, 1),
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
        String basePath = "processing/";
        addOreProcessingIngotRecipes(consumer, basePath + "copper/", MekanismBlocks.COPPER_ORE, MekanismTags.Items.ORES_COPPER, MekanismGases.COPPER_SLURRY,
              MekanismItems.COPPER_CRYSTAL, MekanismTags.Items.CRYSTALS_COPPER, MekanismItems.COPPER_SHARD, MekanismTags.Items.SHARDS_COPPER, MekanismItems.COPPER_CLUMP,
              MekanismTags.Items.CLUMPS_COPPER, MekanismItems.DIRTY_COPPER_DUST, MekanismTags.Items.DIRTY_DUSTS_COPPER, MekanismItems.COPPER_DUST,
              MekanismTags.Items.DUSTS_COPPER, MekanismItems.COPPER_INGOT, MekanismTags.Items.INGOTS_COPPER, MekanismTags.Items.NUGGETS_COPPER,
              MekanismTags.Items.STORAGE_BLOCKS_COPPER, true);
        addOreProcessingIngotRecipes(consumer, basePath + "osmium/", MekanismBlocks.OSMIUM_ORE, MekanismTags.Items.ORES_OSMIUM, MekanismGases.OSMIUM_SLURRY,
              MekanismItems.OSMIUM_CRYSTAL, MekanismTags.Items.CRYSTALS_OSMIUM, MekanismItems.OSMIUM_SHARD, MekanismTags.Items.SHARDS_OSMIUM, MekanismItems.OSMIUM_CLUMP,
              MekanismTags.Items.CLUMPS_OSMIUM, MekanismItems.DIRTY_OSMIUM_DUST, MekanismTags.Items.DIRTY_DUSTS_OSMIUM, MekanismItems.OSMIUM_DUST,
              MekanismTags.Items.DUSTS_OSMIUM, MekanismItems.OSMIUM_INGOT, MekanismTags.Items.INGOTS_OSMIUM, MekanismTags.Items.NUGGETS_OSMIUM,
              MekanismTags.Items.STORAGE_BLOCKS_OSMIUM, true);
        addOreProcessingIngotRecipes(consumer, basePath + "tin/", MekanismBlocks.TIN_ORE, MekanismTags.Items.ORES_TIN, MekanismGases.TIN_SLURRY,
              MekanismItems.TIN_CRYSTAL, MekanismTags.Items.CRYSTALS_TIN, MekanismItems.TIN_SHARD, MekanismTags.Items.SHARDS_TIN, MekanismItems.TIN_CLUMP,
              MekanismTags.Items.CLUMPS_TIN, MekanismItems.DIRTY_TIN_DUST, MekanismTags.Items.DIRTY_DUSTS_TIN, MekanismItems.TIN_DUST,
              MekanismTags.Items.DUSTS_TIN, MekanismItems.TIN_INGOT, MekanismTags.Items.INGOTS_TIN, MekanismTags.Items.NUGGETS_TIN,
              MekanismTags.Items.STORAGE_BLOCKS_TIN, true);
        addOreProcessingIngotRecipes(consumer, basePath + "iron/", Items.IRON_ORE, Tags.Items.ORES_IRON, MekanismGases.IRON_SLURRY,
              MekanismItems.IRON_CRYSTAL, MekanismTags.Items.CRYSTALS_IRON, MekanismItems.IRON_SHARD, MekanismTags.Items.SHARDS_IRON, MekanismItems.IRON_CLUMP,
              MekanismTags.Items.CLUMPS_IRON, MekanismItems.DIRTY_IRON_DUST, MekanismTags.Items.DIRTY_DUSTS_IRON, MekanismItems.IRON_DUST,
              MekanismTags.Items.DUSTS_IRON, Items.IRON_INGOT, Tags.Items.INGOTS_IRON, Tags.Items.NUGGETS_IRON, Tags.Items.STORAGE_BLOCKS_IRON, false);
        addOreProcessingIngotRecipes(consumer, basePath + "gold/", Items.GOLD_ORE, Tags.Items.ORES_GOLD, MekanismGases.GOLD_SLURRY,
              MekanismItems.GOLD_CRYSTAL, MekanismTags.Items.CRYSTALS_GOLD, MekanismItems.GOLD_SHARD, MekanismTags.Items.SHARDS_GOLD, MekanismItems.GOLD_CLUMP,
              MekanismTags.Items.CLUMPS_GOLD, MekanismItems.DIRTY_GOLD_DUST, MekanismTags.Items.DIRTY_DUSTS_GOLD, MekanismItems.GOLD_DUST,
              MekanismTags.Items.DUSTS_GOLD, Items.GOLD_INGOT, Tags.Items.INGOTS_GOLD, Tags.Items.NUGGETS_GOLD, Tags.Items.STORAGE_BLOCKS_GOLD, false);
        //Iron -> enriched iron
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.INGOTS_IRON),
              InfusionIngredient.from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.ENRICHED_IRON.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "iron/enriched"));
        addBronzeProcessingRecipes(consumer, basePath + "bronze/");
        addCoalOreProcessingRecipes(consumer, basePath + "coal/");
        addOreProcessingGemRecipes(consumer, basePath + "diamond/", Items.DIAMOND_ORE, Tags.Items.ORES_DIAMOND, MekanismItems.DIAMOND_DUST,
              MekanismTags.Items.DUSTS_DIAMOND, Items.DIAMOND, Tags.Items.GEMS_DIAMOND, 2, 3, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "emerald/", Items.EMERALD_ORE, Tags.Items.ORES_EMERALD, MekanismItems.EMERALD_DUST,
              MekanismTags.Items.DUSTS_EMERALD, Items.EMERALD, Tags.Items.GEMS_EMERALD, 2, 3, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "lapis_lazuli/", Items.LAPIS_ORE, Tags.Items.ORES_LAPIS, MekanismItems.LAPIS_LAZULI_DUST,
              MekanismTags.Items.DUSTS_LAPIS_LAZULI, Items.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, 12, 16, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "quartz/", Items.NETHER_QUARTZ_ORE, Tags.Items.ORES_QUARTZ, MekanismItems.QUARTZ_DUST,
              MekanismTags.Items.DUSTS_QUARTZ, Items.QUARTZ, Tags.Items.GEMS_QUARTZ, 6, 8, Tags.Items.NETHERRACK);
        addOreProcessingGemRecipes(consumer, basePath + "fluorite/", MekanismBlocks.FLUORITE_ORE, MekanismTags.Items.ORES_FLUORITE, MekanismItems.FLUORITE_DUST,
              MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, 6, 8, Tags.Items.COBBLESTONE);
        addRedstoneProcessingRecipes(consumer, basePath + "redstone/");
        addRefinedGlowstoneProcessingRecipes(consumer, basePath + "refined_glowstone/");
        addRefinedObsidianProcessingRecipes(consumer, basePath + "refined_obsidian/");
        addSteelProcessingRecipes(consumer, basePath + "steel/");
    }

    private void addOreProcessingIngotRecipes(Consumer<IFinishedRecipe> consumer, String basePath, net.minecraft.util.IItemProvider ore, Tag<Item> oreTag,
          SlurryRegistryObject<?, ?> slurry, IItemProvider crystal, Tag<Item> crystalTag, IItemProvider shard, Tag<Item> shardTag, IItemProvider clump, Tag<Item> clumpTag,
          IItemProvider dirtyDust, Tag<Item> dirtyDustTag, IItemProvider dust, Tag<Item> dustTag, net.minecraft.util.IItemProvider ingot, Tag<Item> ingotTag,
          Tag<Item> nuggetTag, Tag<Item> blockTag, boolean notVanilla) {
        RecipeCriterion hasOre = Criterion.has(ore.asItem().getRegistryName().getPath(), oreTag);
        //TODO: Decide if we want to move blocks/nuggets here?
        //Clump
        //from ore
        ItemStackGasToItemStackRecipeBuilder.purifying(
              ItemStackIngredient.from(oreTag),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1),
              clump.getItemStack(3)
        ).addCriterion(Criterion.HAS_PURIFICATION_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "clump/from_ore"));
        //from shard
        ItemStackGasToItemStackRecipeBuilder.purifying(
              ItemStackIngredient.from(shardTag),
              GasStackIngredient.from(MekanismGases.OXYGEN, 1),
              clump.getItemStack()
        ).addCriterion(Criterion.HAS_PURIFICATION_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "clump/from_shard"));
        //Crystal
        //from slurry
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(slurry.getCleanSlurry(), 200),
              crystal.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_CRYSTALLIZER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "crystal/from_slurry"));
        //Dirty Dust
        //from clump
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(clumpTag),
              dirtyDust.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "dirty_dust/from_clump"));
        //Dust
        //from dirty dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(dirtyDustTag),
              dust.getItemStack()
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "dust/from_dirty_dust"));
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(ingotTag),
              dust.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(oreTag),
              dust.getItemStack(2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "dust/from_ore"));
        //Ingot
        //from dust
        addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(dustTag), ingot, 0.5F, 200, Mekanism.rl(basePath + "ingot/from_dust_blasting"),
              Mekanism.rl(basePath + "ingot/from_dust_smelting"), hasOre);
        if (notVanilla) {
            //from block
            ExtendedShapelessRecipeBuilder.shapelessRecipe(ingot, 9)
                  .addIngredient(blockTag)
                  .addCriterion(hasOre)
                  .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
            //from nuggets
            ExtendedShapedRecipeBuilder.shapedRecipe(ingot)
                  .pattern(STORAGE_PATTERN)
                  .key(Pattern.CONSTANT, nuggetTag)
                  .addCriterion(hasOre)
                  .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
            //from ore
            addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(oreTag), ingot, 1, 200, Mekanism.rl(basePath + "ingot/from_ore_blasting"),
                  Mekanism.rl(basePath + "ingot/from_ore_smelting"), hasOre);
        }
        //Ore
        //from dust
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(dustTag, 8),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(ore)
        ).addCriterion(Criterion.HAS_COMBINER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "ore/from_dust"));
        //Shard
        //from crystal
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(crystalTag),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              shard.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "shard/from_crystal"));
        //from ore
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(oreTag),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              shard.getItemStack(4)
        ).addCriterion(Criterion.HAS_CHEMICAL_INJECTION_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "shard/from_ore"));
        //Slurry
        //clean
        FluidGasToGasRecipeBuilder.washing(
              FluidStackIngredient.from(FluidTags.WATER, 5),
              GasStackIngredient.from(slurry.getDirtySlurry(), 1),
              slurry.getCleanSlurry().getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_WASHER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "slurry/clean"));
        //dirty
        ItemStackGasToGasRecipeBuilder.dissolution(
              ItemStackIngredient.from(oreTag),
              GasStackIngredient.from(MekanismGases.SULFURIC_ACID, 1),
              slurry.getDirtySlurry().getGasStack(1_000)
        ).addCriterion(Criterion.HAS_CHEMICAL_DISSOLUTION_CHAMBER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "slurry/dirty"));
    }

    private void addCoalOreProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        RecipeCriterion hasCoalDust = Criterion.has("coal_dust", MekanismTags.Items.DUSTS_COAL);
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL),
              new ItemStack(Items.COAL)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .addCriterion(hasCoalDust)
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.ORES_COAL),
              new ItemStack(Items.COAL, 2)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .addCriterion(Criterion.has("coal_dust", Tags.Items.ORES_COAL))
              .build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.COAL),
              MekanismItems.COAL_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(Criterion.has(Items.COAL))
              .build(consumer, Mekanism.rl(basePath + "to_dust"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL, 3),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.COAL_ORE)
        ).addCriterion(Criterion.HAS_COMBINER)
              .addCriterion(hasCoalDust)
              .build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addOreProcessingGemRecipes(Consumer<IFinishedRecipe> consumer, String basePath, net.minecraft.util.IItemProvider ore, Tag<Item> oreTag,
          IItemProvider dust, Tag<Item> dustTag, net.minecraft.util.IItemProvider gem, Tag<Item> gemTag, int fromOre, int toOre, Tag<Item> combineType) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(dustTag),
              new ItemStack(gem)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(oreTag),
              new ItemStack(gem, fromOre)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(gemTag),
              dust.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "to_dust"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(dustTag, toOre),
              ItemStackIngredient.from(combineType),
              new ItemStack(ore)
        ).addCriterion(Criterion.HAS_COMBINER)
              .build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addBronzeProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        //from infusing
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_COPPER, 3),
              InfusionIngredient.from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_DUST.getItemStack(4)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .addCriterion(Criterion.has("copper_dust", MekanismTags.Items.DUSTS_COPPER))
              .addCriterion(Criterion.has("tin_dust", MekanismTags.Items.DUSTS_TIN))
              .build(consumer, Mekanism.rl(basePath + "dust/from_infusing"));
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_BRONZE),
              MekanismItems.BRONZE_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(Criterion.HAS_TIN)
              .addCriterion(Criterion.HAS_COPPER)
              .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.BRONZE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_BRONZE)
              .addCriterion(Criterion.has("bronze_block", MekanismTags.Items.STORAGE_BLOCKS_BRONZE))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(MekanismTags.Items.DUSTS_BRONZE), MekanismItems.BRONZE_INGOT, 0.5F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"),
              Criterion.has("bronze_dust", MekanismTags.Items.DUSTS_BRONZE));
        //from infusing
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_COPPER, 3),
              InfusionIngredient.from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_INGOT.getItemStack(4)
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .addCriterion(Criterion.HAS_TIN)
              .addCriterion(Criterion.HAS_COPPER)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_infusing"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BRONZE_INGOT)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_BRONZE)
              .addCriterion(Criterion.has("bronze_nugget", MekanismTags.Items.NUGGETS_BRONZE))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addRedstoneProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.ORES_REDSTONE),
              new ItemStack(Items.REDSTONE, 12)
        ).addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .addCriterion(Criterion.has("redstone_ore", Tags.Items.ORES_REDSTONE))
              .build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE, 16),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Items.REDSTONE_ORE)
        ).addCriterion(Criterion.HAS_COMBINER)
              .addCriterion(Criterion.has("redstone", Tags.Items.DUSTS_REDSTONE))
              .build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addRefinedGlowstoneProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .addCriterion(Criterion.has("refined_glowstone_block", MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackGasToItemStackRecipeBuilder.compressing(
              ItemStackIngredient.from(Tags.Items.DUSTS_GLOWSTONE),
              GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
              MekanismItems.REFINED_GLOWSTONE_INGOT.getItemStack()
        ).addCriterion(Criterion.HAS_OSMIUM_COMPRESSOR)
              .addCriterion(Criterion.has("glowstone", Tags.Items.DUSTS_GLOWSTONE))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE)
              .addCriterion(Criterion.has("refined_glowstone_nugget", MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST)
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(Criterion.HAS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addRefinedObsidianProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .addCriterion(Criterion.HAS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //from obsidian dust
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_OBSIDIAN),
              InfusionIngredient.from(MekanismTags.InfuseTypes.DIAMOND, 10),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "dust/from_obsidian_dust"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN)
              .addCriterion(Criterion.has("refined_obsidian_block", MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackGasToItemStackRecipeBuilder.compressing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
              MekanismItems.REFINED_OBSIDIAN_INGOT.getItemStack()
        ).addCriterion(Criterion.HAS_OSMIUM_COMPRESSOR)
              .addCriterion(Criterion.has("refined_obsidian_dust", MekanismTags.Items.DUSTS_REFINED_OBSIDIAN))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN)
              .addCriterion(Criterion.has("refined_obsidian_nugget", MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addSteelProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.STEEL_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .addCriterion(Criterion.has("steel_block", MekanismTags.Items.STORAGE_BLOCKS_STEEL))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(MekanismTags.Items.DUSTS_STEEL), MekanismItems.STEEL_INGOT, 0.5F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"),
              Criterion.has("steel_dust", MekanismTags.Items.DUSTS_STEEL));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STEEL_INGOT)
              .pattern(STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_STEEL)
              .addCriterion(Criterion.has("steel_nugget", MekanismTags.Items.NUGGETS_STEEL))
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Enriched iron -> dust
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismItems.ENRICHED_IRON),
              InfusionIngredient.from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.STEEL_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_METALLURGIC_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "enriched_iron_to_dust"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_STEEL),
              MekanismItems.STEEL_DUST.getItemStack()
        ).addCriterion(Criterion.HAS_CRUSHER)
              .build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
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
        ).energyRequired(FloatingLong.createConst(1_000))
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
        ).energyRequired(FloatingLong.createConst(200))
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
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.ETHENE, MekanismFluids.ETHENE, MekanismTags.Fluids.ETHENE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN, MekanismFluids.HYDROGEN, MekanismTags.Fluids.HYDROGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROGEN_CHLORIDE, MekanismFluids.HYDROGEN_CHLORIDE, MekanismTags.Fluids.HYDROGEN_CHLORIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.LITHIUM, MekanismFluids.LITHIUM, MekanismTags.Fluids.LITHIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.OXYGEN, MekanismFluids.OXYGEN, MekanismTags.Fluids.OXYGEN);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SODIUM, MekanismFluids.SODIUM, MekanismTags.Fluids.SODIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.STEAM, MekanismFluids.STEAM, MekanismTags.Fluids.STEAM);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_DIOXIDE, MekanismFluids.SULFUR_DIOXIDE, MekanismTags.Fluids.SULFUR_DIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFUR_TRIOXIDE, MekanismFluids.SULFUR_TRIOXIDE, MekanismTags.Fluids.SULFUR_TRIOXIDE);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.SULFURIC_ACID, MekanismFluids.SULFURIC_ACID, MekanismTags.Fluids.SULFURIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.HYDROFLUORIC_ACID, MekanismFluids.HYDROFLUORIC_ACID, MekanismTags.Fluids.HYDROFLUORIC_ACID);
        addRotaryCondensentratorRecipe(consumer, basePath, MekanismGases.WATER_VAPOR, new IFluidProvider() {
            @Nonnull
            @Override
            public Fluid getFluid() {
                return Fluids.WATER;
            }
        }, FluidTags.WATER);
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
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.ACACIA_PLANKS, Items.ACACIA_BOAT, Items.ACACIA_DOOR, Items.ACACIA_FENCE_GATE,
              ItemTags.ACACIA_LOGS, Items.ACACIA_PRESSURE_PLATE, Items.ACACIA_TRAPDOOR, "acacia");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.BIRCH_PLANKS, Items.BIRCH_BOAT, Items.BIRCH_DOOR, Items.BIRCH_FENCE_GATE,
              ItemTags.BIRCH_LOGS, Items.BIRCH_PRESSURE_PLATE, Items.BIRCH_TRAPDOOR, "birch");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.DARK_OAK_PLANKS, Items.DARK_OAK_BOAT, Items.DARK_OAK_DOOR, Items.DARK_OAK_FENCE_GATE,
              ItemTags.DARK_OAK_LOGS, Items.DARK_OAK_PRESSURE_PLATE, Items.DARK_OAK_TRAPDOOR, "dark_oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.JUNGLE_PLANKS, Items.JUNGLE_BOAT, Items.JUNGLE_DOOR, Items.JUNGLE_FENCE_GATE,
              ItemTags.JUNGLE_LOGS, Items.JUNGLE_PRESSURE_PLATE, Items.JUNGLE_TRAPDOOR, "jungle");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.OAK_PLANKS, Items.OAK_BOAT, Items.OAK_DOOR, Items.OAK_FENCE_GATE, ItemTags.OAK_LOGS,
              Items.OAK_PRESSURE_PLATE, Items.OAK_TRAPDOOR, "oak");
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, Items.SPRUCE_PLANKS, Items.SPRUCE_BOAT, Items.SPRUCE_DOOR, Items.SPRUCE_FENCE_GATE,
              ItemTags.SPRUCE_LOGS, Items.SPRUCE_PRESSURE_PLATE, Items.SPRUCE_TRAPDOOR, "spruce");
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
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, Items.BLACK_WOOL, "black");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, Items.BLUE_WOOL, "blue");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, Items.BROWN_WOOL, "brown");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, Items.CYAN_WOOL, "cyan");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, Items.GRAY_WOOL, "gray");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, Items.GREEN_WOOL, "green");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, Items.LIGHT_BLUE_WOOL, "light_blue");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, Items.LIGHT_GRAY_WOOL, "light_gray");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, Items.LIME_WOOL, "lime");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, Items.MAGENTA_WOOL, "magenta");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, Items.ORANGE_WOOL, "orange");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, Items.PINK_WOOL, "pink");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, Items.PURPLE_WOOL, "purple");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, Items.RED_WOOL, "red");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, Items.WHITE_WOOL, "white");
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, Items.YELLOW_WOOL, "yellow");
    }

    private void addElectrolyticSeparatorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "separator/";
        //Brine
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(MekanismTags.Fluids.BRINE, 10),
              MekanismGases.SODIUM.getGasStack(1),
              MekanismGases.CHLORINE.getGasStack(1)
        ).addCriterion(Criterion.HAS_ELECTROLYTIC_SEPARATOR)
              .build(consumer, Mekanism.rl(basePath + "brine"));
        //Water
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(FluidTags.WATER, 2),
              MekanismGases.HYDROGEN.getGasStack(2),
              MekanismGases.OXYGEN.getGasStack(1)
        ).addCriterion(Criterion.HAS_ELECTROLYTIC_SEPARATOR)
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
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESTRICTIVE_TRANSPORTER, 2)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, Items.IRON_BARS)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "restrictive_transporter"));
    }

    private void addLogisticalTransporterRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER, MekanismTags.Items.CIRCUITS_BASIC);
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
        addBasicTransmitterRecipe(consumer, basePath, MekanismBlocks.BASIC_UNIVERSAL_CABLE, Tags.Items.DUSTS_REDSTONE);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismBlocks.BASIC_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_INFUSED, Criterion.HAS_INFUSED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismBlocks.ADVANCED_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_REINFORCED, Criterion.HAS_REINFORCED_ALLOY);
        addTransmitterUpgradeRecipe(consumer, basePath, MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE, MekanismBlocks.ELITE_UNIVERSAL_CABLE, MekanismTags.Items.ALLOYS_ATOMIC, Criterion.HAS_ATOMIC_ALLOY);
    }

    private void addBasicTransmitterRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> transmitter,
          Tag<Item> itemTag) {
        String tierName = Attribute.getBaseTier(transmitter.getBlock()).getLowerName();
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, itemTag)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }

    private void addBasicTransmitterRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> transmitter, Item item) {
        String tierName = Attribute.getBaseTier(transmitter.getBlock()).getLowerName();
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(BASIC_TRANSMITTER_PATTERN)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CONSTANT, item)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer, Mekanism.rl(basePath + tierName));
    }

    private void addTransmitterUpgradeRecipe(Consumer<IFinishedRecipe> consumer, String basePath, BlockRegistryObject<? extends ITypeBlock, ?> transmitter,
          IItemProvider previousTransmitter, Tag<Item> alloyTag, RecipeCriterion alloyCriterion) {
        String tierName = Attribute.getBaseTier(transmitter.getBlock()).getLowerName();
        ExtendedShapedRecipeBuilder.shapedRecipe(transmitter, 8)
              .pattern(TRANSMITTER_UPGRADE_PATTERN)
              .key(Pattern.PREVIOUS, previousTransmitter)
              .key(Pattern.ALLOY, alloyTag)
              .addCriterion(Criterion.has(previousTransmitter))
              .addCriterion(alloyCriterion)
              .build(consumer, Mekanism.rl(basePath + tierName));
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
        //Atomic disassembler
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ATOMIC_DISASSEMBLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismTags.Items.ALLOYS_ATOMIC)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Boiler casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer);
        //Boiler valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.BOILER_CASING)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .addCriterion(Criterion.has(MekanismBlocks.BOILER_CASING))
              .build(consumer);
        //Cardboard box
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CARDBOARD_BOX)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.SAWDUST)
              .addCriterion(Criterion.has("sawdust", MekanismTags.Items.SAWDUST))
              .build(consumer);
        //Charcoal
        ExtendedShapelessRecipeBuilder.shapelessRecipe(Items.CHARCOAL, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL)
              .addCriterion(Criterion.has("charcoal_block", MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL))
              .build(consumer, Mekanism.rl("charcoal"));
        //Chargepad
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARGEPAD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Items.STONE_PRESSURE_PLATE)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Chemical crystallizer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_CRYSTALLIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Chemical dissolution chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismTags.Items.ALLOYS_ATOMIC)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Chemical infuser
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.TANK, Pattern.STEEL_CASING, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Chemical injection chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_GOLD)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.CONSTANT, MekanismBlocks.PURIFICATION_CHAMBER)
              .addCriterion(Criterion.HAS_PURIFICATION_CHAMBER)
              .build(consumer);
        //Chemical oxidizer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_OXIDIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(PERSONAL_CHEST_CHAR, Pattern.CONSTANT, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(PERSONAL_CHEST_CHAR, MekanismTags.Items.CHESTS_PERSONAL)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Chemical washer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_WASHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.BUCKET, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.BUCKET, Items.BUCKET)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Combiner
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.COMBINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.STEEL_CASING, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Configuration card
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CONFIGURATION_CARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Configurator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CONFIGURATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.INGOT, Tags.Items.GEMS_LAPIS)
              .key(Pattern.CONSTANT, Tags.Items.RODS_WOODEN)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Crafting formula
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.CRAFTING_FORMULA)
              .addIngredient(Items.PAPER)
              .addIngredient(MekanismTags.Items.CIRCUITS_BASIC)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Crusher
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CRUSHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.BUCKET, Items.LAVA_BUCKET)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Dictionary
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.DICTIONARY)
              .pattern(RecipePattern.createPattern(
                    Pattern.CIRCUIT,
                    Pattern.CONSTANT)
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.BOOK)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Digital miner
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DIGITAL_MINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(SORTER_CHAR, ROBIT_CHAR, SORTER_CHAR),
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(SORTER_CHAR, MekanismBlocks.LOGISTICAL_SORTER)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(ROBIT_CHAR, MekanismItems.ROBIT)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Dynamic tank
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_TANK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.BUCKET, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.BUCKET, Items.BUCKET)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer);
        //Dynamic valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .addCriterion(Criterion.has(MekanismBlocks.DYNAMIC_TANK))
              .build(consumer);
        //Electric bow
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ELECTRIC_BOW)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.ENERGY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, Tags.Items.STRING)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Electric pump
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ELECTRIC_PUMP)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.BUCKET, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).key(Pattern.BUCKET, Items.BUCKET)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Electrolytic core
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ELECTROLYTIC_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.DUSTS_OSMIUM)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismTags.Items.DUSTS_GOLD)
              .key(Pattern.INGOT, MekanismTags.Items.DUSTS_IRON)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Electrolytic separator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ELECTROLYTIC_SEPARATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .addCriterion(Criterion.has(MekanismItems.ELECTROLYTIC_CORE))
              .build(consumer);
        //Energized smelter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ENERGIZED_SMELTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(GLASS_CHAR, Pattern.STEEL_CASING, GLASS_CHAR),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Energy tablet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ENERGY_TABLET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE))
              ).key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.INGOT, Tags.Items.INGOTS_GOLD)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Enrichment chamber
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ENRICHMENT_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Flamethrower
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.FLAMETHROWER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.CONSTANT, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_TIN)
              .key(Pattern.STEEL, Items.FLINT_AND_STEEL)
              .addCriterion(Criterion.HAS_ADVANCED_CIRCUIT)
              .build(consumer);
        //Fluidic plenisher
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FLUIDIC_PLENISHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ELECTRIC_PUMP)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_TIN)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Formulaic assemblicator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FORMULAIC_ASSEMBLICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.PREVIOUS, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.PREVIOUS, Items.CRAFTING_TABLE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Free runners
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.FREE_RUNNERS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.EMPTY, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.EMPTY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.EMPTY, Pattern.ENERGY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Fuelwood heater
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FUELWOOD_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, Items.FURNACE)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Gas mask
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GAS_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, Pattern.EMPTY, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .addCriterion(Criterion.HAS_STEEL)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Gauge dropper
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GAUGE_DROPPER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.EMPTY, GLASS_CHAR),
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR))
              ).key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .addCriterion(Criterion.HAS_OSMIUM)
              .build(consumer);
        RecipeCriterion hasPellet = Criterion.has(MekanismItems.HDPE_PELLET);
        //HDPE rod
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_ROD)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .addCriterion(hasPellet)
              .build(consumer);
        //HDPE sheet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_SHEET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .addCriterion(hasPellet)
              .build(consumer);
        //HDPE stick
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_STICK)
              .pattern(RecipePattern.createPattern(
                    Pattern.CONSTANT,
                    Pattern.CONSTANT)
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .addCriterion(Criterion.has(MekanismItems.HDPE_ROD))
              .build(consumer);
        //Jetpack
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_TIN)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Jetpack armored
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ARMORED_JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.PREVIOUS, Pattern.EMPTY))
              ).key(Pattern.PREVIOUS, MekanismItems.JETPACK)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.STEEL, MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .key(Pattern.CONSTANT, MekanismTags.Items.DUSTS_DIAMOND)
              .addCriterion(Criterion.has(MekanismItems.JETPACK))
              .build(consumer);
        //Laser
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_DIAMOND)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Laser amplifier
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER_AMPLIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_DIAMOND)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_ENERGY_CUBE)
              .addCriterion(Criterion.has(MekanismBlocks.BASIC_ENERGY_CUBE))
              .build(consumer);
        //Laser tractor beam
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER_TRACTOR_BEAM)
              .pattern(RecipePattern.createPattern(
                    PERSONAL_CHEST_CHAR,
                    Pattern.CONSTANT)
              ).key(PERSONAL_CHEST_CHAR, MekanismTags.Items.CHESTS_PERSONAL)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .addCriterion(Criterion.has(MekanismBlocks.LASER_AMPLIFIER))
              .build(consumer);
        //Logistical sorter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LOGISTICAL_SORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.PISTON)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Metallurgic infuser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.METALLURGIC_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.OSMIUM, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.FURNACE)
              .addCriterion(Criterion.HAS_OSMIUM)
              .build(consumer);
        //Network reader
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.NETWORK_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Oredictionificator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.OREDICTIONIFICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.PREVIOUS, MekanismItems.DICTIONARY)
              .key(Pattern.CONSTANT, Tags.Items.CHESTS_WOODEN)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Osmium compressor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.OSMIUM_COMPRESSOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.BUCKET, Items.BUCKET)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Paper
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.PAPER, 6)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.SAWDUST)
              .addCriterion(Criterion.has("sawdust", MekanismTags.Items.SAWDUST))
              .build(consumer, Mekanism.rl("paper"));
        //Personal chest
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PERSONAL_CHEST)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.PREVIOUS, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(GLASS_CHAR, Tags.Items.GLASS)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .build(consumer);
        //Portable teleporter
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.PORTABLE_TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CIRCUIT, TELEPORTATION_CORE_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Precision sawmill
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRECISION_SAWMILL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Pressure disperser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURE_DISPERSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Items.IRON_BARS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Pressurized reaction chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.TANK, Pattern.CONSTANT, Pattern.TANK))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer);
        //Purification chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PURIFICATION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .addCriterion(Criterion.HAS_ENRICHMENT_CHAMBER)
              .build(consumer);
        //Quantum entangloporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QUANTUM_ENTANGLOPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .addCriterion(Criterion.HAS_ULTIMATE_CIRCUIT)
              .addCriterion(Criterion.HAS_ATOMIC_ALLOY)
              .build(consumer);
        //Rail
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.RAIL, 24)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.CONSTANT, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM))
              ).key(Pattern.CONSTANT, Tags.Items.RODS_WOODEN)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .addCriterion(Criterion.HAS_OSMIUM)
              .build(consumer, Mekanism.rl("rails"));
        //Resistive heater
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESISTIVE_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.STEEL_CASING, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT))
              ).key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_TIN)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Robit
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ROBIT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.ENERGY, Pattern.ALLOY, Pattern.ENERGY),
                    TripleLine.of(Pattern.INGOT, PERSONAL_CHEST_CHAR, Pattern.INGOT))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(PERSONAL_CHEST_CHAR, MekanismTags.Items.CHESTS_PERSONAL)
              .addCriterion(Criterion.HAS_ATOMIC_ALLOY)
              .build(consumer);
        //Rotary condensentrator
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ROTARY_CONDENSENTRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.TANK, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_FLUID_TANK)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Scuba tank
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SCUBA_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CIRCUIT, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.TANK, Pattern.ALLOY),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_GAS_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .addCriterion(Criterion.HAS_BASIC_CIRCUIT)
              .addCriterion(Criterion.HAS_INFUSED_ALLOY)
              .build(consumer);
        //Security desk
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SECURITY_DESK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, TELEPORTATION_CORE_CHAR, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Seismic reader
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SEISMIC_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_LAPIS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .addCriterion(Criterion.HAS_ENERGY_TABLET)
              .build(consumer);
        //Seismic vibrator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SEISMIC_VIBRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_LAPIS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_TIN)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Solar neutron activator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Steel casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STEEL_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(GLASS_CHAR, Pattern.OSMIUM, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.OSMIUM, MekanismTags.Items.INGOTS_OSMIUM)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .addCriterion(Criterion.HAS_OSMIUM)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer);
        //Structural glass
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STRUCTURAL_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .addCriterion(Criterion.HAS_STEEL)
              .build(consumer);
        //Superheating element
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SUPERHEATING_ELEMENT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).key(Pattern.INGOT, MekanismTags.Items.INGOTS_COPPER)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Teleportation core
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.TELEPORTATION_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, DIAMOND_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_LAPIS)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.INGOT, Tags.Items.INGOTS_GOLD)
              .key(DIAMOND_CHAR, Tags.Items.GEMS_DIAMOND)
              .addCriterion(Criterion.HAS_ATOMIC_ALLOY)
              .build(consumer);
        //Teleporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT))
              ).key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .addCriterion(Criterion.HAS_STEEL_CASING)
              .build(consumer);
        //Teleporter frame
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.TELEPORTER_FRAME, 9)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.GLOWSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .addCriterion(Criterion.HAS_REFINED_GLOWSTONE)
              .addCriterion(Criterion.HAS_REFINED_OBSIDIAN)
              .build(consumer);
    }

    private void addUraniumRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "processing/uranium/";

        //dirty uranium slurry
        ItemStackGasToGasRecipeBuilder.dissolution(
            ItemStackIngredient.from(MekanismTags.Items.ORES_URANIUM),
            GasStackIngredient.from(MekanismGases.SULFURIC_ACID, 1),
            MekanismGases.URANIUM_SLURRY.getDirtySlurry().getGasStack(1_000)
        ).addCriterion(Criterion.HAS_CHEMICAL_DISSOLUTION_CHAMBER)
            .build(consumer, Mekanism.rl(basePath + "slurry/dirty"));
        //clean uranium slurry
        RecipeCriterion hasOre = Criterion.has(MekanismBlocks.URANIUM_ORE.asItem().getRegistryName().getPath(), MekanismTags.Items.ORES_URANIUM);
        FluidGasToGasRecipeBuilder.washing(
              FluidStackIngredient.from(FluidTags.WATER, 5),
              GasStackIngredient.from(MekanismGases.URANIUM_SLURRY.getDirtySlurry(), 1),
              MekanismGases.URANIUM_SLURRY.getCleanSlurry().getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_WASHER)
              .addCriterion(hasOre)
              .build(consumer, Mekanism.rl(basePath + "slurry/clean"));
        //yellow cake
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.URANIUM_SLURRY.getCleanSlurry(), 250),
              MekanismItems.YELLOW_CAKE_URANIUM.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_CRYSTALLIZER)
              .build(consumer, Mekanism.rl(basePath + "yellow_cake_uranium/from_slurry"));
        //hydrofluoric acid
        ItemStackGasToGasRecipeBuilder.dissolution(
              ItemStackIngredient.from(MekanismTags.Items.GEMS_FLUORITE),
              GasStackIngredient.from(MekanismGases.SULFURIC_ACID, 1),
              MekanismGases.HYDROFLUORIC_ACID.getGasStack(100)
        ).addCriterion(Criterion.HAS_CHEMICAL_DISSOLUTION_CHAMBER)
              .build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid"));
        //uranium oxide
        ItemStackToGasRecipeBuilder.oxidizing(
            ItemStackIngredient.from(MekanismTags.Items.YELLOW_CAKE_URANIUM),
            MekanismGases.URANIUM_OXIDE.getGasStack(100)
        ).addCriterion(Criterion.HAS_CHEMICAL_OXIDIZER)
              .build(consumer, Mekanism.rl(basePath + "uranium_oxide"));
        //uranium hexafluoride
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.HYDROFLUORIC_ACID, 1),
              GasStackIngredient.from(MekanismGases.URANIUM_OXIDE, 1),
              MekanismGases.URANIUM_HEXAFLUORIDE.getGasStack(1)
        ).addCriterion(Criterion.HAS_CHEMICAL_INFUSER)
              .build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
        //fissile fuel
        GasToGasRecipeBuilder.centrifuging(
              GasStackIngredient.from(MekanismGases.URANIUM_HEXAFLUORIDE, 1),
              MekanismGases.FISSILE_FUEL.getGasStack(1)
        ).addCriterion(Criterion.HAS_ISOTOPIC_CENTRIFUGE)
              .build(consumer, Mekanism.rl(basePath + "fissile_fuel"));
        //fissile fuel pellet
        GasToItemStackRecipeBuilder.crystallizing(
              GasStackIngredient.from(MekanismGases.FISSILE_FUEL, 100),
              MekanismItems.FISSILE_FUEL_PELLET.getItemStack()
        ).addCriterion(Criterion.HAS_CHEMICAL_CRYSTALLIZER)
              .build(consumer, Mekanism.rl(basePath + "fissile_fuel_pellet/from_uranium_hexafluoride"));
    }
}