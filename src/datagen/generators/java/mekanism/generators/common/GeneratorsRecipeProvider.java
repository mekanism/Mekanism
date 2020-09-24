package mekanism.generators.common;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.common.content.gear.Modules;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsGases;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
public class GeneratorsRecipeProvider extends BaseRecipeProvider {

    private static final char GLASS_CHAR = 'G';
    private static final char IRON_BARS_CHAR = 'B';
    private static final char BIO_FUEL_CHAR = 'B';
    private static final char FRAME_CHAR = 'F';
    private static final char ELECTROLYTIC_CORE_CHAR = 'C';
    private static final char COPPER_CHAR = 'C';
    private static final char FURNACE_CHAR = 'F';

    public GeneratorsRecipeProvider(DataGenerator gen) {
        super(gen, MekanismGenerators.MODID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        super.registerRecipes(consumer);
        addGeneratorRecipes(consumer);
        addFissionReactorRecipes(consumer);
        addFusionReactorRecipes(consumer);
        addTurbineRecipes(consumer);
        addChemicalInfuserRecipes(consumer);
        addElectrolyticSeparatorRecipes(consumer);
        addRotaryCondensentratorRecipes(consumer);
        addSolarNeutronActivatorRecipes(consumer);
        addGearModuleRecipes(consumer);
    }

    private void addElectrolyticSeparatorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "separator/";
        //Heavy water
        ElectrolysisRecipeBuilder.separating(
              FluidStackIngredient.from(MekanismTags.Fluids.HEAVY_WATER, 2),
              GeneratorsGases.DEUTERIUM.getStack(2),
              MekanismGases.OXYGEN.getStack(1)
        ).energyMultiplier(FloatingLong.createConst(2))
              .build(consumer, MekanismGenerators.rl(basePath + "heavy_water"));
    }

    private void addRotaryCondensentratorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(consumer, basePath, GeneratorsGases.DEUTERIUM, GeneratorsFluids.DEUTERIUM, GeneratorTags.Fluids.DEUTERIUM, GeneratorTags.Gases.DEUTERIUM);
        addRotaryCondensentratorRecipe(consumer, basePath, GeneratorsGases.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL, GeneratorTags.Fluids.FUSION_FUEL, GeneratorTags.Gases.FUSION_FUEL);
        addRotaryCondensentratorRecipe(consumer, basePath, GeneratorsGases.TRITIUM, GeneratorsFluids.TRITIUM, GeneratorTags.Fluids.TRITIUM, GeneratorTags.Gases.TRITIUM);
    }

    private void addRotaryCondensentratorRecipe(Consumer<IFinishedRecipe> consumer, String basePath, IGasProvider gas, IFluidProvider fluidOutput,
          ITag<Fluid> fluidInput, ITag<Gas> gasInput) {
        RotaryRecipeBuilder.rotary(
              FluidStackIngredient.from(fluidInput, 1),
              GasStackIngredient.from(gasInput, 1),
              gas.getStack(1),
              fluidOutput.getFluidStack(1)
        ).build(consumer, MekanismGenerators.rl(basePath + gas.getName()));
    }

    private void addChemicalInfuserRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "chemical_infusing/";
        //DT Fuel
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(GeneratorsGases.DEUTERIUM, 1),
              GasStackIngredient.from(GeneratorsGases.TRITIUM, 1),
              GeneratorsGases.FUSION_FUEL.getStack(1)
        ).build(consumer, MekanismGenerators.rl(basePath + "fusion_fuel"));
    }

    private void addSolarNeutronActivatorRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "activating/";
        GasToGasRecipeBuilder.activating(
              GasStackIngredient.from(MekanismGases.LITHIUM, 1),
              GeneratorsGases.TRITIUM.getStack(1)
        ).build(consumer, MekanismGenerators.rl(basePath + "tritium"));
    }

    private void addGeneratorRecipes(Consumer<IFinishedRecipe> consumer) {
        //Solar panel (item component)
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsItems.SOLAR_PANEL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR),
                    TripleLine.of(Pattern.REDSTONE, Pattern.ALLOY, Pattern.REDSTONE),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer);
        //Solar Generator
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.SOLAR_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ENERGY, Pattern.OSMIUM))
              ).key(Pattern.CONSTANT, GeneratorsItems.SOLAR_PANEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .build(consumer, MekanismGenerators.rl("generator/solar"));
        //Advanced Solar Generator
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).key(Pattern.PREVIOUS, GeneratorsBlocks.SOLAR_GENERATOR)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .build(consumer, MekanismGenerators.rl("generator/advanced_solar"));
        //Bio
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.BIO_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.ALLOY, Pattern.REDSTONE),
                    TripleLine.of(BIO_FUEL_CHAR, Pattern.CIRCUIT, BIO_FUEL_CHAR),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT))
              ).key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(BIO_FUEL_CHAR, MekanismTags.Items.FUELS_BIO)
              .build(consumer, MekanismGenerators.rl("generator/bio"));
        //Gas Burning
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.GAS_BURNING_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL_CASING, ELECTROLYTIC_CORE_CHAR, Pattern.STEEL_CASING),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM))
              ).key(ELECTROLYTIC_CORE_CHAR, MekanismItems.ELECTROLYTIC_CORE)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .build(consumer, MekanismGenerators.rl("generator/gas_burning"));
        //Heat
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.HEAT_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.WOOD, Pattern.OSMIUM, Pattern.WOOD),
                    TripleLine.of(COPPER_CHAR, FURNACE_CHAR, COPPER_CHAR))
              ).key(Pattern.WOOD, ItemTags.PLANKS)
              .key(Pattern.INGOT, Tags.Items.INGOTS_IRON)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(COPPER_CHAR, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.COPPER))
              .key(FURNACE_CHAR, Blocks.FURNACE)
              .build(consumer, MekanismGenerators.rl("generator/heat"));
        //Wind
        MekDataShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.WIND_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ENERGY, Pattern.CIRCUIT, Pattern.ENERGY))
              ).key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_BASIC)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .build(consumer, MekanismGenerators.rl("generator/wind"));
    }

    private void addFissionReactorRecipes(Consumer<IFinishedRecipe> consumer) {
        // Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FISSION_REACTOR_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .build(consumer, MekanismGenerators.rl("fission_reactor/casing"));
        // Port
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FISSION_REACTOR_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY),
                    TripleLine.of(FRAME_CHAR, Pattern.CIRCUIT, FRAME_CHAR),
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(FRAME_CHAR, GeneratorsBlocks.FISSION_REACTOR_CASING)
              .build(consumer, MekanismGenerators.rl("fission_reactor/port"));
        //Logic Adapter
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY),
                    TripleLine.of(Pattern.REDSTONE, FRAME_CHAR, Pattern.REDSTONE),
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY))
              ).key(FRAME_CHAR, GeneratorsBlocks.FISSION_REACTOR_CASING)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .build(consumer, MekanismGenerators.rl("fission_reactor/logic_adapter"));
        //Fission Fuel Assembly
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT))
              ).key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .build(consumer, MekanismGenerators.rl("fission_reactor/fuel_assembly"));
        //Control Rod Assembly
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ELITE)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer, MekanismGenerators.rl("fission_reactor/control_rod_assembly"));
    }

    private void addGearModuleRecipes(Consumer<IFinishedRecipe> consumer) {
        //Solar Recharging Unit
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.MODULES.get(Modules.SOLAR_RECHARGING_UNIT))
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CONSTANT, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.PREVIOUS, Pattern.PREVIOUS))
              ).key(Pattern.INGOT, MekanismTags.Items.ALLOYS_ELITE)
              .key(Pattern.ALLOY, MekanismItems.MODULE_BASE)
              .key(Pattern.CONSTANT, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR)
              .key(Pattern.PREVIOUS, MekanismItems.POLONIUM_PELLET)
              .build(consumer);
    }

    private void addFusionReactorRecipes(Consumer<IFinishedRecipe> consumer) {
        //Hohlraum
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD), 4),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.CARBON, 10),
              GeneratorsItems.HOHLRAUM.getItemStack()
        ).build(consumer);
        //Laser Focus Matrix
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.LASER_FOCUS_MATRIX, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.REDSTONE, GLASS_CHAR),
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY))
              ).key(GLASS_CHAR, GeneratorsBlocks.REACTOR_GLASS)
              .key(Pattern.REDSTONE, Tags.Items.STORAGE_BLOCKS_REDSTONE)
              .build(consumer);
        //Frame
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FUSION_REACTOR_FRAME, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY))
              ).key(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_ULTIMATE)
              .key(Pattern.CONSTANT, MekanismTags.Items.PELLETS_POLONIUM)
              .build(consumer, MekanismGenerators.rl("reactor/frame"));
        //Glass
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.REACTOR_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, GLASS_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(GLASS_CHAR, Tags.Items.GLASS)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD))
              .key(Pattern.STEEL, MekanismItems.ENRICHED_IRON)
              .build(consumer, MekanismGenerators.rl("reactor/glass"));
        //Port
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FUSION_REACTOR_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY),
                    TripleLine.of(FRAME_CHAR, Pattern.CIRCUIT, FRAME_CHAR),
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .build(consumer, MekanismGenerators.rl("reactor/port"));
        //Logic Adapter
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY),
                    TripleLine.of(Pattern.REDSTONE, FRAME_CHAR, Pattern.REDSTONE),
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY))
              ).key(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .key(Pattern.REDSTONE, Tags.Items.DUSTS_REDSTONE)
              .build(consumer, MekanismGenerators.rl("reactor/logic_adapter"));
        //Controller
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, GLASS_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(FRAME_CHAR, Pattern.TANK, FRAME_CHAR),
                    TripleLine.of(FRAME_CHAR, FRAME_CHAR, FRAME_CHAR))
              ).key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .key(GLASS_CHAR, Tags.Items.GLASS_PANES)
              .key(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .key(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .build(consumer, MekanismGenerators.rl("reactor/controller"));
    }

    private void addTurbineRecipes(Consumer<IFinishedRecipe> consumer) {
        //Electromagnetic Coil
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.ELECTROMAGNETIC_COIL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, Tags.Items.INGOTS_GOLD)
              .key(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .build(consumer);
        //Rotational Complex
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.ROTATIONAL_COMPLEX)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.ALLOY, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .build(consumer);
        //Saturating Condenser
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.SATURATING_CONDENSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.BUCKET, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.INGOT, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN))
              .key(Pattern.BUCKET, Items.BUCKET)
              .build(consumer);
        //Blade
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsItems.TURBINE_BLADE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, MekanismGenerators.rl("turbine/blade"));
        //Rotor
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.TURBINE_ROTOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL))
              ).key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .key(Pattern.ALLOY, MekanismTags.Items.ALLOYS_INFUSED)
              .build(consumer, MekanismGenerators.rl("turbine/rotor"));
        //Casing
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.TURBINE_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.OSMIUM, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).key(Pattern.OSMIUM, MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM))
              .key(Pattern.STEEL, MekanismTags.Items.INGOTS_STEEL)
              .build(consumer, MekanismGenerators.rl("turbine/casing"));
        //Valve
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.TURBINE_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, GeneratorsBlocks.TURBINE_CASING)
              .key(Pattern.CIRCUIT, MekanismTags.Items.CIRCUITS_ADVANCED)
              .build(consumer, MekanismGenerators.rl("turbine/valve"));
        //Vent
        ExtendedShapedRecipeBuilder.shapedRecipe(GeneratorsBlocks.TURBINE_VENT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, IRON_BARS_CHAR, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).key(Pattern.CONSTANT, GeneratorsBlocks.TURBINE_CASING)
              .key(IRON_BARS_CHAR, Blocks.IRON_BARS)
              .build(consumer, MekanismGenerators.rl("turbine/vent"));
    }
}