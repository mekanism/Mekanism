package mekanism.common.recipe.impl;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.compat.AE2RecipeProvider;
import mekanism.common.recipe.compat.BYGRecipeProvider;
import mekanism.common.recipe.compat.BiomesOPlentyRecipeProvider;
import mekanism.common.recipe.compat.ILikeWoodBOPRecipeProvider;
import mekanism.common.recipe.compat.ILikeWoodBYGRecipeProvider;
import mekanism.common.recipe.compat.ILikeWoodRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;

@ParametersAreNonnullByDefault
public class MekanismRecipeProvider extends BaseRecipeProvider {

    static final char DIAMOND_CHAR = 'D';
    static final char GLASS_CHAR = 'G';
    static final char PERSONAL_STORAGE_CHAR = 'P';
    static final char ROBIT_CHAR = 'R';
    static final char SORTER_CHAR = 'S';
    static final char TELEPORTATION_CORE_CHAR = 'T';

    //TODO: Do we want to use same pattern for fluid tank and chemical tank at some point
    static final RecipePattern TIER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));
    static final RecipePattern STORAGE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    public static final RecipePattern BASIC_MODULE = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
          TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR));

    public MekanismRecipeProvider(DataGenerator gen, ExistingFileHelper existingFileHelper) {
        super(gen, existingFileHelper, Mekanism.MODID);
    }

    @Override
    protected void addRecipes(Consumer<FinishedRecipe> consumer) {
        addMiscRecipes(consumer);
        addGearModuleRecipes(consumer);
        addLateGameRecipes(consumer);
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return List.of(
              new BinRecipeProvider(),
              new ChemicalInfuserRecipeProvider(),
              new ChemicalInjectorRecipeProvider(),
              new ChemicalTankRecipeProvider(),
              new CombinerRecipeProvider(),
              new ControlCircuitRecipeProvider(),
              new CrusherRecipeProvider(),
              new ChemicalCrystallizerRecipeProvider(),
              new EnergyConversionRecipeProvider(),
              new EnergyCubeRecipeProvider(),
              new EnrichingRecipeProvider(),
              new EvaporatingRecipeProvider(),
              new FactoryRecipeProvider(),
              new FluidTankRecipeProvider(),
              new GasConversionRecipeProvider(),
              new InductionRecipeProvider(),
              new InfusionConversionRecipeProvider(),
              new MetallurgicInfuserRecipeProvider(),
              new NucleosynthesizingRecipeProvider(),
              new OreProcessingRecipeProvider(),
              new OxidizingRecipeProvider(),
              new PaintingRecipeProvider(),
              new PigmentExtractingRecipeProvider(),
              new PigmentMixingRecipeProvider(),
              new PressurizedReactionRecipeProvider(),
              new RotaryRecipeProvider(),
              new SawingRecipeProvider(),
              new SeparatingRecipeProvider(),
              new StorageRecipeProvider(),
              new ThermalEvaporationRecipeProvider(),
              new TierInstallerRecipeProvider(),
              new TransmitterRecipeProvider(),
              new UpgradeRecipeProvider(),
              //Mod Compat Recipe providers
              new AE2RecipeProvider(),
              new BiomesOPlentyRecipeProvider(),
              new BYGRecipeProvider(),
              new ILikeWoodRecipeProvider(),
              new ILikeWoodBOPRecipeProvider(),
              new ILikeWoodBYGRecipeProvider()
        );
    }

    private void addMiscRecipes(Consumer<FinishedRecipe> consumer) {
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
              .build(consumer);
        //Boiler casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .build(consumer);
        //Boiler valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.BOILER_CASING)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .build(consumer);
        //Canteen
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CANTEEN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.CONSTANT, Items.BOWL)
              .build(consumer);
        //Cardboard box
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CARDBOARD_BOX)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.SAWDUST)
              .build(consumer);
        //Charcoal
        ExtendedShapelessRecipeBuilder.shapelessRecipe(Items.CHARCOAL, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_CHARCOAL)
              .build(consumer, Mekanism.rl("charcoal"));
        //Chargepad
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARGEPAD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .build(consumer);
        //Chemical crystallizer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_CRYSTALLIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT))
              ).key(Pattern.ALLOY, MekanismTags.Items.GEMS_FLUORITE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Chemical dissolution chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Chemical infuser
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.TANK, Pattern.STEEL_CASING, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
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
              .build(consumer);
        //Chemical oxidizer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_OXIDIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(PERSONAL_STORAGE_CHAR, Pattern.CONSTANT, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(PERSONAL_STORAGE_CHAR, MekanismTags.Items.PERSONAL_STORAGE)
              .build(consumer);
        //Chemical washer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_WASHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.BUCKET, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.BUCKET, MekanismBlocks.BASIC_FLUID_TANK)
              .build(consumer);
        //Combiner
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.COMBINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.STEEL_CASING, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.COBBLESTONE, Tags.Items.COBBLESTONE_NORMAL)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Configuration card
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CONFIGURATION_CARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
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
              .build(consumer);
        //Crafting formula
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.CRAFTING_FORMULA)
              .addIngredient(Items.PAPER)
              .addIngredient(MekanismTags.Items.CIRCUITS_BASIC)
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
              .build(consumer);
        //Dictionary
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.DICTIONARY)
              .pattern(RecipePattern.createPattern(
                    Pattern.CIRCUIT,
                    Pattern.CONSTANT)
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.BOOK)
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
              .build(consumer);
        //Dosimeter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.DOSIMETER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .build(consumer);
        //Dye Base
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.DYE_BASE, 3)
              .addIngredient(MekanismTags.Items.SAWDUST, 2)
              .addIngredient(Items.CLAY_BALL)
              .build(consumer);
        //Dynamic tank
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_TANK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.BUCKET, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.BUCKET, Items.BUCKET)
              .build(consumer);
        //Dynamic valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
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
              .build(consumer);
        //Electric pump
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ELECTRIC_PUMP)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.BUCKET, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).key(Pattern.BUCKET, Items.BUCKET)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Electrolytic core
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ELECTROLYTIC_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD))
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON))
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
              .build(consumer);
        //Flamethrower
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.FLAMETHROWER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.CONSTANT, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.STEEL, Items.FLINT_AND_STEEL)
              .build(consumer);
        //Fluidic plenisher
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FLUIDIC_PLENISHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ELECTRIC_PUMP)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .build(consumer);
        //Formulaic assemblicator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FORMULAIC_ASSEMBLICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.PREVIOUS, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.PREVIOUS, Blocks.CRAFTING_TABLE)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
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
              .key(Pattern.CONSTANT, Blocks.FURNACE)
              .build(consumer);
        //Scuba mask
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.SCUBA_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, Pattern.EMPTY, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer);
        //Gauge dropper
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GAUGE_DROPPER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.EMPTY, GLASS_CHAR),
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR))
              ).key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .build(consumer);
        //Geiger Counter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GEIGER_COUNTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .build(consumer);
        //Hazmat Mask
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.DYE, Tags.Items.DYES_ORANGE)
              .build(consumer);
        //Hazmat Gown
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_GOWN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.DYE, Tags.Items.DYES_ORANGE)
              .build(consumer);
        //Hazmat Pants
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.DYE, Tags.Items.DYES_ORANGE)
              .build(consumer);
        //Hazmat Boots
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.DYE, Tags.Items.DYES_BLACK)
              .build(consumer);
        //HDPE rod
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_ROD)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .build(consumer);
        //HDPE sheet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_SHEET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .build(consumer);
        //HDPE stick
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_STICK)
              .pattern(RecipePattern.createPattern(
                    Pattern.CONSTANT,
                    Pattern.CONSTANT)
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .build(consumer);
        //Industrial Alarm
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUSTRIAL_ALARM)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Blocks.REDSTONE_LAMP)
              .build(consumer);
        //Isotopic Centrifuge
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ISOTOPIC_CENTRIFUGE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .build(consumer);
        //Jetpack
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
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
              .build(consumer);
        //HDPE Elytra
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_REINFORCED_ELYTRA)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.HDPE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.PREVIOUS, Pattern.HDPE_CHAR),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.EMPTY, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .key(Pattern.PREVIOUS, Items.ELYTRA)
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
              .build(consumer);
        //Laser tractor beam
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER_TRACTOR_BEAM)
              .pattern(RecipePattern.createPattern(
                    PERSONAL_STORAGE_CHAR,
                    Pattern.CONSTANT)
              ).key(PERSONAL_STORAGE_CHAR, MekanismTags.Items.PERSONAL_STORAGE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .build(consumer);
        //Logistical sorter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LOGISTICAL_SORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Blocks.PISTON)
              .build(consumer);
        //Metallurgic infuser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.METALLURGIC_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.OSMIUM, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Blocks.FURNACE)
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
              .build(consumer);
        //Paper
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.PAPER, 6)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.SAWDUST)
              .build(consumer, Mekanism.rl("paper"));
        //Personal barrel
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PERSONAL_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.PREVIOUS, Tags.Items.BARRELS_WOODEN)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(GLASS_CHAR, Tags.Items.GLASS)
              .build(consumer);
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
              .build(consumer);
        //Pressure disperser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURE_DISPERSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Blocks.IRON_BARS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer);
        //Pressurized reaction chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.TANK, Pattern.CONSTANT, Pattern.TANK))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .build(consumer);
        //Purification chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PURIFICATION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
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
              .build(consumer);
        //Rail
        ExtendedShapedRecipeBuilder.shapedRecipe(Blocks.RAIL, 24)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.CONSTANT, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM))
              ).key(Pattern.CONSTANT, Tags.Items.RODS_WOODEN)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .build(consumer, Mekanism.rl("rails"));
        //Resistive heater
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESISTIVE_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.STEEL_CASING, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT))
              ).key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .build(consumer);
        //Robit
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ROBIT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.ENERGY, Pattern.ALLOY, Pattern.ENERGY),
                    TripleLine.of(Pattern.INGOT, PERSONAL_STORAGE_CHAR, Pattern.INGOT))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(PERSONAL_STORAGE_CHAR, MekanismTags.Items.PERSONAL_STORAGE)
              .build(consumer);
        //Rotary condensentrator
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ROTARY_CONDENSENTRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.TANK, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_FLUID_TANK)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .build(consumer);
        //Scuba tank
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SCUBA_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CIRCUIT, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.TANK, Pattern.ALLOY),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
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
              .build(consumer);
        //Seismic vibrator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SEISMIC_VIBRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.CONSTANT, Tags.Items.GEMS_LAPIS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
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
              .build(consumer);
        //Steel casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STEEL_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(GLASS_CHAR, Pattern.OSMIUM, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer);
        //Structural glass
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STRUCTURAL_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer);
        //Superheating element
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SUPERHEATING_ELEMENT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).key(Pattern.INGOT, Tags.Items.INGOTS_COPPER)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
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
              .build(consumer);
        //Teleporter frame
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.TELEPORTER_FRAME, 9)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.GLOWSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.GLOWSTONE, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE)
              .key(Pattern.INGOT, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .build(consumer);
        //Base QIO Drive
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BASE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CIRCUIT, MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
              .key(Pattern.CONSTANT, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Hyper-Dense QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CONSTANT, Tags.Items.GEMS_DIAMOND)
              .key(Pattern.PREVIOUS, MekanismItems.BASE_QIO_DRIVE)
              .build(consumer);
        //Time-Dilating QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.TIME_DILATING_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CONSTANT, Tags.Items.GEMS_EMERALD)
              .key(Pattern.PREVIOUS, MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .build(consumer);
        //Supermassive QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SUPERMASSIVE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.CONSTANT, MekanismTags.Items.PELLETS_ANTIMATTER)
              .key(Pattern.PREVIOUS, MekanismItems.TIME_DILATING_QIO_DRIVE)
              .build(consumer);
        //QIO Drive Array
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_DRIVE_ARRAY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.GLASS, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).key(Pattern.CONSTANT, MekanismTags.Items.PERSONAL_STORAGE)
              .key(Pattern.INGOT, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.GLASS, Tags.Items.GLASS_PANES)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .build(consumer);
        //QIO Redstone Adapter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_REDSTONE_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.WOOD, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.REDSTONE, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.WOOD, Blocks.REDSTONE_TORCH)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .build(consumer);
        //QIO Exporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_EXPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CONSTANT, Blocks.PISTON)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .build(consumer);
        //QIO Importer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_IMPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CONSTANT, Blocks.STICKY_PISTON)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .build(consumer);
        //QIO Dashboard
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.GLASS, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).key(Pattern.GLASS, Tags.Items.GLASS_PANES)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .build(consumer);
        //Portable QIO Dashboard
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.PORTABLE_QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY))
              ).key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.QIO_DASHBOARD)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .build(consumer);
        //Meka-Tool
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKA_TOOL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, 'o', Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key('o', MekanismItems.CONFIGURATOR)
              .key(Pattern.CONSTANT, MekanismItems.ATOMIC_DISASSEMBLER)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //MekaSuit Helmet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_HELMET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_HELMET)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //MekaSuit Bodyarmor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_BODYARMOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_CHESTPLATE)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //MekaSuit Pants
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_LEGGINGS)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //MekaSuit Boots
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_BOOTS)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //SPS Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SPS_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).key(Pattern.CONSTANT, MekanismTags.Items.PELLETS_PLUTONIUM)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //SPS Port
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SPS_PORT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, MekanismBlocks.SPS_CASING)
              .build(consumer);
        //Supercharged Coil
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SUPERCHARGED_COIL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of('c', 'c', 'c'),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER)
              .key('c', Tags.Items.INGOTS_COPPER)
              .key(Pattern.ALLOY, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Nutritional Liquifier
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.NUTRITIONAL_LIQUIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.BOWL)
              .build(consumer);
        //Pigment Extractor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PIGMENT_EXTRACTOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.FLINT)
              .build(consumer);
        //Pigment Mixer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PIGMENT_MIXER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .build(consumer);
        //Painting Machine
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PAINTING_MACHINE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.DYE_BASE)
              .build(consumer);
        //Modification Station
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.MODIFICATION_STATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.WOOD, Pattern.PLASTIC),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.WOOD, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Antiprotonic Nucleosynthesizer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, MekanismTags.Items.PELLETS_ANTIMATTER)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.ATOMIC_ALLOY)
              .build(consumer);
        //Radioactive Waste Barrel
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RADIOACTIVE_WASTE_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .build(consumer);
    }

    private void addGearModuleRecipes(Consumer<FinishedRecipe> consumer) {
        //Module Base
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_BASE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.NUGGET, MekanismTags.Items.NUGGETS_BRONZE)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Jetpack Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_JETPACK)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.JETPACK)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Electrolytic Breathing Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ELECTROLYTIC_BREATHING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Dosimeter Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_DOSIMETER)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.DOSIMETER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Geiger Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GEIGER)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.GEIGER_COUNTER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Energy Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ENERGY)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Laser Dissipation Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_LASER_DISSIPATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Radiation Shielding Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_RADIATION_SHIELDING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.LEAD))
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Charge Distribution Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_CHARGE_DISTRIBUTION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Teleportation Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_TELEPORTATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_ANTIMATTER)
              .build(consumer);
        //Nutritional Injection Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_NUTRITIONAL_INJECTION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.CANTEEN)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Silk Touch Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_SILK_TOUCH)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.WOOD, Pattern.PREVIOUS, Pattern.WOOD),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.REFINED_GLOWSTONE_BLOCK)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.WOOD, Items.DIAMOND_PICKAXE)
              .build(consumer);
        //Excavation Escalation Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_EXCAVATION_ESCALATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_PICKAXE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Attack Amplification Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ATTACK_AMPLIFICATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_SWORD)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Farming Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_FARMING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_HOE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Shearing Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_SHEARING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.SHEARS)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .build(consumer);
        //Vein Mining Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_VEIN_MINING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of('x', Pattern.PREVIOUS, 's'),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.DIAMOND_PICKAXE)
              .key('x', Items.DIAMOND_AXE)
              .key('s', Items.DIAMOND_SHOVEL)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer);
        //Vision Enhancement Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_VISION_ENHANCEMENT)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.EMERALD)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Inhalation Purification Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_INHALATION_PURIFICATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, 'o', Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.HAZMAT_MASK)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .key('o', MekanismItems.SCUBA_MASK)
              .build(consumer);
        //Magnetic Attraction Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_MAGNETIC_ATTRACTION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Blocks.IRON_BARS)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Frost Walker Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_FROST_WALKER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismFluids.HYDROGEN.getBucket())
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Locomotive Boosting Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_LOCOMOTIVE_BOOSTING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.DIAMOND_LEGGINGS)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Hydraulic Propulsion Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_HYDRAULIC_PROPULSION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.FREE_RUNNERS)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Gyroscopic Stabilization Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GYROSCOPIC_STABILIZATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.OBSIDIAN)
              .key(Pattern.HDPE_CHAR, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
        //Gravitational Modulating Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GRAVITATIONAL_MODULATING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Tags.Items.NETHER_STARS)
              .key(Pattern.ENERGY, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER)
              .key(Pattern.HDPE_CHAR, MekanismItems.ANTIMATTER_PELLET)
              .build(consumer);
        //Elytra Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ELYTRA)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.NUGGET, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_REINFORCED_ELYTRA)
              .key(Pattern.HDPE_CHAR, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.NUGGET, MekanismItems.ANTIMATTER_PELLET)
              .build(consumer);
    }

    private void addLateGameRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "processing/lategame/";

        //plutonium
        GasToGasRecipeBuilder.centrifuging(
              IngredientCreatorAccess.gas().from(MekanismGases.NUCLEAR_WASTE, 10),
              MekanismGases.PLUTONIUM.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "plutonium"));
        //polonium
        GasToGasRecipeBuilder.activating(
              IngredientCreatorAccess.gas().from(MekanismGases.NUCLEAR_WASTE, 10),
              MekanismGases.POLONIUM.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "polonium"));

        //plutonium pellet
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_FLUORITE),
              IngredientCreatorAccess.fluid().from(FluidTags.WATER, 1_000),
              IngredientCreatorAccess.gas().from(MekanismGases.PLUTONIUM, 1_000),
              100,
              MekanismItems.PLUTONIUM_PELLET.getItemStack(),
              MekanismGases.SPENT_NUCLEAR_WASTE.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "plutonium_pellet/from_reaction"));
        //polonium pellet
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_FLUORITE),
              IngredientCreatorAccess.fluid().from(FluidTags.WATER, 1_000),
              IngredientCreatorAccess.gas().from(MekanismGases.POLONIUM, 1_000),
              100,
              MekanismItems.POLONIUM_PELLET.getItemStack(),
              MekanismGases.SPENT_NUCLEAR_WASTE.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "polonium_pellet/from_reaction"));

        //antimatter pellet
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.gas().from(MekanismGases.ANTIMATTER, 1_000),
              MekanismItems.ANTIMATTER_PELLET.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "antimatter_pellet/from_gas"));

        //back to antimatter
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PELLETS_ANTIMATTER),
              MekanismGases.ANTIMATTER.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "antimatter/from_pellet"));
    }
}