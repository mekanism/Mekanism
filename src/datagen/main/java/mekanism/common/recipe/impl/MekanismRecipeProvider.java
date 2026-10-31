package mekanism.common.recipe.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ClearConfigurationRecipe;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.compat.ICompatRecipeDatagen;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.TameAnimalTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;

public class MekanismRecipeProvider extends BaseRecipeProvider {

    static final char GLASS_CHAR = 'G';
    static final char PERSONAL_STORAGE_CHAR = 'P';
    static final char MIXING_CHAR = 'M';
    static final char ROBIT_CHAR = 'R';
    static final char SORTER_CHAR = 'S';
    static final char TELEPORTATION_CORE_CHAR = 'T';

    static final RecipePattern TIER_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
          TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
          TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY));
    static final RecipePattern STORAGE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    static final RecipePattern TYPED_STORAGE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    public static final RecipePattern BASIC_MODULE = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
          TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR));

    private final List<ISubRecipeProvider> compatProviders = new ArrayList<>();

    public MekanismRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, Set<String> disabledCompats) {
        super(recipeOutput, advancementOutput, Mekanism.MODID);

        //Mod Compat Recipe providers
        checkCompat(disabledCompats, "ae2");
        checkCompat(disabledCompats, "biomesoplenty");
        checkCompat(disabledCompats, "biomeswevegone");
        checkCompat(disabledCompats, "farmersdelight");
    }

    private void checkCompat(Set<String> disabledCompats, String modid) {
        if (ModList.get().isLoaded(modid)) {
            ICompatRecipeDatagen compatRecipeDatagen = ICompatRecipeDatagen.INSTANCES.get(modid);
            if (compatRecipeDatagen == null) {
                throw new IllegalStateException("Mod: " + modid + " does not have a compat recipe service present");
            }
            compatProviders.add(compatRecipeDatagen.recipeProvider(output));
        } else {
            disabledCompats.add(modid);
        }
    }

    @Override
    protected void addRecipes() {
        addMiscRecipes();
        addGearModuleRecipes();
        addLateGameRecipes();
        for (ISubRecipeProvider compatProvider : compatProviders) {
            compatProvider.addRecipes(output);
        }
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return List.of(
              new BinRecipeProvider(this.items, this.fluids, this.chemicals),
              new ChemicalInfuserRecipeProvider(this.items, this.fluids, this.chemicals),
              new ChemicalInjectorRecipeProvider(this.items, this.fluids, this.chemicals, this.output.lookup(Registries.BIOME)),
              new ChemicalTankRecipeProvider(this.items, this.fluids, this.chemicals),
              new CombinerRecipeProvider(this.items, this.fluids, this.chemicals),
              new ControlCircuitRecipeProvider(this.items, this.fluids, this.chemicals),
              new CrusherRecipeProvider(this.items, this.fluids, this.chemicals),
              new ChemicalCrystallizerRecipeProvider(this.items, this.fluids, this.chemicals),
              new EnergyConversionRecipeProvider(this.items, this.fluids, this.chemicals),
              new EnergyCubeRecipeProvider(this.items, this.fluids, this.chemicals),
              new EnrichingRecipeProvider(this.items, this.fluids, this.chemicals),
              new EvaporatingRecipeProvider(this.items, this.fluids, this.chemicals),
              new FactoryRecipeProvider(this.items, this.fluids, this.chemicals),
              new FluidTankRecipeProvider(this.items, this.fluids, this.chemicals),
              new GasConversionRecipeProvider(this.items, this.fluids, this.chemicals),
              new InductionRecipeProvider(this.items, this.fluids, this.chemicals),
              new InfusionConversionRecipeProvider(this.items, this.fluids, this.chemicals),
              new MetallurgicInfuserRecipeProvider(this.items, this.fluids, this.chemicals),
              new NucleosynthesizingRecipeProvider(this.items, this.fluids, this.chemicals),
              new OreProcessingRecipeProvider(this.items, this.fluids, this.chemicals),
              new OxidizingRecipeProvider(this.items, this.fluids, this.chemicals),
              new PaintingRecipeProvider(this.items, this.fluids, this.chemicals),
              new PigmentExtractingRecipeProvider(this.items, this.fluids, this.chemicals),
              new PigmentMixingRecipeProvider(this.items, this.fluids, this.chemicals),
              new PressurizedReactionRecipeProvider(this.items, this.fluids, this.chemicals),
              new RotaryRecipeProvider(this.items, this.fluids, this.chemicals),
              new SawingRecipeProvider(this.items, this.fluids, this.chemicals),
              new SeparatingRecipeProvider(this.items, this.fluids, this.chemicals),
              new StorageRecipeProvider(this.items, this.fluids, this.chemicals),
              new ThermalEvaporationRecipeProvider(this.items, this.fluids, this.chemicals),
              new TierInstallerRecipeProvider(this.items, this.fluids, this.chemicals),
              new TransmitterRecipeProvider(this.items, this.fluids, this.chemicals),
              new UpgradeRecipeProvider(this.items, this.fluids, this.chemicals, this.output.lookup(MekanismRegistries.Keys.UPGRADES))
        );
    }

    private void addMiscRecipes() {
        SpecialRecipeBuilder.special(() -> ClearConfigurationRecipe.INSTANCE).save(output, ResourceKey.create(Registries.RECIPE, MekanismRecipeSerializersInternal.CLEAR_CONFIGURATION.getId()));
        //Atomic disassembler
        MekDataShapedRecipeBuilder.shaped(MekanismItems.ATOMIC_DISASSEMBLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_ATOMIC))
              .save(output);
        //Boiler casing
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.BOILER_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output);
        //Boiler valve
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.BOILER_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, MekanismBlocks.BOILER_CASING)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.BOILER_CASING))
              .save(output);
        //Canteen
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.CANTEEN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.CONSTANT, this.items, ItemIds.BOWL)
              .category(RecipeCategory.FOOD)
              .unlockedBy("has_tin_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.TIN)))
              .unlockedBy(hasCriterion(ItemIds.BOWL))
              .save(output);
        //Cardboard box
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.CARDBOARD_BOX)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.RESIN, Pattern.EMPTY, Pattern.RESIN),
                    TripleLine.of(Pattern.CONSTANT, Pattern.RESIN, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_WOOD)
              .define(Pattern.RESIN, this.items, Tags.Items.CLUMPS_RESIN)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_sawdust", has(MekanismTags.Items.DUSTS_WOOD))
              .save(output);
        //Bio Fuel
        ExtendedShapelessRecipeBuilder.shapeless(MekanismItems.BIO_FUEL, 9)
              .requires(MekanismBlocks.BIO_FUEL_BLOCK)
              .unlockedBy(hasCriterion(MekanismBlocks.BIO_FUEL_BLOCK))
              .save(output);
        //Sulfur as dye
        ExtendedShapelessRecipeBuilder.shapeless(this.items, ItemIds.DYE.yellow())
              .requires(MekanismItems.SULFUR_DUST)
              .group("yellow_dye")//Vanilla group for things that make yellow dye
              .unlockedBy(hasCriterion(MekanismItems.SULFUR_DUST))
              .save(output, "sulfur_dye");
        //Charcoal
        ExtendedShapelessRecipeBuilder.shapeless(this.items, ItemIds.CHARCOAL, 9)
              .requires(MekanismBlocks.CHARCOAL_BLOCK)
              .unlockedBy(hasCriterion(ItemIds.CHARCOAL))
              .save(output, "charcoal");
        //Chargepad
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHARGEPAD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL))
              ).define(Pattern.CONSTANT, this.items, BlockItemIds.POLISHED_BLACKSTONE_PRESSURE_PLATE)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output);
        //Chemical crystallizer
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_CRYSTALLIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.GEMS_FLUORITE)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER))
              .save(output);
        //Chemical dissolution chamber
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER))
              .save(output);
        //Chemical infuser
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.TANK, Pattern.STEEL_CASING, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Chemical injection chamber
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .define(Pattern.CONSTANT, MekanismBlocks.PURIFICATION_CHAMBER)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.INJECTING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.PURIFICATION_CHAMBER))
              .save(output);
        //Chemical oxidizer
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_OXIDIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(PERSONAL_STORAGE_CHAR, Pattern.CONSTANT, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .define(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.BlockItems.PERSONAL_STORAGE)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Chemical washer
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.CHEMICAL_WASHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.BUCKET, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.BUCKET, MekanismBlocks.BASIC_FLUID_TANK)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER))
              .save(output);
        //Combiner
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.COMBINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.STEEL_CASING, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.COBBLESTONE, this.items, MekanismTags.Items.STONE_CRAFTING_MATERIALS)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.COMBINING.getSerializedName()))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ELITE))
              .save(output);
        //Configuration card
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.CONFIGURATION_CARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Configurator
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.CONFIGURATOR)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.INGOT, Pattern.INGOT),
                    DoubleLine.of(Pattern.EMPTY, Pattern.ALLOY),
                    DoubleLine.of(Pattern.EMPTY, Pattern.STEEL))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.ALLOY, osmiumIngot(this.items))
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_osmium_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output);
        //Crafting formula
        ExtendedShapelessRecipeBuilder.shapeless(MekanismItems.CRAFTING_FORMULA)
              .requires(this.items, ItemIds.PAPER)
              .requires(this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Crusher
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.CRUSHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_LAVA)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.CRUSHING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Dictionary
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.DICTIONARY)
              .pattern(RecipePattern.createPattern(
                    Pattern.CIRCUIT,
                    Pattern.CONSTANT)
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.CONSTANT, this.items, ItemIds.BOOK)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Digital miner
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.DIGITAL_MINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(SORTER_CHAR, ROBIT_CHAR, SORTER_CHAR),
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(SORTER_CHAR, MekanismBlocks.LOGISTICAL_SORTER)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .define(ROBIT_CHAR, MekanismItems.ROBIT)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismItems.ATOMIC_DISASSEMBLER))
              .unlockedBy("has_any_raw_ore", has(Tags.Items.RAW_MATERIALS))
              .unlockedBy("has_any_ore", has(Tags.BlockItems.ORES))
              .save(output);
        //Dosimeter
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.DOSIMETER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Dye Base
        ExtendedShapelessRecipeBuilder.shapeless(MekanismItems.DYE_BASE, 3)
              .requires(this.items, MekanismTags.Items.DUSTS_WOOD, 2)
              .requires(this.items, ItemIds.CLAY_BALL)
              .unlockedBy("has_clay", has(ItemIds.CLAY_BALL))
              .unlockedBy("has_sawdust", has(MekanismTags.Items.DUSTS_WOOD))
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_any_dye", has(Tags.Items.DYES))
              .save(output);
        //Dynamic tank
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.DYNAMIC_TANK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.BUCKET, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_EMPTY)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_bucket", has(Tags.Items.BUCKETS_EMPTY))
              .save(output);
        //Dynamic valve
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.DYNAMIC_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.DYNAMIC_TANK))
              .save(output);
        //Electric bow
        MekDataShapedRecipeBuilder.shaped(MekanismItems.ELECTRIC_BOW)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.ENERGY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.STRINGS)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.COMBAT)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output);
        //Electric pump
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.ELECTRIC_PUMP)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.BUCKET, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_EMPTY)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("in_water", insideOf(Blocks.WATER))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Electrolytic core
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.ELECTROLYTIC_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).define(Pattern.OSMIUM, this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.OSMIUM))
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD))
              .define(Pattern.INGOT, this.items, MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.IRON))
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .save(output);
        //Electrolytic separator
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.ELECTROLYTIC_SEPARATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismItems.ELECTROLYTIC_CORE))
              .unlockedBy("in_water", insideOf(Blocks.WATER))
              .save(output);
        //Energized smelter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.ENERGIZED_SMELTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(GLASS_CHAR, Pattern.STEEL_CASING, GLASS_CHAR),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.SMELTING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Energy tablet
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.ENERGY_TABLET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE))
              ).define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .save(output);
        //Enrichment chamber
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.ENRICHMENT_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.ENRICHING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .unlockedBy("has_any_raw_ore", has(Tags.Items.RAW_MATERIALS))
              .unlockedBy("has_any_ore", has(Tags.BlockItems.ORES))
              .save(output);
        //Flamethrower
        MekDataShapedRecipeBuilder.shaped(MekanismItems.FLAMETHROWER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.STEEL, this.items, Tags.Items.TOOLS_IGNITER)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_tank", has(MekanismBlocks.BASIC_CHEMICAL_TANK))
              .save(output);
        //Fluidic plenisher
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.FLUIDIC_PLENISHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.PREVIOUS, MekanismBlocks.ELECTRIC_PUMP)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.INGOT, tinIngot(this.items))
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              //Unlock at the same time as the electric puump without requiring crafting one
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Formulaic assemblicator
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.FORMULAIC_ASSEMBLICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.PREVIOUS, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).define(Pattern.CONSTANT, this.items, Tags.BlockItems.CHESTS_WOODEN)
              .define(Pattern.PREVIOUS, this.items, BlockItemIds.CRAFTER)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.REDSTONE)//Mirror vanilla crafter
              .unlockedBy(hasCriterion(BlockItemIds.CRAFTER))
              .save(output);
        //Free runners
        MekDataShapedRecipeBuilder.shaped(MekanismItems.FREE_RUNNERS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.EMPTY, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.EMPTY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.EMPTY, Pattern.ENERGY))
              ).define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output);
        //Armored Free Runners
        MekDataShapedRecipeBuilder.shaped(MekanismItems.ARMORED_FREE_RUNNERS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT))
              ).define(Pattern.PREVIOUS, MekanismItems.FREE_RUNNERS)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_DIAMOND)
              .define(Pattern.STEEL, this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_STEEL)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy("has_free_runners", has(MekanismItems.FREE_RUNNERS))
              .save(output);
        //Fuelwood heater
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.FUELWOOD_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Scuba mask
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.SCUBA_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, Pattern.EMPTY, Pattern.STEEL))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Gauge dropper
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.GAUGE_DROPPER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.EMPTY, GLASS_CHAR),
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_PANES)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_osmium_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .save(output);
        //Geiger Counter
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.GEIGER_COUNTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Hazmat Mask
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HAZMAT_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Hazmat Gown
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HAZMAT_GOWN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Hazmat Pants
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HAZMAT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Hazmat Boots
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HAZMAT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.DYE, this.items, Tags.Items.DYES_BLACK)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //HDPE rod
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HDPE_ROD)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
              .save(output);
        //HDPE stick
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HDPE_STICK)
              .pattern(RecipePattern.createPattern(
                    Pattern.CONSTANT,
                    Pattern.CONSTANT)
              ).define(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
              .save(output);
        //Industrial Alarm
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.INDUSTRIAL_ALARM)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.REDSTONE_LAMP)
              .category(RecipeCategory.REDSTONE)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
              .save(output);
        //Isotopic Centrifuge
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.ISOTOPIC_CENTRIFUGE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Jetpack
        MekDataShapedRecipeBuilder.shaped(MekanismItems.JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, tinIngot(this.items))
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy("has_tank", has(MekanismBlocks.BASIC_CHEMICAL_TANK))
              .save(output);
        //Jetpack armored
        MekDataShapedRecipeBuilder.shaped(MekanismItems.ARMORED_JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.PREVIOUS, Pattern.EMPTY))
              ).define(Pattern.PREVIOUS, MekanismItems.JETPACK)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .define(Pattern.STEEL, this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_STEEL)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_DIAMOND)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy("has_jetpack", has(MekanismItems.JETPACK))
              .save(output);
        //HDPE Elytra
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.HDPE_REINFORCED_ELYTRA)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.HDPE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.PREVIOUS, Pattern.HDPE_CHAR),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.EMPTY, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .define(Pattern.PREVIOUS, this.items, ItemIds.ELYTRA)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(ItemIds.ELYTRA))
              .save(output);
        //Laser
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.LASER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.GEMS_DIAMOND)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_REINFORCED))
              .save(output);
        //Laser amplifier
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.LASER_AMPLIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.GEMS_DIAMOND)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_ENERGY_CUBE)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.LASER))
              .save(output);
        //Laser tractor beam
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.LASER_TRACTOR_BEAM)
              .pattern(RecipePattern.createPattern(
                    PERSONAL_STORAGE_CHAR,
                    Pattern.CONSTANT)
              ).define(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.BlockItems.PERSONAL_STORAGE)
              .define(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .category(RecipeCategory.REDSTONE)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.LASER))
              .save(output);
        //Logistical sorter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.LOGISTICAL_SORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.PISTON)
              .category(RecipeCategory.REDSTONE)//Make it be in the same general area as hoppers
              .unlockedBy("has_any_transporter", has(MekanismTags.BlockItems.ITEM_TRANSMITTERS.item()))
              .save(output);
        //Metallurgic infuser
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.METALLURGIC_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.OSMIUM, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.INFUSING.getSerializedName()))
              .unlockedBy("has_osmium_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .save(output);
        //Network reader
        MekDataShapedRecipeBuilder.shaped(MekanismItems.NETWORK_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .unlockedBy("has_any_transmitter", has(MekanismTags.BlockItems.TRANSMITTERS.item()))
              .save(output);
        //Oredictionificator
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.OREDICTIONIFICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_PANES)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.PREVIOUS, MekanismItems.DICTIONARY)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.CHESTS_WOODEN)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismItems.DICTIONARY))
              .unlockedBy("has_any_raw_ore", has(Tags.Items.RAW_MATERIALS))
              .unlockedBy("has_any_ore", has(Tags.BlockItems.ORES))
              .save(output);
        //Osmium compressor
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.OSMIUM_COMPRESSOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_EMPTY)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.COMPRESSING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Paper
        ExtendedShapedRecipeBuilder.shaped(this.items, ItemIds.PAPER, 6)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_WOOD)
              .unlockedBy("has_sawdust", has(MekanismTags.Items.DUSTS_WOOD))
              .save(output, "paper");

        //Twice the number of slots need items in them than for vanilla chests as that means the backpack style feature is more useful then
        RecipeCriterion personalStorageLotsOfItems = RecipeProviderUtil.lotsOfItems(20);
        //Personal barrel
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PERSONAL_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.PREVIOUS, this.items, Tags.BlockItems.BARRELS_WOODEN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(personalStorageLotsOfItems)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Personal chest
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PERSONAL_CHEST)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.PREVIOUS, this.items, Tags.BlockItems.CHESTS_WOODEN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(personalStorageLotsOfItems)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Portable teleporter
        MekDataShapedRecipeBuilder.shaped(MekanismItems.PORTABLE_TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CIRCUIT, TELEPORTATION_CORE_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY))
              ).define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .save(output);
        //Precision sawmill
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PRECISION_SAWMILL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.SAWING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Pressure disperser
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PRESSURE_DISPERSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).define(Pattern.CONSTANT, this.items, Tags.BlockItems.BARS)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_casing", has(MekanismBlocks.BOILER_CASING))
              .save(output);
        //Pressurized reaction chamber
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.TANK, Pattern.CONSTANT, Pattern.TANK))
              ).define(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.ENRICHMENT_CHAMBER))
              .save(output);
        //Purification chamber
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.PURIFICATION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .group(Mekanism.rl(FactoryType.PURIFYING.getSerializedName()))
              .unlockedBy(hasCriterion(MekanismBlocks.ENRICHMENT_CHAMBER))
              .save(output);
        //Quantum entangloporter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.QUANTUM_ENTANGLOPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .save(output);
        //Rail
        ExtendedShapedRecipeBuilder.shaped(this.items, BlockItemIds.RAIL.item(), 24)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.CONSTANT, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.RODS_WOODEN)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy("has_minecart", has(ItemIds.MINECART))//Same unlock condition as vanilla rails
              .save(output, "rails");
        //Resistive heater
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.RESISTIVE_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.STEEL_CASING, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT))
              ).define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output);
        //Robit
        MekDataShapedRecipeBuilder.shaped(MekanismItems.ROBIT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.ENERGY, Pattern.ALLOY, Pattern.ENERGY),
                    TripleLine.of(Pattern.INGOT, PERSONAL_STORAGE_CHAR, Pattern.INGOT))
              ).define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.BlockItems.PERSONAL_STORAGE)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_ATOMIC))
              //Also teach the player about robits if they tame a pet
              .unlockedBy("tamed_animal", TameAnimalTrigger.TriggerInstance.tamedAnimal())
              .save(output);
        //Rotary condensentrator
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.ROTARY_CONDENSENTRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.TANK, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CONSTANT, MekanismBlocks.BASIC_FLUID_TANK)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_chemical_tank", has(MekanismBlocks.BASIC_CHEMICAL_TANK))
              .unlockedBy("has_fluid_tank", has(MekanismBlocks.BASIC_FLUID_TANK))
              .save(output);
        //Scuba tank
        MekDataShapedRecipeBuilder.shaped(MekanismItems.SCUBA_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CIRCUIT, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.TANK, Pattern.ALLOY),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output);
        //Security desk
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SECURITY_DESK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, MekanismItems.NETWORK_READER)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              //Note: We just require a steel casing instead of any higher tier circuits as we want to let people know about it once they might have any machines
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Seismic reader
        MekDataShapedRecipeBuilder.shaped(MekanismItems.SEISMIC_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.GEMS_LAPIS)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.TOOLS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismBlocks.SEISMIC_VIBRATOR))
              .save(output);
        //Seismic vibrator
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SEISMIC_VIBRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.GEMS_LAPIS)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .save(output);
        //Solar neutron activator
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ELITE))
              .save(output);
        //Steel casing
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.STEEL_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(GLASS_CHAR, Pattern.OSMIUM, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output);
        //Structural glass
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.STRUCTURAL_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_glass", has(Tags.BlockItems.GLASS_BLOCKS_CHEAP))
              .save(output);
        //Superheating element
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SUPERHEATING_ELEMENT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).define(Pattern.INGOT, this.items, BlockItemIds.COPPER_CHAIN.weathering().unaffected())
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.BUILDING_BLOCKS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_casing", has(MekanismBlocks.BOILER_CASING))
              .save(output);
        //Teleportation core
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.TELEPORTATION_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.DIAMOND, Pattern.INGOT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT))
              ).define(Pattern.CONSTANT, this.items, Tags.Items.ENDER_PEARLS)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .define(Pattern.DIAMOND, this.items, Tags.Items.GEMS_DIAMOND)
              .unlockedBy("has_ender_pearls", has(Tags.Items.ENDER_PEARLS))
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_ATOMIC))
              .save(output);
        //Teleporter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT))
              ).define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .save(output);
        //Teleporter frame
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.TELEPORTER_FRAME, 9)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.GLOWSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.GLOWSTONE, this.items, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE)
              .define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .category(RecipeCategory.TRANSPORTATION)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .save(output);
        //Base QIO Drive
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.BASE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.CIRCUIT, MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
              .define(Pattern.CONSTANT, this.items, Tags.Items.ENDER_PEARLS)
              .group(Mekanism.rl("qio_drive"))
              //Lots of slots occupied and at least ten are full
              .unlockedBy(RecipeProviderUtil.lotsOfItems(20, 10))
              .unlockedBy("has_circuit", has(MekanismItems.ULTIMATE_CONTROL_CIRCUIT))
              .save(output);
        //Hyper-Dense QIO Drive
        MekDataShapedRecipeBuilder.shaped(MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .define(Pattern.CONSTANT, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.PREVIOUS, MekanismItems.BASE_QIO_DRIVE)
              .group(Mekanism.rl("qio_drive"))
              .unlockedBy("has_plutonium", has(MekanismTags.Items.PELLETS_PLUTONIUM))
              .save(output);
        //Time-Dilating QIO Drive
        MekDataShapedRecipeBuilder.shaped(MekanismItems.TIME_DILATING_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.PREVIOUS, MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .group(Mekanism.rl("qio_drive"))
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //Supermassive QIO Drive
        MekDataShapedRecipeBuilder.shaped(MekanismItems.SUPERMASSIVE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .define(Pattern.PREVIOUS, MekanismItems.TIME_DILATING_QIO_DRIVE)
              .group(Mekanism.rl("qio_drive"))
              .unlockedBy("has_antimatter", has(MekanismTags.Items.PELLETS_ANTIMATTER))
              .save(output);
        //QIO Drive Array
        MekDataShapedRecipeBuilder.shaped(MekanismBlocks.QIO_DRIVE_ARRAY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.GLASS, TELEPORTATION_CORE_CHAR),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.INGOT, TELEPORTATION_CORE_CHAR))
              ).define(Pattern.CONSTANT, this.items, MekanismTags.BlockItems.PERSONAL_STORAGE)
              .define(Pattern.INGOT, this.items, Tags.Items.ENDER_PEARLS)
              .define(Pattern.GLASS, this.items, Tags.BlockItems.GLASS_PANES)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .save(output);
        //QIO Redstone Adapter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.QIO_REDSTONE_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.WOOD, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.REDSTONE, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, Tags.Items.ENDER_PEARLS)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.WOOD, this.items, BlockItemIds.REDSTONE_TORCH)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .category(RecipeCategory.REDSTONE)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              .unlockedBy("has_redstone_torch", has(BlockItemIds.REDSTONE_TORCH))
              .save(output);
        //QIO Exporter
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.QIO_EXPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.PISTON)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.INGOT, leadIngot(this.items))
              .category(RecipeCategory.REDSTONE)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              .save(output);
        //QIO Importer
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.QIO_IMPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.STICKY_PISTON)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.INGOT, leadIngot(this.items))
              .category(RecipeCategory.REDSTONE)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              .save(output);
        //QIO Dashboard
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.GLASS, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).define(Pattern.GLASS, this.items, Tags.BlockItems.GLASS_PANES)
              .define(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.INGOT, leadIngot(this.items))
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(MekanismItems.TELEPORTATION_CORE))
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Portable QIO Dashboard
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.PORTABLE_QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.CONSTANT, MekanismBlocks.QIO_DASHBOARD)
              .define(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .category(RecipeCategory.TOOLS)
              .unlockedBy("has_dashboard", has(MekanismBlocks.QIO_DASHBOARD))
              .save(output);
        //Meka-Tool
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MEKA_TOOL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, 'o', Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define('o', MekanismItems.CONFIGURATOR)
              .define(Pattern.CONSTANT, MekanismItems.ATOMIC_DISASSEMBLER)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .unlockedBy(hasCriterion(MekanismItems.ATOMIC_DISASSEMBLER))
              .save(output);
        //MekaSuit Helmet
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MEKASUIT_HELMET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, this.items, ItemIds.NETHERITE_HELMET)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //MekaSuit Bodyarmor
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MEKASUIT_BODYARMOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, this.items, ItemIds.NETHERITE_CHESTPLATE)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //MekaSuit Pants
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MEKASUIT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, this.items, ItemIds.NETHERITE_LEGGINGS)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //MekaSuit Boots
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MEKASUIT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, this.items, ItemIds.NETHERITE_BOOTS)
              .define(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //SPS Casing
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SPS_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).define(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_plutonium", has(MekanismTags.Items.PELLETS_PLUTONIUM))
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //SPS Port
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SPS_PORT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, MekanismBlocks.SPS_CASING)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(MekanismBlocks.SPS_CASING))
              .save(output);
        //Supercharged Coil
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.SUPERCHARGED_COIL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.CONSTANT, MekanismBlocks.LASER)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_COPPER)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.BUILDING_BLOCKS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_casing", has(MekanismBlocks.SPS_CASING))
              .save(output);
        //Nutritional Liquifier
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.NUTRITIONAL_LIQUIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.CONSTANT, this.items, ItemIds.BOWL)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy(hasCriterion(MekanismBlocks.STEEL_CASING))
              .unlockedBy(hasCriterion(ItemIds.BOWL))
              .save(output);
        //Pigment Extractor
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PIGMENT_EXTRACTOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.CONSTANT, this.items, ItemIds.FLINT)
              .category(RecipeCategory.DECORATIONS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_any_dye", has(Tags.Items.DYES))
              .save(output);
        //Pigment Mixer
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PIGMENT_MIXER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .category(RecipeCategory.DECORATIONS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_any_dye", has(Tags.Items.DYES))
              .save(output);
        //Painting Machine
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.PAINTING_MACHINE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, MekanismItems.DYE_BASE)
              .category(RecipeCategory.DECORATIONS)
              //Mirror how vanilla does certain things by requiring a related item rather than one actually necessary to craft it
              .unlockedBy("has_any_dye", has(Tags.Items.DYES))
              .save(output);
        //Modification Station
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.MODIFICATION_STATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.WOOD, Pattern.PLASTIC),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.WOOD, this.items, Tags.BlockItems.CHESTS_WOODEN)
              .define(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output);
        //Antiprotonic Nucleosynthesizer
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.CONSTANT, MekanismItems.ATOMIC_ALLOY)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_antimatter", has(MekanismTags.Items.PELLETS_ANTIMATTER))
              .save(output);
        //Radioactive Waste Barrel
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.RADIOACTIVE_WASTE_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, leadIngot(this.items))
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output);
        //Dimensional Stabilizer
        ExtendedShapedRecipeBuilder.shaped(MekanismBlocks.DIMENSIONAL_STABILIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.DIAMOND, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).define(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .define(Pattern.DIAMOND, this.items, Tags.BlockItems.STORAGE_BLOCKS_DIAMOND)
              .category(RecipeCategory.DECORATIONS)//Mirror vanilla work stations
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_ATOMIC))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ULTIMATE))
              .save(output);
    }

    private void addGearModuleRecipes() {
        //Module Base
        ExtendedShapedRecipeBuilder.shaped(MekanismItems.MODULE_BASE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET))
              ).define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.NUGGET, this.items, MekanismTags.Items.NUGGETS_BRONZE)
              .define(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .unlockedBy("has_plastic", has(MekanismItems.HDPE_PELLET))
              .unlockedBy("has_plastic_sheet", has(MekanismItems.HDPE_SHEET))
              .save(output);
        //Jetpack Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.JETPACK_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, Ingredient.of(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK))
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.JETPACK_UNIT.getId());
        //Electrolytic Breathing Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.ELECTROLYTIC_BREATHING_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.ELECTROLYTIC_BREATHING_UNIT.getId());
        //Dosimeter Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.DOSIMETER_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.DOSIMETER)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.DOSIMETER_UNIT.getId());
        //Geiger Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.GEIGER_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.GEIGER_COUNTER)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.GEIGER_UNIT.getId());
        //Energy Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.ENERGY_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_CELL)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.ENERGY_UNIT.getId());
        //Laser Dissipation Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.LASER_DISSIPATION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.COMBAT)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.LASER_DISSIPATION_UNIT.getId());
        //Radiation Shielding Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.RADIATION_SHIELDING_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(BlockResourceInfo.LEAD))
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.RADIATION_SHIELDING_UNIT.getId());
        //Color Modulation Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.COLOR_MODULATION_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(MIXING_CHAR, Pattern.CONSTANT, MIXING_CHAR),
                    TripleLine.of(Pattern.OTHER, Pattern.PREVIOUS, Pattern.OTHER),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(MIXING_CHAR, MekanismBlocks.PIGMENT_MIXER)
              .define(Pattern.OTHER, MekanismBlocks.PAINTING_MACHINE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismBlocks.LASER)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.COLOR_MODULATION_UNIT.getId());
        //Charge Distribution Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.CHARGE_DISTRIBUTION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.CHARGE_DISTRIBUTION_UNIT.getId());
        //Teleportation Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.TELEPORTATION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.TELEPORTATION_CORE)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.TELEPORTATION_UNIT.getId());
        //Nutritional Injection Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.NUTRITIONAL_INJECTION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.CANTEEN)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.FOOD)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.NUTRITIONAL_INJECTION_UNIT.getId());
        //Silk Touch Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.SILK_TOUCH_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.DIAMOND, Pattern.PREVIOUS, Pattern.DIAMOND),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.DIAMOND, this.items, ItemIds.DIAMOND_PICKAXE)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.SILK_TOUCH_UNIT.getId());
        //Fortune Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.FORTUNE_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.DIAMOND, Pattern.PREVIOUS, Pattern.DIAMOND),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.DIAMOND, this.items, Tags.BlockItems.STORAGE_BLOCKS_DIAMOND)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.FORTUNE_UNIT.getId());
        //Blasting Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.BLASTING_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.TNT)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.BLASTING_UNIT.getId());
        //Excavation Escalation Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.EXCAVATION_ESCALATION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, ItemIds.IRON_PICKAXE)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.EXCAVATION_ESCALATION_UNIT.getId());
        //Attack Amplification Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.ATTACK_AMPLIFICATION_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, ItemIds.IRON_SWORD)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.COMBAT)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.ATTACK_AMPLIFICATION_UNIT.getId());
        //Farming Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.FARMING_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, ItemIds.IRON_HOE)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.FARMING_UNIT.getId());
        //Shearing Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.SHEARING_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, Tags.Items.TOOLS_SHEAR)
              .define(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.SHEARING_UNIT.getId());
        //Vein Mining Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.VEIN_MINING_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of('x', Pattern.PREVIOUS, 's'),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, ItemIds.DIAMOND_PICKAXE)
              .define('x', this.items, ItemIds.DIAMOND_AXE)
              .define('s', this.items, ItemIds.DIAMOND_SHOVEL)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.VEIN_MINING_UNIT.getId());
        //Vision Enhancement Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.VISION_ENHANCEMENT_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, Tags.Items.GEMS_EMERALD)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.VISION_ENHANCEMENT_UNIT.getId());
        //Inhalation Purification Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.INHALATION_PURIFICATION_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, 'o', Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.HAZMAT_MASK)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define('o', MekanismItems.SCUBA_MASK)
              .category(RecipeCategory.COMBAT)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.INHALATION_PURIFICATION_UNIT.getId());
        //Magnetic Attraction Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.MAGNETIC_ATTRACTION_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.BARS_IRON)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.MAGNETIC_ATTRACTION_UNIT.getId());
        //Frost Walker Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.FROST_WALKER_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismFluids.HYDROGEN.getBucket())
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.FROST_WALKER_UNIT.getId());
        //Soul Speed Module
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.SOUL_SURFER_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.BLOCK, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.BLOCK, this.items, ItemTags.SOUL_FIRE_BASE_BLOCKS)
              .define(Pattern.CONSTANT, Ingredient.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS))
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.SOUL_SURFER_UNIT.getId());
        //Locomotive Boosting Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.LOCOMOTIVE_BOOSTING_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, ItemIds.DIAMOND_LEGGINGS)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.LOCOMOTIVE_BOOSTING_UNIT.getId());
        //Hydraulic Propulsion Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.HYDRAULIC_PROPULSION_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, Ingredient.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS))
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.HYDRAULIC_PROPULSION_UNIT.getId());
        //Gyroscopic Stabilization Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.GYROSCOPIC_STABILIZATION_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, Tags.BlockItems.OBSIDIANS_NORMAL)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TOOLS)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.GYROSCOPIC_STABILIZATION_UNIT.getId());
        //Hydrostatic Repulsion Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.HYDROSTATIC_REPULSOR_UNIT))
              .pattern(BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.LIGHTNING_ROD.weathering().unaffected())
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.HYDROSTATIC_REPULSOR_UNIT.getId());
        //Motorized Servo Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.MOTORIZED_SERVO_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.CONSTANT, this.items, BlockItemIds.BLUE_ICE)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.MOTORIZED_SERVO_UNIT.getId());
        //Gravitational Modulating Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.GRAVITATIONAL_MODULATING_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, this.items, Tags.Items.NETHER_STARS)
              .define(Pattern.ENERGY, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.GRAVITATIONAL_MODULATING_UNIT.getId());
        //Elytra Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(MekanismModules.ELYTRA_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.NUGGET, Pattern.HDPE_CHAR))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, MekanismItems.HDPE_REINFORCED_ELYTRA)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .define(Pattern.NUGGET, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .category(RecipeCategory.TRANSPORTATION)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, MekanismModules.ELYTRA_UNIT.getId());
    }

    private void addLateGameRecipes() {
        String basePath = "processing/lategame/";

        //plutonium
        ChemicalToChemicalRecipeBuilder.centrifuging(
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.NUCLEAR_WASTE, 10),
                    chemicalTemplate(ChemicalIds.PLUTONIUM, 1)
              ).unlockedBy("has_waste_barrel", has(MekanismBlocks.RADIOACTIVE_WASTE_BARREL))
              .save(output, basePath + "plutonium");
        //polonium
        ChemicalToChemicalRecipeBuilder.activating(
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.NUCLEAR_WASTE, 10),
                    chemicalTemplate(ChemicalIds.POLONIUM, 1)
              ).unlockedBy("has_waste_barrel", has(MekanismBlocks.RADIOACTIVE_WASTE_BARREL))
              .save(output, basePath + "polonium");

        //plutonium pellet
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_FLUORITE),
                    IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.PLUTONIUM, 1_000),
                    100,
                    MekanismItems.PLUTONIUM_PELLET.asTemplate(),
                    chemicalTemplate(ChemicalIds.SPENT_NUCLEAR_WASTE, 1_000)
              ).unlockedBy("has_fluorite", has(MekanismTags.Items.DUSTS_FLUORITE))
              .save(output, basePath + "plutonium_pellet/from_reaction");
        //polonium pellet
        PressurizedReactionRecipeBuilder.reaction(
                    IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_FLUORITE),
                    IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.POLONIUM, 1_000),
                    100,
                    MekanismItems.POLONIUM_PELLET.asTemplate(),
                    chemicalTemplate(ChemicalIds.SPENT_NUCLEAR_WASTE, 1_000)
              ).unlockedBy("has_fluorite", has(MekanismTags.Items.DUSTS_FLUORITE))
              .save(output, basePath + "polonium_pellet/from_reaction");

        //antimatter pellet
        ChemicalCrystallizerRecipeBuilder.crystallizing(
                    IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.ANTIMATTER, 1_000),
                    MekanismItems.ANTIMATTER_PELLET.asTemplate()
              ).unlockedBy("has_pellets", BaseAdvancementProvider.hasItems(items,
                    //Note: Requires both pellets
                    MekanismTags.Items.PELLETS_PLUTONIUM,
                    MekanismTags.Items.PELLETS_POLONIUM
              )).save(output, basePath + "antimatter_pellet/from_gas");

        //back to antimatter
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.PELLETS_ANTIMATTER),
              chemicalTemplate(ChemicalIds.ANTIMATTER, 1_000)
        ).group(ChemicalIds.ANTIMATTER)
              .unlockedBy("has_antimatter", has(MekanismTags.Items.PELLETS_ANTIMATTER))
              .save(output, basePath + "antimatter/from_pellet");
    }
}