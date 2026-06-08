package mekanism.common.recipe.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.PressurizedReactionRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ClearConfigurationRecipe;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.compat.AE2RecipeProvider;
import mekanism.common.recipe.compat.BWGRecipeProvider;
import mekanism.common.recipe.compat.BiomesOPlentyRecipeProvider;
import mekanism.common.recipe.compat.FarmersDelightRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.DoubleLine;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismRecipeSerializersInternal;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.Tags;

@NothingNullByDefault
public class MekanismRecipeProvider extends BaseRecipeProvider {

    static final char DIAMOND_CHAR = 'D';
    static final char GLASS_CHAR = 'G';
    static final char PERSONAL_STORAGE_CHAR = 'P';
    static final char MIXING_CHAR = 'M';
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
    static final RecipePattern TYPED_STORAGE_PATTERN = RecipePattern.createPattern(
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
          TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT));
    public static final RecipePattern BASIC_MODULE = RecipePattern.createPattern(
          TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
          TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
          TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR));

    private final List<ISubRecipeProvider> compatProviders = new ArrayList<>();
    private final Set<String> disabledCompats;

    public MekanismRecipeProvider(HolderLookup.Provider registries, RecipeOutput output, Set<String> disabledCompats) {
        super(output, registries);

        //Mod Compat Recipe providers
        this.disabledCompats = disabledCompats;
        checkCompat("ae2", AE2RecipeProvider::new);
        checkCompat("biomesoplenty", BiomesOPlentyRecipeProvider::new);
        checkCompat("biomeswevegone", BWGRecipeProvider::new);
        checkCompat("farmersdelight", FarmersDelightRecipeProvider::new);
    }

    private void checkCompat(String modid, BiFunction<HolderLookup.Provider, String, ISubRecipeProvider> providerCreator) {
        if (ModList.get().isLoaded(modid)) {
            compatProviders.add(providerCreator.apply(this.registries, modid));
        } else {
            disabledCompats.add(modid);
        }
    }

    @Override
    protected void addRecipes(HolderLookup.Provider registries) {
        addMiscRecipes();
        addGearModuleRecipes();
        addLateGameRecipes();
        for (ISubRecipeProvider compatProvider : compatProviders) {
            compatProvider.addRecipes(output, registries);
        }
    }

    @Override
    protected List<ISubRecipeProvider> getSubRecipeProviders() {
        return List.of(
              new BinRecipeProvider(this.items),
              new ChemicalInfuserRecipeProvider(),
              new ChemicalInjectorRecipeProvider(this.items, this.chemicals),
              new ChemicalTankRecipeProvider(this.items),
              new CombinerRecipeProvider(this.items),
              new ControlCircuitRecipeProvider(this.items, this.chemicals),
              new CrusherRecipeProvider(this.items),
              new ChemicalCrystallizerRecipeProvider(),
              new EnergyConversionRecipeProvider(this.items),
              new EnergyCubeRecipeProvider(this.items),
              new EnrichingRecipeProvider(this.items),
              new EvaporatingRecipeProvider(),
              new FactoryRecipeProvider(this.items),
              new FluidTankRecipeProvider(this.items),
              new GasConversionRecipeProvider(this.items),
              new InductionRecipeProvider(this.items),
              new InfusionConversionRecipeProvider(this.items),
              new MetallurgicInfuserRecipeProvider(this.items, this.chemicals),
              new NucleosynthesizingRecipeProvider(this.items),
              new OreProcessingRecipeProvider(this.items, this.fluids, this.chemicals),
              new OxidizingRecipeProvider(this.items),
              new PaintingRecipeProvider(this.items),
              new PigmentExtractingRecipeProvider(this.items),
              new PigmentMixingRecipeProvider(),
              new PressurizedReactionRecipeProvider(this.items, this.fluids),
              new RotaryRecipeProvider(this.fluids),
              new SawingRecipeProvider(this.items),
              new SeparatingRecipeProvider(this.fluids),
              new StorageRecipeProvider(this.items),
              new ThermalEvaporationRecipeProvider(this.items),
              new TierInstallerRecipeProvider(this.items),
              new TransmitterRecipeProvider(this.items),
              new UpgradeRecipeProvider(this.items)
        );
    }

    private void addMiscRecipes() {
        SpecialRecipeBuilder.special(() -> ClearConfigurationRecipe.INSTANCE).save(output, ResourceKey.create(Registries.RECIPE, MekanismRecipeSerializersInternal.CLEAR_CONFIGURATION.getId()));
        //Atomic disassembler
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ATOMIC_DISASSEMBLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Boiler casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .save(output);
        //Boiler valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.BOILER_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.BOILER_CASING)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .save(output);
        //Canteen
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CANTEEN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, tinIngot(this.items))
              .key(Pattern.CONSTANT, Items.BOWL)
              .category(RecipeCategory.FOOD)
              .save(output);
        //Cardboard box
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CARDBOARD_BOX)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_WOOD)
              .save(output);
        //Bio Fuel
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.BIO_FUEL, 9)
              .addIngredient(MekanismBlocks.BIO_FUEL_BLOCK)
              .save(output);
        //Sulfur as dye
        ExtendedShapelessRecipeBuilder.shapelessRecipe(Items.YELLOW_DYE.builtInRegistryHolder())
              .addIngredient(MekanismItems.SULFUR_DUST)
              .save(output, Mekanism.rl("sulfur_dye"));
        //Charcoal
        ExtendedShapelessRecipeBuilder.shapelessRecipe(Items.CHARCOAL.builtInRegistryHolder(), 9)
              .addIngredient(MekanismBlocks.CHARCOAL_BLOCK)
              .save(output, Mekanism.rl("charcoal"));
        //Chargepad
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHARGEPAD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .save(output);
        //Chemical crystallizer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_CRYSTALLIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.GEMS_FLUORITE)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Chemical dissolution chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Chemical infuser
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.TANK, Pattern.STEEL_CASING, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Chemical injection chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_INJECTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.CONSTANT, MekanismBlocks.PURIFICATION_CHAMBER)
              .save(output);
        //Chemical oxidizer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_OXIDIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(PERSONAL_STORAGE_CHAR, Pattern.CONSTANT, Pattern.TANK),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.Items.PERSONAL_STORAGE)
              .save(output);
        //Chemical washer
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CHEMICAL_WASHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.BUCKET, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.BUCKET, MekanismBlocks.BASIC_FLUID_TANK)
              .save(output);
        //Combiner
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.COMBINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.COBBLESTONE, Pattern.STEEL_CASING, Pattern.COBBLESTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.COBBLESTONE, this.items, MekanismTags.Items.STONE_CRAFTING_MATERIALS)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Configuration card
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CONFIGURATION_CARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.ALLOY, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(output);
        //Configurator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.CONFIGURATOR)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.INGOT, Pattern.INGOT),
                    DoubleLine.of(Pattern.EMPTY, Pattern.ALLOY),
                    DoubleLine.of(Pattern.EMPTY, Pattern.STEEL))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.ALLOY, osmiumIngot(this.items))
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .save(output);
        //Crafting formula
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.CRAFTING_FORMULA)
              .addIngredient(Items.PAPER)
              .addIngredient(this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .save(output);
        //Crusher
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.CRUSHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_LAVA)
              .save(output);
        //Dictionary
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.DICTIONARY)
              .pattern(RecipePattern.createPattern(
                    Pattern.CIRCUIT,
                    Pattern.CONSTANT)
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.BOOK)
              .save(output);
        //Digital miner
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DIGITAL_MINER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(SORTER_CHAR, ROBIT_CHAR, SORTER_CHAR),
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(SORTER_CHAR, MekanismBlocks.LOGISTICAL_SORTER)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(ROBIT_CHAR, MekanismItems.ROBIT)
              .save(output);
        //Dosimeter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.DOSIMETER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Dye Base
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.DYE_BASE, 3)
              .addIngredient(this.items, MekanismTags.Items.DUSTS_WOOD, 2)
              .addIngredient(Items.CLAY_BALL)
              .save(output);
        //Dynamic tank
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_TANK, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.BUCKET, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.BUCKET, Items.BUCKET)
              .save(output);
        //Dynamic valve
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DYNAMIC_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .save(output);
        //Electric bow
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ELECTRIC_BOW)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.ENERGY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.STRINGS)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //Electric pump
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ELECTRIC_PUMP)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.BUCKET, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).key(Pattern.BUCKET, Items.BUCKET)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Electrolytic core
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ELECTROLYTIC_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.OSMIUM, Pattern.ALLOY))
              ).key(Pattern.OSMIUM, this.items, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.OSMIUM))
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD))
              .key(Pattern.INGOT, this.items, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON))
              .save(output);
        //Electrolytic separator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ELECTROLYTIC_SEPARATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .save(output);
        //Energized smelter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ENERGIZED_SMELTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(GLASS_CHAR, Pattern.STEEL_CASING, GLASS_CHAR),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Energy tablet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.ENERGY_TABLET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.REDSTONE, Pattern.INGOT, Pattern.REDSTONE))
              ).key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(output);
        //Enrichment chamber
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ENRICHMENT_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Flamethrower
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.FLAMETHROWER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.INGOT, tinIngot(this.items))
              .key(Pattern.STEEL, Items.FLINT_AND_STEEL)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Fluidic plenisher
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FLUIDIC_PLENISHER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ELECTRIC_PUMP)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, tinIngot(this.items))
              .save(output);
        //Formulaic assemblicator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FORMULAIC_ASSEMBLICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.PREVIOUS, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.PREVIOUS, Items.CRAFTER)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Free runners
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.FREE_RUNNERS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.EMPTY, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.EMPTY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.EMPTY, Pattern.ENERGY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.TRANSPORTATION)
              .save(output);
        //Armored Free Runners
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ARMORED_FREE_RUNNERS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT))
              ).key(Pattern.PREVIOUS, MekanismItems.FREE_RUNNERS)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_DIAMOND)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .category(RecipeCategory.TRANSPORTATION)
              .save(output);
        //Fuelwood heater
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.FUELWOOD_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, Items.FURNACE)
              .save(output);
        //Scuba mask
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.SCUBA_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, Pattern.EMPTY, Pattern.STEEL))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Gauge dropper
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GAUGE_DROPPER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.EMPTY, GLASS_CHAR),
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_PANES)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Geiger Counter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.GEIGER_COUNTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Hazmat Mask
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_MASK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Hazmat Gown
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_GOWN)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Hazmat Pants
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.DYE, this.items, Tags.Items.DYES_ORANGE)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Hazmat Boots
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HAZMAT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.DYE, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.DYE, this.items, Tags.Items.DYES_BLACK)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //HDPE rod
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_ROD)
              .pattern(RecipePattern.createPattern(
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT),
                    DoubleLine.of(Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_PELLET)
              .save(output);
        //HDPE stick
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_STICK)
              .pattern(RecipePattern.createPattern(
                    Pattern.CONSTANT,
                    Pattern.CONSTANT)
              ).key(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .save(output);
        //Industrial Alarm
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.INDUSTRIAL_ALARM)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.REDSTONE_LAMP)
              .category(RecipeCategory.REDSTONE)
              .save(output);
        //Isotopic Centrifuge
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ISOTOPIC_CENTRIFUGE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.TANK, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .save(output);
        //Jetpack
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CIRCUIT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, tinIngot(this.items))
              .category(RecipeCategory.TRANSPORTATION)
              .save(output);
        //Jetpack armored
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ARMORED_JETPACK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.EMPTY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.PREVIOUS, Pattern.EMPTY))
              ).key(Pattern.PREVIOUS, MekanismItems.JETPACK)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_DIAMOND)
              .category(RecipeCategory.TRANSPORTATION)
              .save(output);
        //HDPE Elytra
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.HDPE_REINFORCED_ELYTRA)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.HDPE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.PREVIOUS, Pattern.HDPE_CHAR),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.EMPTY, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .key(Pattern.PREVIOUS, Items.ELYTRA)
              .save(output);
        //Laser
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.GEMS_DIAMOND)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .save(output);
        //Laser amplifier
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER_AMPLIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.GEMS_DIAMOND)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_ENERGY_CUBE)
              .save(output);
        //Laser tractor beam
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LASER_TRACTOR_BEAM)
              .pattern(RecipePattern.createPattern(
                    PERSONAL_STORAGE_CHAR,
                    Pattern.CONSTANT)
              ).key(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.Items.PERSONAL_STORAGE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .save(output);
        //Logistical sorter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.LOGISTICAL_SORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.CONSTANT, Items.PISTON)
              .save(output);
        //Metallurgic infuser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.METALLURGIC_INFUSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.OSMIUM, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.FURNACE)
              .save(output);
        //Network reader
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.NETWORK_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .save(output);
        //Oredictionificator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.OREDICTIONIFICATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_PANES)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.PREVIOUS, MekanismItems.DICTIONARY)
              .key(Pattern.CONSTANT, this.items, Tags.Items.CHESTS_WOODEN)
              .save(output);
        //Osmium compressor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.OSMIUM_COMPRESSOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.BUCKET, Pattern.STEEL_CASING, Pattern.BUCKET),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.BUCKET, Items.BUCKET)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Paper
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.PAPER.builtInRegistryHolder(), 6)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, this.items, MekanismTags.Items.DUSTS_WOOD)
              .save(output, Mekanism.rl("paper"));
        //Personal barrel
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PERSONAL_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.PREVIOUS, this.items, Tags.Items.BARRELS_WOODEN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .category(RecipeCategory.DECORATIONS)
              .save(output);
        //Personal chest
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PERSONAL_CHEST)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CIRCUIT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.PREVIOUS, this.items, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .category(RecipeCategory.DECORATIONS)
              .save(output);
        //Portable teleporter
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.PORTABLE_TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY),
                    TripleLine.of(Pattern.CIRCUIT, TELEPORTATION_CORE_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.EMPTY, Pattern.ENERGY, Pattern.EMPTY))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .save(output);
        //Precision sawmill
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRECISION_SAWMILL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Pressure disperser
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURE_DISPERSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(Pattern.CONSTANT, Items.IRON_BARS)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(output);
        //Pressurized reaction chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.TANK, Pattern.CONSTANT, Pattern.TANK))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CONSTANT, MekanismBlocks.DYNAMIC_TANK)
              .save(output);
        //Purification chamber
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PURIFICATION_CHAMBER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.PREVIOUS, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.PREVIOUS, MekanismBlocks.ENRICHMENT_CHAMBER)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .save(output);
        //Quantum entangloporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QUANTUM_ENTANGLOPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .save(output);
        //Rail
        ExtendedShapedRecipeBuilder.shapedRecipe(Items.RAIL.builtInRegistryHolder(), 24)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.CONSTANT, Pattern.OSMIUM),
                    TripleLine.of(Pattern.OSMIUM, Pattern.EMPTY, Pattern.OSMIUM))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.RODS_WOODEN)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .save(output, Mekanism.rl("rails"));
        //Resistive heater
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RESISTIVE_HEATER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.REDSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.STEEL_CASING, Pattern.REDSTONE),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT))
              ).key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.INGOT, tinIngot(this.items))
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Robit
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.ROBIT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.ENERGY, Pattern.ALLOY, Pattern.ENERGY),
                    TripleLine.of(Pattern.INGOT, PERSONAL_STORAGE_CHAR, Pattern.INGOT))
              ).key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(PERSONAL_STORAGE_CHAR, this.items, MekanismTags.Items.PERSONAL_STORAGE)
              .save(output);
        //Rotary condensentrator
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ROTARY_CONDENSENTRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR),
                    TripleLine.of(Pattern.TANK, Pattern.ENERGY, Pattern.CONSTANT),
                    TripleLine.of(GLASS_CHAR, Pattern.CIRCUIT, GLASS_CHAR))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_FLUID_TANK)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .save(output);
        //Scuba tank
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SCUBA_TANK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CIRCUIT, Pattern.EMPTY),
                    TripleLine.of(Pattern.ALLOY, Pattern.TANK, Pattern.ALLOY),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.TOOLS)
              .save(output);
        //Security desk
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SECURITY_DESK)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.NETWORK_READER)
              .save(output);
        //Seismic reader
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SEISMIC_READER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.CONSTANT, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ENERGY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.STEEL, Pattern.STEEL))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.GEMS_LAPIS)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .save(output);
        //Seismic vibrator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SEISMIC_VIBRATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.GEMS_LAPIS)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.INGOT, tinIngot(this.items))
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Solar neutron activator
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_BRONZE)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .save(output);
        //Steel casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STEEL_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(GLASS_CHAR, Pattern.OSMIUM, GLASS_CHAR),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.OSMIUM, osmiumIngot(this.items))
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .save(output);
        //Structural glass
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.STRUCTURAL_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, GLASS_CHAR, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(GLASS_CHAR, this.items, Tags.Items.GLASS_BLOCKS_CHEAP)
              .key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .save(output);
        //Superheating element
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SUPERHEATING_ELEMENT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY))
              ).key(Pattern.INGOT, this.items, Tags.Items.INGOTS_COPPER)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Teleportation core
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.TELEPORTATION_CORE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT),
                    TripleLine.of(Pattern.INGOT, DIAMOND_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.CONSTANT, Pattern.ALLOY, Pattern.CONSTANT))
              ).key(Pattern.CONSTANT, this.items, Tags.Items.ENDER_PEARLS)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .key(DIAMOND_CHAR, this.items, Tags.Items.GEMS_DIAMOND)
              .save(output);
        //Teleporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.TELEPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL_CASING, TELEPORTATION_CORE_CHAR, Pattern.STEEL_CASING),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT))
              ).key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .save(output);
        //Teleporter frame
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.TELEPORTER_FRAME, 9)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.GLOWSTONE, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.GLOWSTONE, this.items, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE)
              .key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .save(output);
        //Base QIO Drive
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BASE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, leadIngot(this.items))
              .key(Pattern.CIRCUIT, MekanismItems.ULTIMATE_CONTROL_CIRCUIT)
              .key(Pattern.CONSTANT, this.items, Tags.Items.ENDER_PEARLS)
              .save(output);
        //Hyper-Dense QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .key(Pattern.CONSTANT, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.PREVIOUS, MekanismItems.BASE_QIO_DRIVE)
              .save(output);
        //Time-Dilating QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.TIME_DILATING_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.PREVIOUS, MekanismItems.HYPER_DENSE_QIO_DRIVE)
              .save(output);
        //Supermassive QIO Drive
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismItems.SUPERMASSIVE_QIO_DRIVE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.CONSTANT, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.PREVIOUS, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .key(Pattern.PREVIOUS, MekanismItems.TIME_DILATING_QIO_DRIVE)
              .save(output);
        //QIO Drive Array
        MekDataShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_DRIVE_ARRAY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.GLASS, TELEPORTATION_CORE_CHAR),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(TELEPORTATION_CORE_CHAR, Pattern.INGOT, TELEPORTATION_CORE_CHAR))
              ).key(Pattern.CONSTANT, this.items, MekanismTags.Items.PERSONAL_STORAGE)
              .key(Pattern.INGOT, this.items, Tags.Items.ENDER_PEARLS)
              .key(Pattern.GLASS, this.items, Tags.Items.GLASS_PANES)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .save(output);
        //QIO Redstone Adapter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_REDSTONE_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.WOOD, Pattern.INGOT),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.REDSTONE, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, Tags.Items.ENDER_PEARLS)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.WOOD, Items.REDSTONE_TORCH)
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .save(output);
        //QIO Exporter
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_EXPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .key(Pattern.CONSTANT, Items.PISTON)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, leadIngot(this.items))
              .save(output);
        //QIO Importer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_IMPORTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .key(Pattern.CONSTANT, Items.STICKY_PISTON)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, leadIngot(this.items))
              .save(output);
        //QIO Dashboard
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, Pattern.GLASS, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, TELEPORTATION_CORE_CHAR, Pattern.INGOT))
              ).key(Pattern.GLASS, this.items, Tags.Items.GLASS_PANES)
              .key(Pattern.ALLOY, this.items, Tags.Items.ENDER_PEARLS)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.INGOT, leadIngot(this.items))
              .save(output);
        //Portable QIO Dashboard
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.PORTABLE_QIO_DASHBOARD)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, TELEPORTATION_CORE_CHAR, Pattern.ALLOY))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CONSTANT, MekanismBlocks.QIO_DASHBOARD)
              .key(TELEPORTATION_CORE_CHAR, MekanismItems.TELEPORTATION_CORE)
              .save(output);
        //Meka-Tool
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKA_TOOL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, 'o', Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key('o', MekanismItems.CONFIGURATOR)
              .key(Pattern.CONSTANT, MekanismItems.ATOMIC_DISASSEMBLER)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //MekaSuit Helmet
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_HELMET)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_HELMET)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //MekaSuit Bodyarmor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_BODYARMOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_CHESTPLATE)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //MekaSuit Pants
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_PANTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_LEGGINGS)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //MekaSuit Boots
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MEKASUIT_BOOTS)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.CIRCUIT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.PLASTIC, Pattern.CONSTANT, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.ENERGY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, Items.NETHERITE_BOOTS)
              .key(Pattern.ENERGY, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.COMBAT)
              .save(output);
        //SPS Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SPS_CASING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).key(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_PLUTONIUM)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //SPS Port
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SPS_PORT)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, MekanismBlocks.SPS_CASING)
              .save(output);
        //Supercharged Coil
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.SUPERCHARGED_COIL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of('c', 'c', 'c'),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.CONSTANT, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.ALLOY, Pattern.ALLOY, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER)
              .key('c', this.items, Tags.Items.INGOTS_COPPER)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Nutritional Liquifier
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.NUTRITIONAL_LIQUIFIER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.BOWL)
              .save(output);
        //Pigment Extractor
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PIGMENT_EXTRACTOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.REDSTONE, Pattern.CIRCUIT, Pattern.REDSTONE))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.CONSTANT, Items.FLINT)
              .save(output);
        //Pigment Mixer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PIGMENT_MIXER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_REINFORCED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_ROD)
              .save(output);
        //Painting Machine
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.PAINTING_MACHINE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.DYE_BASE)
              .save(output);
        //Modification Station
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.MODIFICATION_STATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PLASTIC, Pattern.WOOD, Pattern.PLASTIC),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.STEEL_CASING, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.PLASTIC, Pattern.ALLOY, Pattern.PLASTIC))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.WOOD, this.items, Tags.Items.CHESTS_WOODEN)
              .key(Pattern.PLASTIC, MekanismItems.HDPE_SHEET)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Antiprotonic Nucleosynthesizer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.STEEL_CASING, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT))
              ).key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.CONSTANT, MekanismItems.ATOMIC_ALLOY)
              .save(output);
        //Radioactive Waste Barrel
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.RADIOACTIVE_WASTE_BARREL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.EMPTY, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, leadIngot(this.items))
              .save(output);
        //Dimensional Stabilizer
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismBlocks.DIMENSIONAL_STABILIZER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.ALLOY, DIAMOND_CHAR, Pattern.ALLOY),
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT))
              ).key(Pattern.INGOT, this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ATOMIC)
              .key(DIAMOND_CHAR, this.items, Tags.Items.STORAGE_BLOCKS_DIAMOND)
              .save(output);
    }

    private void addGearModuleRecipes() {
        //Module Base
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_BASE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET),
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.NUGGET, Pattern.INGOT, Pattern.NUGGET))
              ).key(Pattern.INGOT, tinIngot(this.items))
              .key(Pattern.NUGGET, this.items, MekanismTags.Items.NUGGETS_BRONZE)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_SHEET)
              .save(output);
        //Jetpack Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_JETPACK)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Ingredient.of(MekanismItems.JETPACK, MekanismItems.ARMORED_JETPACK))
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Electrolytic Breathing Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ELECTROLYTIC_BREATHING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.ELECTROLYTIC_CORE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Dosimeter Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_DOSIMETER)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.DOSIMETER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Geiger Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GEIGER)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.GEIGER_COUNTER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Energy Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ENERGY)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_CELL)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Laser Dissipation Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_LASER_DISSIPATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER_AMPLIFIER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Radiation Shielding Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_RADIATION_SHIELDING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(PrimaryResource.LEAD))
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Color Modulation Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_COLOR_MODULATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(MIXING_CHAR, Pattern.CONSTANT, MIXING_CHAR),
                    TripleLine.of(Pattern.OTHER, Pattern.PREVIOUS, Pattern.OTHER),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(MIXING_CHAR, MekanismBlocks.PIGMENT_MIXER)
              .key(Pattern.OTHER, MekanismBlocks.PAINTING_MACHINE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.LASER)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Charge Distribution Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_CHARGE_DISTRIBUTION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismBlocks.BASIC_INDUCTION_PROVIDER)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Teleportation Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_TELEPORTATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.TELEPORTATION_CORE)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .save(output);
        //Nutritional Injection Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_NUTRITIONAL_INJECTION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.CANTEEN)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Silk Touch Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_SILK_TOUCH)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.DIAMOND, Pattern.PREVIOUS, Pattern.DIAMOND),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.DIAMOND, Items.DIAMOND_PICKAXE)
              .save(output);
        //Fortune Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_FORTUNE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.DIAMOND, Pattern.PREVIOUS, Pattern.DIAMOND),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, this.items, MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.DIAMOND, this.items, Tags.Items.STORAGE_BLOCKS_DIAMOND)
              .save(output);
        //Blasting Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_BLASTING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.TNT)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .save(output);
        //Excavation Escalation Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_EXCAVATION_ESCALATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_PICKAXE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Attack Amplification Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ATTACK_AMPLIFICATION)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_SWORD)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Farming Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_FARMING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_HOE)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Shearing Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_SHEARING)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ADVANCED)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.SHEARS)
              .key(Pattern.HDPE_CHAR, MekanismItems.HDPE_SHEET)
              .save(output);
        //Vein Mining Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_VEIN_MINING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of('x', Pattern.PREVIOUS, 's'),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.DIAMOND_PICKAXE)
              .key('x', Items.DIAMOND_AXE)
              .key('s', Items.DIAMOND_SHOVEL)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Vision Enhancement Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_VISION_ENHANCEMENT)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.EMERALD)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Inhalation Purification Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_INHALATION_PURIFICATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, 'o', Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.HAZMAT_MASK)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key('o', MekanismItems.SCUBA_MASK)
              .save(output);
        //Magnetic Attraction Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_MAGNETIC_ATTRACTION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.PREVIOUS, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.IRON_BARS)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Frost Walker Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_FROST_WALKER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismFluids.HYDROGEN.getBucket())
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Soul Speed Module
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_SOUL_SURFER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.BLOCK, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.BLOCK, this.items, ItemTags.SOUL_FIRE_BASE_BLOCKS)
              .key(Pattern.CONSTANT, Ingredient.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS))
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Locomotive Boosting Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_LOCOMOTIVE_BOOSTING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.DIAMOND_LEGGINGS)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Hydraulic Propulsion Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_HYDRAULIC_PROPULSION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Ingredient.of(MekanismItems.FREE_RUNNERS, MekanismItems.ARMORED_FREE_RUNNERS))
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Gyroscopic Stabilization Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GYROSCOPIC_STABILIZATION)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.CONSTANT, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.OBSIDIAN)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Hydrostatic Repulsion Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_HYDROSTATIC_REPULSOR)
              .pattern(BASIC_MODULE)
              .key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, Items.LIGHTNING_ROD)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Motorized Servo Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_MOTORIZED_SERVO)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CIRCUIT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.PREVIOUS, Pattern.CONSTANT),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.CONSTANT, Items.BLUE_ICE)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .save(output);
        //Gravitational Modulating Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_GRAVITATIONAL_MODULATING)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ENERGY, Pattern.PREVIOUS, Pattern.ENERGY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.HDPE_CHAR, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, this.items, Tags.Items.NETHER_STARS)
              .key(Pattern.ENERGY, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .save(output);
        //Elytra Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULE_ELYTRA)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.ALLOY, Pattern.PREVIOUS, Pattern.ALLOY),
                    TripleLine.of(Pattern.HDPE_CHAR, Pattern.NUGGET, Pattern.HDPE_CHAR))
              ).key(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, MekanismItems.HDPE_REINFORCED_ELYTRA)
              .key(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .key(Pattern.NUGGET, this.items, MekanismTags.Items.PELLETS_ANTIMATTER)
              .save(output);
    }

    private void addLateGameRecipes() {
        String basePath = "processing/lategame/";

        //plutonium
        ChemicalToChemicalRecipeBuilder.centrifuging(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.NUCLEAR_WASTE, 10),
              MekanismChemicals.PLUTONIUM.asTemplate(1)
        ).save(output, Mekanism.rl(basePath + "plutonium"));
        //polonium
        ChemicalToChemicalRecipeBuilder.activating(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.NUCLEAR_WASTE, 10),
              MekanismChemicals.POLONIUM.asTemplate(1)
        ).save(output, Mekanism.rl(basePath + "polonium"));

        //plutonium pellet
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_FLUORITE),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.PLUTONIUM, 1_000),
              100,
              MekanismItems.PLUTONIUM_PELLET.asTemplate(),
              MekanismChemicals.SPENT_NUCLEAR_WASTE.asTemplate(1_000)
        ).save(output, Mekanism.rl(basePath + "plutonium_pellet/from_reaction"));
        //polonium pellet
        PressurizedReactionRecipeBuilder.reaction(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_FLUORITE),
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 1_000),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.POLONIUM, 1_000),
              100,
              MekanismItems.POLONIUM_PELLET.asTemplate(),
              MekanismChemicals.SPENT_NUCLEAR_WASTE.asTemplate(1_000)
        ).save(output, Mekanism.rl(basePath + "polonium_pellet/from_reaction"));

        //antimatter pellet
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.ANTIMATTER, 1_000),
              MekanismItems.ANTIMATTER_PELLET.asTemplate()
        ).save(output, Mekanism.rl(basePath + "antimatter_pellet/from_gas"));

        //back to antimatter
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.PELLETS_ANTIMATTER),
              MekanismChemicals.ANTIMATTER.asTemplate(1_000)
        ).save(output, Mekanism.rl(basePath + "antimatter/from_pellet"));
    }
}