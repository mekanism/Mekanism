package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
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
import mekanism.client.jei.machine.FluidToFluidRecipeWrapper;
import mekanism.client.jei.machine.GasToGasRecipeWrapper;
import mekanism.client.jei.machine.ItemStackGasToGasRecipeWrapper;
import mekanism.client.jei.machine.ItemStackGasToItemStackRecipeWrapper;
import mekanism.client.jei.machine.ItemStackToGasRecipeWrapper;
import mekanism.client.jei.machine.ItemStackToItemStackRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeWrapper;
import mekanism.client.jei.machine.other.CombinerRecipeWrapper;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.client.jei.machine.other.PRCRecipeWrapper;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeWrapper;
import mekanism.client.jei.machine.other.SawmillRecipeWrapper;
import mekanism.common.Mekanism;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.tier.FactoryTier;
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class RecipeRegistryHelper {

    public static void registerEnrichmentChamber(IModRegistry registry) {
        if (!MachineType.ENRICHMENT_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.ENRICHMENT_CHAMBER, ItemStackToItemStackRecipeWrapper::new);
        registry.addRecipeClickArea(GuiEnrichmentChamber.class, 79, 40, 24, 7, Recipe.ENRICHMENT_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.ENRICHMENT_CHAMBER, Recipe.ENRICHMENT_CHAMBER);
    }

    public static void registerCrusher(IModRegistry registry) {
        if (!MachineType.CRUSHER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CRUSHER, ItemStackToItemStackRecipeWrapper::new);
        registry.addRecipeClickArea(GuiCrusher.class, 79, 40, 24, 7, Recipe.CRUSHER.getJEICategory());
        registerRecipeItem(registry, MachineType.CRUSHER, Recipe.CRUSHER);
    }

    public static void registerCombiner(IModRegistry registry) {
        if (!MachineType.COMBINER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.COMBINER, CombinerRecipeWrapper::new);
        registry.addRecipeClickArea(GuiCombiner.class, 79, 40, 24, 7, Recipe.COMBINER.getJEICategory());
        registerRecipeItem(registry, MachineType.COMBINER, Recipe.COMBINER);
    }

    public static void registerPurification(IModRegistry registry) {
        if (!MachineType.PURIFICATION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PURIFICATION_CHAMBER, ItemStackGasToItemStackRecipeWrapper::new);
        registry.addRecipeClickArea(GuiPurificationChamber.class, 79, 40, 24, 7, Recipe.PURIFICATION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.PURIFICATION_CHAMBER, Recipe.PURIFICATION_CHAMBER);
    }

    public static void registerCompressor(IModRegistry registry) {
        if (!MachineType.OSMIUM_COMPRESSOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.OSMIUM_COMPRESSOR, ItemStackGasToItemStackRecipeWrapper::new);
        registry.addRecipeClickArea(GuiOsmiumCompressor.class, 79, 40, 24, 7, Recipe.OSMIUM_COMPRESSOR.getJEICategory());
        registerRecipeItem(registry, MachineType.OSMIUM_COMPRESSOR, Recipe.OSMIUM_COMPRESSOR);
    }

    public static void registerInjection(IModRegistry registry) {
        if (!MachineType.CHEMICAL_INJECTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, ItemStackGasToItemStackRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalInjectionChamber.class, 79, 40, 24, 7, Recipe.CHEMICAL_INJECTION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_INJECTION_CHAMBER, Recipe.CHEMICAL_INJECTION_CHAMBER);
    }

    public static void registerSawmill(IModRegistry registry) {
        if (!MachineType.PRECISION_SAWMILL.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PRECISION_SAWMILL, SawmillRecipeWrapper::new);
        registry.addRecipeClickArea(GuiPrecisionSawmill.class, 79, 40, 24, 7, Recipe.PRECISION_SAWMILL.getJEICategory());
        registerRecipeItem(registry, MachineType.PRECISION_SAWMILL, Recipe.PRECISION_SAWMILL);
    }

    public static void registerMetallurgicInfuser(IModRegistry registry) {
        if (!MachineType.METALLURGIC_INFUSER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.METALLURGIC_INFUSER, MetallurgicInfuserRecipeWrapper::new);
        registry.addRecipeClickArea(GuiMetallurgicInfuser.class, 72, 47, 32, 8, Recipe.METALLURGIC_INFUSER.getJEICategory());
        registerRecipeItem(registry, MachineType.METALLURGIC_INFUSER, Recipe.METALLURGIC_INFUSER);
    }

    public static void registerCrystallizer(IModRegistry registry) {
        if (!MachineType.CHEMICAL_CRYSTALLIZER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalCrystallizer.class, 53, 62, 48, 8, Recipe.CHEMICAL_CRYSTALLIZER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_CRYSTALLIZER, Recipe.CHEMICAL_CRYSTALLIZER);
    }

    public static void registerDissolution(IModRegistry registry) {
        if (!MachineType.CHEMICAL_DISSOLUTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, ItemStackGasToGasRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalDissolutionChamber.class, 64, 40, 48, 8, Recipe.CHEMICAL_DISSOLUTION_CHAMBER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_DISSOLUTION_CHAMBER, Recipe.CHEMICAL_DISSOLUTION_CHAMBER);
    }

    public static void registerChemicalInfuser(IModRegistry registry) {
        if (!MachineType.CHEMICAL_INFUSER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_INFUSER, ChemicalInfuserRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalInfuser.class, 47, 39, 28, 8, Recipe.CHEMICAL_INFUSER.getJEICategory());
        registry.addRecipeClickArea(GuiChemicalInfuser.class, 101, 39, 28, 8, Recipe.CHEMICAL_INFUSER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_INFUSER, Recipe.CHEMICAL_INFUSER);
    }

    public static void registerOxidizer(IModRegistry registry) {
        if (!MachineType.CHEMICAL_OXIDIZER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, ItemStackToGasRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalOxidizer.class, 64, 40, 48, 8, Recipe.CHEMICAL_OXIDIZER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_OXIDIZER, Recipe.CHEMICAL_OXIDIZER);
    }

    public static void registerWasher(IModRegistry registry) {
        if (!MachineType.CHEMICAL_WASHER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.CHEMICAL_WASHER, ChemicalWasherRecipeWrapper::new);
        registry.addRecipeClickArea(GuiChemicalWasher.class, 61, 39, 55, 8, Recipe.CHEMICAL_WASHER.getJEICategory());
        registerRecipeItem(registry, MachineType.CHEMICAL_WASHER, Recipe.CHEMICAL_WASHER);
    }

    public static void registerNeutronActivator(IModRegistry registry) {
        if (!MachineType.SOLAR_NEUTRON_ACTIVATOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, GasToGasRecipeWrapper::new);
        registry.addRecipeClickArea(GuiSolarNeutronActivator.class, 64, 39, 48, 8, Recipe.SOLAR_NEUTRON_ACTIVATOR.getJEICategory());
        registerRecipeItem(registry, MachineType.SOLAR_NEUTRON_ACTIVATOR, Recipe.SOLAR_NEUTRON_ACTIVATOR);
    }

    public static void registerSeparator(IModRegistry registry) {
        if (!MachineType.ELECTROLYTIC_SEPARATOR.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, ElectrolyticSeparatorRecipeWrapper::new);
        registry.addRecipeClickArea(GuiElectrolyticSeparator.class, 80, 30, 16, 6, Recipe.ELECTROLYTIC_SEPARATOR.getJEICategory());
        registerRecipeItem(registry, MachineType.ELECTROLYTIC_SEPARATOR, Recipe.ELECTROLYTIC_SEPARATOR);
    }

    public static void registerEvaporationPlant(IModRegistry registry) {
        addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, FluidToFluidRecipeWrapper::new);
        registry.addRecipeClickArea(GuiThermalEvaporationController.class, 49, 20, 78, 38, Recipe.THERMAL_EVAPORATION_PLANT.getJEICategory());
        registry.addRecipeCatalyst(BasicBlockType.THERMAL_EVAPORATION_CONTROLLER.getStack(1), Recipe.THERMAL_EVAPORATION_PLANT.getJEICategory());
    }

    public static void registerReactionChamber(IModRegistry registry) {
        if (!MachineType.PRESSURIZED_REACTION_CHAMBER.isEnabled()) {
            return;
        }
        addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, PRCRecipeWrapper::new);
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
        registry.addRecipeClickArea(GuiRotaryCondensentrator.class, 64, 39, 48, 8, condensentrating, decondensentrating);
        registry.addRecipeCatalyst(MachineType.ROTARY_CONDENSENTRATOR.getStack(), condensentrating, decondensentrating);
    }

    public static void registerSmelter(IModRegistry registry) {
        if (!MachineType.ENERGIZED_SMELTER.isEnabled()) {
            return;
        }
        registry.handleRecipes(Recipe.ENERGIZED_SMELTER.getRecipeClass(), ItemStackToItemStackRecipeWrapper::new, Recipe.ENERGIZED_SMELTER.getJEICategory());
        if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasRemovedRecipe()) {// Removed / Removed + Added
            // Add all recipes
            List<ItemStackToItemStackRecipe> recipeList = Recipe.ENERGIZED_SMELTER.get();
            registry.addRecipes(recipeList.stream().map(ItemStackToItemStackRecipeWrapper::new).collect(Collectors.toList()),
                  Recipe.ENERGIZED_SMELTER.getJEICategory());

            registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, Recipe.ENERGIZED_SMELTER.getJEICategory());
        } else if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasAddedRecipe()) {// Added but not removed
            // Only add added recipes
            Map<ItemStack, ItemStack> smeltingList = FurnaceRecipes.instance().getSmeltingList();

            List<ItemStackToItemStackRecipe> smeltingRecipes = Recipe.ENERGIZED_SMELTER.get();
            List<ItemStackToItemStackRecipeWrapper> smeltingWrapper = new ArrayList<>();
            for (ItemStackToItemStackRecipe recipe : smeltingRecipes) {
                if (recipe.getInput().getRepresentations().stream().allMatch(smeltingList::containsKey)) {
                    //If it does not contain all inputs then we add it
                    //TODO: Decide if we should be handling it differently if only some of them match
                    smeltingWrapper.add(new ItemStackToItemStackRecipeWrapper(recipe));
                }
            }
            registry.addRecipes(smeltingWrapper, Recipe.ENERGIZED_SMELTER.getJEICategory());

            registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING, Recipe.ENERGIZED_SMELTER.getJEICategory());
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
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerFormulaicAssemblicator.class, VanillaRecipeCategoryUid.CRAFTING, 20, 9, 35, 36);
    }

    private static void registerVanillaSmeltingRecipeCatalyst(IModRegistry registry) {
        registry.addRecipeCatalyst(MachineType.ENERGIZED_SMELTER.getStack(), VanillaRecipeCategoryUid.SMELTING);
        FactoryTier.forEnabled(tier -> registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, RecipeType.SMELTING), VanillaRecipeCategoryUid.SMELTING));
    }

    private static <RECIPE extends IMekanismRecipe> void addRecipes(IModRegistry registry, Recipe<RECIPE> type, IRecipeWrapperFactory<RECIPE> factory) {
        String recipeCategoryUid = type.getJEICategory();
        //TODO: Is the handleRecipes being too broad given the more generic recipe types
        registry.handleRecipes(type.getRecipeClass(), factory, recipeCategoryUid);
        registry.addRecipes(type.get().stream().map(factory::getRecipeWrapper).collect(Collectors.toList()), recipeCategoryUid);
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
            RecipeType finalFactoryType = factoryType;
            FactoryTier.forEnabled(tier -> registry.addRecipeCatalyst(MekanismUtils.getFactory(tier, finalFactoryType), recipe.getJEICategory()));
        }
    }
}