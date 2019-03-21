package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnergizedSmelter;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.chemical.GuiChemicalCrystallizer;
import mekanism.client.gui.chemical.GuiChemicalDissolutionChamber;
import mekanism.client.gui.chemical.GuiChemicalInfuser;
import mekanism.client.gui.chemical.GuiChemicalInjectionChamber;
import mekanism.client.gui.chemical.GuiChemicalOxidizer;
import mekanism.client.gui.chemical.GuiChemicalWasher;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.client.jei.machine.ChanceMachineRecipeWrapper;
import mekanism.client.jei.machine.DoubleMachineRecipeWrapper;
import mekanism.client.jei.machine.MachineRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeWrapper;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.client.jei.machine.other.PRCRecipeWrapper;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeWrapper;
import mekanism.client.jei.machine.other.SolarNeutronRecipeWrapper;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeWrapper;
import mekanism.common.Tier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CombinerRecipe;
import mekanism.common.recipe.machines.CrusherRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.EnrichmentRecipe;
import mekanism.common.recipe.machines.InjectionRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.machines.OsmiumCompressorRecipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.machines.PurificationRecipe;
import mekanism.common.recipe.machines.SawmillRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.common.Loader;

public class RecipeRegistryHelper {

    public static void registerEnrichmentChamber(IModRegistry registry) {
        if (!MachineType.ENRICHMENT_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.ENRICHMENT_CHAMBER, EnrichmentRecipe.class, MachineRecipeWrapper::new);
        registry.addRecipeClickArea(GuiEnrichmentChamber.class, 79, 40, 24, 7,
              Recipe.ENRICHMENT_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.ENRICHMENT_CHAMBER, Recipe.ENRICHMENT_CHAMBER);
    }

    public static void registerCrusher(IModRegistry registry) {
        if (!MachineType.CRUSHER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CRUSHER, CrusherRecipe.class, MachineRecipeWrapper::new);
        registry.addRecipeClickArea(GuiCrusher.class, 79, 40, 24, 7, Recipe.CRUSHER.getJEICategory());
        registerRecipeItem(registry, MachineType.CRUSHER, Recipe.CRUSHER);
    }

    public static void registerCombiner(IModRegistry registry) {
        if (!MachineType.COMBINER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.COMBINER, CombinerRecipe.class, DoubleMachineRecipeWrapper::new);
        registry.addRecipeClickArea(GuiCombiner.class, 79, 40, 24, 7, Recipe.COMBINER.getJEICategory());
        registerRecipeItem(registry, MachineType.COMBINER, Recipe.COMBINER);
    }

    public static void registerPurification(IModRegistry registry) {
        if (!MachineType.PURIFICATION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PURIFICATION_CHAMBER, PurificationRecipe.class, AdvancedMachineRecipeWrapper::new);
        registry.addRecipeClickArea(GuiPurificationChamber.class, 79, 40, 24, 7,
              Recipe.PURIFICATION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.PURIFICATION_CHAMBER, Recipe.PURIFICATION_CHAMBER);
    }

    public static void registerCompressor(IModRegistry registry) {
        if (!MachineType.OSMIUM_COMPRESSOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.OSMIUM_COMPRESSOR, OsmiumCompressorRecipe.class, AdvancedMachineRecipeWrapper::new);
        registry
              .addRecipeClickArea(GuiOsmiumCompressor.class, 79, 40, 24, 7, Recipe.OSMIUM_COMPRESSOR.getJEICategory());
        registerRecipeItem(registry, MachineType.OSMIUM_COMPRESSOR, Recipe.OSMIUM_COMPRESSOR);
    }

    public static void registerInjection(IModRegistry registry) {
        if (!MachineType.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, InjectionRecipe.class,
              AdvancedMachineRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalInjectionChamber.class, 79, 40, 24, 7,
              Recipe.CHEMICAL_INJECTION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_INJECTION_CHAMBER, Recipe.CHEMICAL_INJECTION_CHAMBER);
    }

    public static void registerSawmill(IModRegistry registry) {
        if (!MachineType.PRECISION_SAWMILL.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PRECISION_SAWMILL, SawmillRecipe.class, ChanceMachineRecipeWrapper::new);
        registry
              .addRecipeClickArea(GuiPrecisionSawmill.class, 79, 40, 24, 7, Recipe.PRECISION_SAWMILL.getJEICategory());
        registerRecipeItem(registry, MachineType.PRECISION_SAWMILL, Recipe.PRECISION_SAWMILL);
    }

    public static void registerMetallurgicInfuser(IModRegistry registry) {
        if (!MachineType.METALLURGIC_INFUSER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.METALLURGIC_INFUSER, MetallurgicInfuserRecipe.class,
              MetallurgicInfuserRecipeWrapper::new);
        registry
              .addRecipeClickArea(GuiMetallurgicInfuser.class, 72, 47, 32, 8,
                    Recipe.METALLURGIC_INFUSER.getJEICategory());
        registerRecipeItem(registry, MachineType.METALLURGIC_INFUSER, Recipe.METALLURGIC_INFUSER);
    }

    public static void registerCrystallizer(IModRegistry registry) {
        if (!MachineType.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, CrystallizerRecipe.class,
              ChemicalCrystallizerRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalCrystallizer.class, 53, 62, 48, 8,
              Recipe.CHEMICAL_CRYSTALLIZER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_CRYSTALLIZER, Recipe.CHEMICAL_CRYSTALLIZER);
    }

    public static void registerDissolution(IModRegistry registry) {
        if (!MachineType.CHEMICAL_DISSOLUTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, DissolutionRecipe.class,
              ChemicalDissolutionChamberRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalDissolutionChamber.class, 64, 40, 48, 8,
              Recipe.CHEMICAL_DISSOLUTION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_DISSOLUTION_CHAMBER, Recipe.CHEMICAL_DISSOLUTION_CHAMBER);
    }

    public static void registerChemicalInfuser(IModRegistry registry) {
        if (!MachineType.CHEMICAL_INFUSER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_INFUSER, ChemicalInfuserRecipe.class, ChemicalInfuserRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalInfuser.class, 47, 39, 28, 8, Recipe.CHEMICAL_INFUSER.getJEICategory());
        registry.addRecipeClickArea(GuiChemicalInfuser.class, 101, 39, 28, 8, Recipe.CHEMICAL_INFUSER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_INFUSER, Recipe.CHEMICAL_INFUSER);
    }

    public static void registerOxidizer(IModRegistry registry) {
        if (!MachineType.CHEMICAL_OXIDIZER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, OxidationRecipe.class, ChemicalOxidizerRecipeWrapper::new);
        registry
              .addRecipeClickArea(GuiChemicalOxidizer.class, 64, 40, 48, 8, Recipe.CHEMICAL_OXIDIZER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_OXIDIZER, Recipe.CHEMICAL_OXIDIZER);
    }

    public static void registerWasher(IModRegistry registry) {
        if (!MachineType.CHEMICAL_WASHER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_WASHER, WasherRecipe.class, ChemicalWasherRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalWasher.class, 61, 39, 55, 8, Recipe.CHEMICAL_WASHER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_WASHER, Recipe.CHEMICAL_WASHER);
    }

    public static void registerNeutronActivator(IModRegistry registry) {
        if (!MachineType.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, SolarNeutronRecipe.class, SolarNeutronRecipeWrapper::new);
        registry.addRecipeClickArea(GuiSolarNeutronActivator.class, 64, 39, 48, 8,
              Recipe.SOLAR_NEUTRON_ACTIVATOR.getJEICategory());
        registerRecipeItem(registry, MachineType.SOLAR_NEUTRON_ACTIVATOR, Recipe.SOLAR_NEUTRON_ACTIVATOR);
    }

    public static void registerSeparator(IModRegistry registry) {
        if (!MachineType.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, SeparatorRecipe.class,
              ElectrolyticSeparatorRecipeWrapper::new);

        registry.addRecipeClickArea(GuiElectrolyticSeparator.class, 80, 30, 16, 6,
              Recipe.ELECTROLYTIC_SEPARATOR.getJEICategory());
        registerRecipeItem(registry, MachineType.ELECTROLYTIC_SEPARATOR, Recipe.ELECTROLYTIC_SEPARATOR);
    }

    public static void registerEvaporationPlant(IModRegistry registry) {
        addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, ThermalEvaporationRecipe.class,
              ThermalEvaporationRecipeWrapper::new);

        registry.addRecipeClickArea(GuiThermalEvaporationController.class, 49, 20, 78, 38,
              Recipe.THERMAL_EVAPORATION_PLANT.getJEICategory());

        registry.addRecipeCatalyst(BasicBlockType.THERMAL_EVAPORATION_CONTROLLER.getStack(1),
              Recipe.THERMAL_EVAPORATION_PLANT.getJEICategory());
    }

    public static void registerReactionChamber(IModRegistry registry) {
        if (!MachineType.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, PressurizedRecipe.class, PRCRecipeWrapper::new);

        registry.addRecipeClickArea(GuiPRC.class, 75, 37, 36, 10, Recipe.PRESSURIZED_REACTION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.PRESSURIZED_REACTION_CHAMBER, Recipe.PRESSURIZED_REACTION_CHAMBER);
    }

    public static void registerCondensentrator(IModRegistry registry) {
        if (!MachineType.ROTARY_CONDENSENTRATOR.isEnabled()) {
            return;
        }
        List<RotaryCondensentratorRecipeWrapper> condensentratorRecipes = new ArrayList<>();
        List<RotaryCondensentratorRecipeWrapper> decondensentratorRecipes = new ArrayList<>();

        for (Gas gas : GasRegistry.getRegisteredGasses()) {
            if (gas.hasFluid()) {
                condensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, true));
                decondensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, false));
            }
        }
        String condensentrating = "mekanism.rotary_condensentrator_condensentrating";
        String decondensentrating = "mekanism.rotary_condensentrator_decondensentrating";
        registry.addRecipes(condensentratorRecipes, condensentrating);
        registry.addRecipes(decondensentratorRecipes, decondensentrating);
        registry
              .addRecipeClickArea(GuiRotaryCondensentrator.class, 64, 39, 48, 8, condensentrating, decondensentrating);
        registry.addRecipeCatalyst(MachineType.ROTARY_CONDENSENTRATOR.getStack(), condensentrating, decondensentrating);
    }

    public static void registerSmelter(IModRegistry registry) {
        if (!MachineType.ENERGIZED_SMELTER.isEnabled()) {
            return;
        }
        registry.handleRecipes(SmeltingRecipe.class, MachineRecipeWrapper::new,
              Recipe.ENERGIZED_SMELTER.getJEICategory());

        boolean crafttweakerLoaded = Loader.isModLoaded("crafttweaker");

        if (crafttweakerLoaded && EnergizedSmelter.hasRemovedRecipe()) // Removed / Removed + Added
        {
            // Add all recipes
            Collection<SmeltingRecipe> recipeList = Recipe.ENERGIZED_SMELTER.get().values();
            registry.addRecipes(recipeList.stream().map(MachineRecipeWrapper::new).collect(Collectors.toList()),
                  Recipe.ENERGIZED_SMELTER.getJEICategory());

            registry
                  .addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7,
                        Recipe.ENERGIZED_SMELTER.getJEICategory());
        } else if (crafttweakerLoaded && EnergizedSmelter.hasAddedRecipe()) // Added but not removed
        {
            // Only add added recipes
            Map<ItemStackInput, SmeltingRecipe> smeltingRecipes = Recipe.ENERGIZED_SMELTER.get();
            List<MachineRecipeWrapper> smeltingWrapper = smeltingRecipes.entrySet().stream()
                  .filter(entry -> !FurnaceRecipes.instance().getSmeltingList().keySet()
                        .contains(entry.getKey().ingredient)).map(entry -> new MachineRecipeWrapper(entry.getValue()))
                  .collect(Collectors.toList());
            registry.addRecipes(smeltingWrapper, Recipe.ENERGIZED_SMELTER.getJEICategory());

            registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING,
                  Recipe.ENERGIZED_SMELTER.getJEICategory());
            registerVanillaSmeltingRecipeCatalyst(registry);
        } else {
            //Only use furnace list, so no extra registration.
            registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING);
            registerVanillaSmeltingRecipeCatalyst(registry);
        }
        registerRecipeItem(registry, MachineType.ENERGIZED_SMELTER, Recipe.ENERGIZED_SMELTER);
    }


    public static void registerFormulaicAssemblicator(IModRegistry registry) {
        if (!MachineType.FORMULAIC_ASSEMBLICATOR.isEnabled()) {
            return;
        }
        registry.addRecipeCatalyst(MachineType.FORMULAIC_ASSEMBLICATOR.getStack(), VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry()
              .addRecipeTransferHandler(ContainerFormulaicAssemblicator.class, VanillaRecipeCategoryUid.CRAFTING,
                    20, 9, 35, 36);
    }

    private static void registerVanillaSmeltingRecipeCatalyst(IModRegistry registry) {
        registry.addRecipeCatalyst(MachineType.ENERGIZED_SMELTER.getStack(), VanillaRecipeCategoryUid.SMELTING);
        for (Tier.FactoryTier tier : Tier.FactoryTier.values()) {
            if (tier.machineType.isEnabled()) {
                registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, RecipeType.SMELTING),
                      VanillaRecipeCategoryUid.SMELTING);
            }
        }
    }

    private static <T> void addRecipes(IModRegistry registry, Recipe type, Class<T> recipeClass,
          IRecipeWrapperFactory<T> factory) {
        String recipeCategoryUid = type.getJEICategory();
        registry.handleRecipes(recipeClass, factory, recipeCategoryUid);
        Collection<T> recipeList = type.get().values();
        registry.addRecipes(recipeList.stream().map(factory::getRecipeWrapper).collect(Collectors.toList()),
              recipeCategoryUid);
    }

    private static void registerRecipeItem(IModRegistry registry, MachineType type, Recipe recipe) {
        registry.addRecipeCatalyst(type.getStack(), recipe.getJEICategory());
        RecipeType factoryType = null;
        for (RecipeType t : RecipeType.values()) {
            if (t.getType() == type) {
                factoryType = t;
                break;
            }
        }
        if (factoryType != null) {
            for (Tier.FactoryTier tier : Tier.FactoryTier.values()) {
                if (tier.machineType.isEnabled()) {
                    registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, factoryType), recipe.getJEICategory());
                }
            }
        }
    }
}