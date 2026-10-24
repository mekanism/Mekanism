package mekanism.generators.common;

import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ElectrolysisRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.RotaryRecipeBuilder;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.MekDataShapedRecipeBuilder;
import mekanism.common.recipe.impl.MekanismRecipeProvider;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.recipe.pattern.RecipePattern;
import mekanism.common.recipe.pattern.RecipePattern.TripleLine;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsModules;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

public class GeneratorsRecipeProvider extends BaseRecipeProvider {

    private static final char GLASS_CHAR = 'G';
    private static final char BARS_CHAR = 'B';
    private static final char BIO_FUEL_CHAR = 'B';
    private static final char FRAME_CHAR = 'F';
    private static final char ELECTROLYTIC_CORE_CHAR = 'C';
    private static final char COPPER_CHAR = 'C';
    private static final char FURNACE_CHAR = 'F';

    public GeneratorsRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void addRecipes() {
        addGeneratorRecipes();
        addFissionReactorRecipes();
        addFusionReactorRecipes();
        addTurbineRecipes();
        addChemicalInfuserRecipes();
        addElectrolyticSeparatorRecipes();
        addRotaryCondensentratorRecipes();
        addSolarNeutronActivatorRecipes();
        addGearModuleRecipes();
    }

    private void addElectrolyticSeparatorRecipes() {
        String basePath = "separator/";
        //Heavy water
        ElectrolysisRecipeBuilder.separating(
                    IngredientCreatorAccess.fluid().from(this.fluids, MekanismTags.Fluids.HEAVY_WATER, 2),
                    chemicalTemplate(ChemicalIds.DEUTERIUM, 2),
                    chemicalTemplate(ChemicalIds.OXYGEN, 1)
              ).energyMultiplier(2)
              .unlockedBy("in_water", insideOf(Blocks.WATER))
              .save(output, MekanismGenerators.rl(basePath + "heavy_water"));
    }

    private void addRotaryCondensentratorRecipes() {
        String basePath = "rotary/";
        addRotaryCondensentratorRecipe(basePath, ChemicalIds.DEUTERIUM, GeneratorsFluids.DEUTERIUM, GeneratorTags.Fluids.DEUTERIUM, GeneratorTags.Chemicals.DEUTERIUM);
        addRotaryCondensentratorRecipe(basePath, ChemicalIds.FUSION_FUEL, GeneratorsFluids.FUSION_FUEL, GeneratorTags.Fluids.FUSION_FUEL, GeneratorTags.Chemicals.FUSION_FUEL);
        addRotaryCondensentratorRecipe(basePath, ChemicalIds.TRITIUM, GeneratorsFluids.TRITIUM, GeneratorTags.Fluids.TRITIUM, GeneratorTags.Chemicals.TRITIUM);
    }

    private void addRotaryCondensentratorRecipe(String basePath, ResourceKey<Chemical> chemicalOutput, Holder<Fluid> fluidOutput, TagKey<Fluid> fluidInput, TagKey<Chemical> chemicalInput) {
        RotaryRecipeBuilder.rotary(
              IngredientCreatorAccess.fluid().from(this.fluids, fluidInput, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, chemicalInput, 1),
              chemicalTemplate(chemicalOutput, 1),
              new FluidStackTemplate(fluidOutput, 1)
        ).unlockedBy("has_condensentrator", has(MekanismBlocks.ROTARY_CONDENSENTRATOR))
              .save(output, MekanismGenerators.rl(basePath + chemicalOutput.identifier().getPath()));
    }

    private void addChemicalInfuserRecipes() {
        String basePath = "chemical_infusing/";
        //DT Fuel
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.DEUTERIUM, 1),
              IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.TRITIUM, 1),
              chemicalTemplate(ChemicalIds.FUSION_FUEL, 2)
        ).unlockedBy("in_heavy_water", insideOf(MekanismFluids.HEAVY_WATER.getBlock()))
              .save(output, MekanismGenerators.rl(basePath + "fusion_fuel"));
    }

    private void addSolarNeutronActivatorRecipes() {
        String basePath = "activating/";
        ChemicalToChemicalRecipeBuilder.activating(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.LITHIUM, 1),
              chemicalTemplate(ChemicalIds.TRITIUM, 1)
        ).unlockedBy("has_lithium_dust", has(MekanismTags.Items.DUSTS_LITHIUM))
              .save(output, MekanismGenerators.rl(basePath + "tritium"));
    }

    private void addGeneratorRecipes() {
        //Solar panel (item component)
        ExtendedShapedRecipeBuilder.shaped(GeneratorsItems.SOLAR_PANEL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(GLASS_CHAR, GLASS_CHAR, GLASS_CHAR),
                    TripleLine.of(Pattern.REDSTONE, Pattern.ALLOY, Pattern.REDSTONE),
                    TripleLine.of(Pattern.OSMIUM, Pattern.OSMIUM, Pattern.OSMIUM))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_PANES)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .save(output);
        //Solar Generator
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.SOLAR_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CONSTANT, Pattern.CONSTANT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.INGOT, Pattern.ALLOY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ENERGY, Pattern.OSMIUM))
              ).define(Pattern.CONSTANT, GeneratorsItems.SOLAR_PANEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output, MekanismGenerators.rl("generator/solar"));
        //Advanced Solar Generator
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.PREVIOUS, Pattern.ALLOY, Pattern.PREVIOUS),
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT))
              ).define(Pattern.PREVIOUS, GeneratorsBlocks.SOLAR_GENERATOR)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(GeneratorsBlocks.SOLAR_GENERATOR))
              .save(output, MekanismGenerators.rl("generator/advanced_solar"));
        //Bio
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.BIO_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.REDSTONE, Pattern.ALLOY, Pattern.REDSTONE),
                    TripleLine.of(BIO_FUEL_CHAR, Pattern.CIRCUIT, BIO_FUEL_CHAR),
                    TripleLine.of(Pattern.INGOT, Pattern.ALLOY, Pattern.INGOT))
              ).define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(BIO_FUEL_CHAR, this.items, MekanismTags.Items.FUELS_BIO)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_BASIC))
              .save(output, MekanismGenerators.rl("generator/bio"));
        //Gas Burning
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.GAS_BURNING_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.STEEL_CASING, ELECTROLYTIC_CORE_CHAR, Pattern.STEEL_CASING),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM))
              ).define(ELECTROLYTIC_CORE_CHAR, MekanismItems.ELECTROLYTIC_CORE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_alloy", has(MekanismTags.Items.ALLOYS_INFUSED))
              .unlockedBy(hasCriterion(MekanismItems.ELECTROLYTIC_CORE))
              .save(output, MekanismGenerators.rl("generator/gas_burning"));
        //Heat
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.HEAT_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.INGOT, Pattern.INGOT),
                    TripleLine.of(Pattern.WOOD, Pattern.OSMIUM, Pattern.WOOD),
                    TripleLine.of(COPPER_CHAR, FURNACE_CHAR, COPPER_CHAR))
              ).define(Pattern.WOOD, this.items, ItemTags.PLANKS)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_IRON)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(COPPER_CHAR, this.items, Tags.Items.INGOTS_COPPER)
              .define(FURNACE_CHAR, this.items, Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_osmium_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .save(output, MekanismGenerators.rl("generator/heat"));
        //Wind
        MekDataShapedRecipeBuilder.shaped(GeneratorsBlocks.WIND_GENERATOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.OSMIUM, Pattern.EMPTY),
                    TripleLine.of(Pattern.OSMIUM, Pattern.ALLOY, Pattern.OSMIUM),
                    TripleLine.of(Pattern.ENERGY, Pattern.CIRCUIT, Pattern.ENERGY))
              ).define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_BASIC)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .define(Pattern.OSMIUM, osmiumIngot(this.items))
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output, MekanismGenerators.rl("generator/wind"));
    }

    private void addFissionReactorRecipes() {
        // Casing
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FISSION_REACTOR_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL_CASING, Pattern.INGOT),
                    TripleLine.of(Pattern.EMPTY, Pattern.INGOT, Pattern.EMPTY))
              ).define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.INGOT, leadIngot(this.items))
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output, MekanismGenerators.rl("fission_reactor/casing"));
        // Port
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FISSION_REACTOR_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY),
                    TripleLine.of(FRAME_CHAR, Pattern.CIRCUIT, FRAME_CHAR),
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(FRAME_CHAR, GeneratorsBlocks.FISSION_REACTOR_CASING)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(GeneratorsBlocks.FISSION_REACTOR_CASING))
              .save(output, MekanismGenerators.rl("fission_reactor/port"));
        //Logic Adapter
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FISSION_REACTOR_LOGIC_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY),
                    TripleLine.of(Pattern.REDSTONE, FRAME_CHAR, Pattern.REDSTONE),
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY))
              ).define(FRAME_CHAR, GeneratorsBlocks.FISSION_REACTOR_CASING)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .category(RecipeCategory.REDSTONE)
              .group(MekanismGenerators.rl("logic_adapter"))
              .unlockedBy("has_casing", has(GeneratorsBlocks.FISSION_REACTOR_CASING))
              .save(output, MekanismGenerators.rl("fission_reactor/logic_adapter"));
        //Fission Fuel Assembly
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FISSION_FUEL_ASSEMBLY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.TANK, Pattern.INGOT),
                    TripleLine.of(Pattern.INGOT, Pattern.STEEL, Pattern.INGOT))
              ).define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output, MekanismGenerators.rl("fission_reactor/fuel_assembly"));
        //Control Rod Assembly
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.CONTROL_ROD_ASSEMBLY)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.INGOT, Pattern.CIRCUIT, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ELITE)
              .define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_lead_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.LEAD)))
              .save(output, MekanismGenerators.rl("fission_reactor/control_rod_assembly"));
    }

    private void addGearModuleRecipes() {
        //Geothermal Generator Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(GeneratorsModules.GEOTHERMAL_GENERATOR_UNIT))
              .pattern(MekanismRecipeProvider.BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, GeneratorsBlocks.HEAT_GENERATOR)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, GeneratorsModules.GEOTHERMAL_GENERATOR_UNIT.getId());
        //Solar Recharging Unit
        ExtendedShapedRecipeBuilder.shaped(IModuleHelper.INSTANCE.asTemplate(GeneratorsModules.SOLAR_RECHARGING_UNIT))
              .pattern(MekanismRecipeProvider.BASIC_MODULE)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ELITE)
              .define(Pattern.PREVIOUS, MekanismItems.MODULE_BASE)
              .define(Pattern.CONSTANT, GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR)
              .define(Pattern.HDPE_CHAR, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .unlockedBy(hasCriterion(MekanismItems.MODULE_BASE))
              .save(output, GeneratorsModules.SOLAR_RECHARGING_UNIT.getId());
    }

    private void addFusionReactorRecipes() {
        TagKey<Item> goldDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.GOLD);
        //Hohlraum
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, goldDust, 4),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.CARBON, 10),
              false,
              GeneratorsItems.HOHLRAUM.asTemplate()
        ).unlockedBy("has_gold_dust", has(goldDust))
              .save(output);
        //Laser Focus Matrix
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.LASER_FOCUS_MATRIX, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY),
                    TripleLine.of(GLASS_CHAR, Pattern.REDSTONE, GLASS_CHAR),
                    TripleLine.of(Pattern.EMPTY, GLASS_CHAR, Pattern.EMPTY))
              ).define(GLASS_CHAR, GeneratorsBlocks.REACTOR_GLASS)
              .define(Pattern.REDSTONE, this.items, Tags.BlockItems.STORAGE_BLOCKS_REDSTONE)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_reactor_glass", has(GeneratorsBlocks.REACTOR_GLASS))
              .save(output);
        //Frame
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FUSION_REACTOR_FRAME, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.STEEL_CASING, Pattern.CONSTANT),
                    TripleLine.of(Pattern.ALLOY, Pattern.CONSTANT, Pattern.ALLOY))
              ).define(Pattern.STEEL_CASING, MekanismBlocks.STEEL_CASING)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_ULTIMATE)
              .define(Pattern.CONSTANT, this.items, MekanismTags.Items.PELLETS_POLONIUM)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_polonium", has(MekanismTags.Items.PELLETS_POLONIUM))
              .save(output, MekanismGenerators.rl("reactor/frame"));
        //Glass
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.REACTOR_GLASS, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, GLASS_CHAR, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_BLOCKS_CHEAP)
              .define(Pattern.INGOT, leadIngot(this.items))
              .define(Pattern.STEEL, MekanismItems.ENRICHED_IRON)
              .category(RecipeCategory.DECORATIONS)
              .unlockedBy("has_glass", has(Tags.BlockItems.GLASS_BLOCKS_CHEAP))
              .save(output, MekanismGenerators.rl("reactor/glass"));
        //Port
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FUSION_REACTOR_PORT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY),
                    TripleLine.of(FRAME_CHAR, Pattern.CIRCUIT, FRAME_CHAR),
                    TripleLine.of(Pattern.EMPTY, FRAME_CHAR, Pattern.EMPTY))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(GeneratorsBlocks.FUSION_REACTOR_FRAME))
              .save(output, MekanismGenerators.rl("reactor/port"));
        //Logic Adapter
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FUSION_REACTOR_LOGIC_ADAPTER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY),
                    TripleLine.of(Pattern.REDSTONE, FRAME_CHAR, Pattern.REDSTONE),
                    TripleLine.of(Pattern.EMPTY, Pattern.REDSTONE, Pattern.EMPTY))
              ).define(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .define(Pattern.REDSTONE, this.items, Tags.Items.DUSTS_REDSTONE)
              .category(RecipeCategory.REDSTONE)
              .group(MekanismGenerators.rl("logic_adapter"))
              .unlockedBy("has_casing", has(GeneratorsBlocks.FUSION_REACTOR_FRAME))
              .save(output, MekanismGenerators.rl("reactor/logic_adapter"));
        //Controller
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.FUSION_REACTOR_CONTROLLER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.CIRCUIT, GLASS_CHAR, Pattern.CIRCUIT),
                    TripleLine.of(FRAME_CHAR, Pattern.TANK, FRAME_CHAR),
                    TripleLine.of(FRAME_CHAR, FRAME_CHAR, FRAME_CHAR))
              ).define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ULTIMATE)
              .define(GLASS_CHAR, this.items, Tags.BlockItems.GLASS_PANES)
              .define(FRAME_CHAR, GeneratorsBlocks.FUSION_REACTOR_FRAME)
              .define(Pattern.TANK, MekanismBlocks.BASIC_CHEMICAL_TANK)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(GeneratorsBlocks.FUSION_REACTOR_FRAME))
              .save(output, MekanismGenerators.rl("reactor/controller"));
    }

    private void addTurbineRecipes() {
        //Electromagnetic Coil
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.ELECTROMAGNETIC_COIL)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.ENERGY, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, this.items, Tags.Items.INGOTS_GOLD)
              .define(Pattern.ENERGY, MekanismItems.ENERGY_TABLET)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .unlockedBy(hasCriterion(MekanismItems.ENERGY_TABLET))
              .save(output);
        //Rotational Complex
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.ROTATIONAL_COMPLEX)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.CIRCUIT, Pattern.ALLOY, Pattern.CIRCUIT),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_circuit", has(MekanismTags.Items.CIRCUITS_ADVANCED))
              .save(output);
        //Saturating Condenser
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.SATURATING_CONDENSER)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL),
                    TripleLine.of(Pattern.INGOT, Pattern.BUCKET, Pattern.INGOT),
                    TripleLine.of(Pattern.STEEL, Pattern.INGOT, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.INGOT, tinIngot(this.items))
              .define(Pattern.BUCKET, this.items, Tags.Items.BUCKETS_EMPTY)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output);
        //Blade
        ExtendedShapedRecipeBuilder.shaped(GeneratorsItems.TURBINE_BLADE)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .unlockedBy(hasCriterion(GeneratorsBlocks.TURBINE_ROTOR))
              .save(output, MekanismGenerators.rl("turbine/blade"));
        //Rotor
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.TURBINE_ROTOR)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL),
                    TripleLine.of(Pattern.STEEL, Pattern.ALLOY, Pattern.STEEL))
              ).define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .define(Pattern.ALLOY, this.items, MekanismTags.Items.ALLOYS_INFUSED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, MekanismGenerators.rl("turbine/rotor"));
        //Casing
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.TURBINE_CASING, 4)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY),
                    TripleLine.of(Pattern.STEEL, Pattern.OSMIUM, Pattern.STEEL),
                    TripleLine.of(Pattern.EMPTY, Pattern.STEEL, Pattern.EMPTY))
              ).define(Pattern.OSMIUM, osmiumIngot(this.items))
              .define(Pattern.STEEL, this.items, MekanismTags.Items.INGOTS_STEEL)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_osmium_ingot", has(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.OSMIUM)))
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, MekanismGenerators.rl("turbine/casing"));
        //Valve
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.TURBINE_VALVE, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, Pattern.CIRCUIT, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, GeneratorsBlocks.TURBINE_CASING)
              .define(Pattern.CIRCUIT, this.items, MekanismTags.Items.CIRCUITS_ADVANCED)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(GeneratorsBlocks.TURBINE_CASING))
              .save(output, MekanismGenerators.rl("turbine/valve"));
        //Vent
        ExtendedShapedRecipeBuilder.shaped(GeneratorsBlocks.TURBINE_VENT, 2)
              .pattern(RecipePattern.createPattern(
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY),
                    TripleLine.of(Pattern.CONSTANT, BARS_CHAR, Pattern.CONSTANT),
                    TripleLine.of(Pattern.EMPTY, Pattern.CONSTANT, Pattern.EMPTY))
              ).define(Pattern.CONSTANT, GeneratorsBlocks.TURBINE_CASING)
              .define(BARS_CHAR, this.items, Tags.BlockItems.BARS)
              .category(RecipeCategory.BUILDING_BLOCKS)
              .unlockedBy("has_casing", has(GeneratorsBlocks.TURBINE_CASING))
              .save(output, MekanismGenerators.rl("turbine/vent"));
    }
}