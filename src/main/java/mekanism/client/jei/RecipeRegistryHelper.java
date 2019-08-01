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
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.base.FactoryType;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.integration.crafttweaker.handlers.EnergizedSmelter;
import mekanism.common.inventory.container.ContainerFormulaicAssemblicator;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.SmeltingRecipe;
import mekanism.common.recipe.outputs.MachineOutput;
import mekanism.common.tier.FactoryTier;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.block.Block;
import net.minecraft.item.crafting.FurnaceRecipes;

public class RecipeRegistryHelper {

    private static boolean registerPrecheck(IModRegistry registry, MekanismBlock mekanismBlock) {
        Block block = mekanismBlock.getBlock();
        if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
            return false;
        }
        registerRecipeItem(registry, mekanismBlock);
        return true;
    }

    public static void registerEnrichmentChamber(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.ENRICHMENT_CHAMBER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.ENRICHMENT_CHAMBER, MachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiEnrichmentChamber.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerCrusher(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CRUSHER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CRUSHER, MachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiCrusher.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerCombiner(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.COMBINER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.COMBINER, DoubleMachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiCombiner.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerPurification(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.PURIFICATION_CHAMBER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.PURIFICATION_CHAMBER, AdvancedMachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiPurificationChamber.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerCompressor(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.OSMIUM_COMPRESSOR;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.OSMIUM_COMPRESSOR, AdvancedMachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiOsmiumCompressor.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerInjection(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_INJECTION_CHAMBER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, AdvancedMachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalInjectionChamber.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerSawmill(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.PRECISION_SAWMILL;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.PRECISION_SAWMILL, ChanceMachineRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiPrecisionSawmill.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
        }
    }

    public static void registerMetallurgicInfuser(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.METALLURGIC_INFUSER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.METALLURGIC_INFUSER, MetallurgicInfuserRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiMetallurgicInfuser.class, 72, 47, 32, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerCrystallizer(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_CRYSTALLIZER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalCrystallizer.class, 53, 62, 48, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerDissolution(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, ChemicalDissolutionChamberRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalDissolutionChamber.class, 64, 40, 48, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerChemicalInfuser(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_INFUSER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_INFUSER, ChemicalInfuserRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalInfuser.class, 47, 39, 28, 8, mekanismBlock.getJEICategory());
            registry.addRecipeClickArea(GuiChemicalInfuser.class, 101, 39, 28, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerOxidizer(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_OXIDIZER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, ChemicalOxidizerRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalOxidizer.class, 64, 40, 48, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerWasher(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.CHEMICAL_WASHER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.CHEMICAL_WASHER, ChemicalWasherRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiChemicalWasher.class, 61, 39, 55, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerNeutronActivator(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.SOLAR_NEUTRON_ACTIVATOR;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, SolarNeutronRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiSolarNeutronActivator.class, 64, 39, 48, 8, mekanismBlock.getJEICategory());
        }
    }

    public static void registerSeparator(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.ELECTROLYTIC_SEPARATOR;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, ElectrolyticSeparatorRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiElectrolyticSeparator.class, 80, 30, 16, 6, mekanismBlock.getJEICategory());
        }
    }

    public static void registerEvaporationPlant(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.THERMAL_EVAPORATION_CONTROLLER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, ThermalEvaporationRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiThermalEvaporationController.class, 49, 20, 78, 38, mekanismBlock.getJEICategory());
        }
    }

    public static void registerReactionChamber(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.PRESSURIZED_REACTION_CHAMBER;
        if (registerPrecheck(registry, mekanismBlock)) {
            addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, PRCRecipeWrapper::new, mekanismBlock);
            registry.addRecipeClickArea(GuiPRC.class, 75, 37, 36, 10, mekanismBlock.getJEICategory());
        }
    }

    public static void registerCondensentrator(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.ROTARY_CONDENSENTRATOR;
        Block block = mekanismBlock.getBlock();
        if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
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
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), condensentrating, decondensentrating);
    }

    public static void registerSmelter(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.ENERGIZED_SMELTER;
        if (registerPrecheck(registry, mekanismBlock)) {
            registry.handleRecipes(SmeltingRecipe.class, MachineRecipeWrapper::new, mekanismBlock.getJEICategory());
            if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasRemovedRecipe()) {// Removed / Removed + Added
                // Add all recipes
                Collection<SmeltingRecipe> recipeList = Recipe.ENERGIZED_SMELTER.get().values();
                registry.addRecipes(recipeList.stream().map(MachineRecipeWrapper::new).collect(Collectors.toList()), mekanismBlock.getJEICategory());
                registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, mekanismBlock.getJEICategory());
            } else if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasAddedRecipe()) {// Added but not removed
                // Only add added recipes
                Map<ItemStackInput, SmeltingRecipe> smeltingRecipes = Recipe.ENERGIZED_SMELTER.get();
                List<MachineRecipeWrapper> smeltingWrapper = smeltingRecipes.entrySet().stream().filter(entry ->
                      !FurnaceRecipes.instance().getSmeltingList().containsKey(entry.getKey().ingredient)).map(entry ->
                      new MachineRecipeWrapper<>(entry.getValue())).collect(Collectors.toList());
                registry.addRecipes(smeltingWrapper, mekanismBlock.getJEICategory());
                registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING, mekanismBlock.getJEICategory());
                //Vanilla catalyst
                registerRecipeItem(registry, MekanismBlock.ENERGIZED_SMELTER, VanillaRecipeCategoryUid.SMELTING);
            } else {
                //Only use furnace list, so no extra registration.
                registry.addRecipeClickArea(GuiEnergizedSmelter.class, 79, 40, 24, 7, VanillaRecipeCategoryUid.SMELTING);
                //Vanilla catalyst
                registerRecipeItem(registry, MekanismBlock.ENERGIZED_SMELTER, VanillaRecipeCategoryUid.SMELTING);
            }
        }
    }


    public static void registerFormulaicAssemblicator(IModRegistry registry) {
        MekanismBlock mekanismBlock = MekanismBlock.FORMULAIC_ASSEMBLICATOR;
        Block block = mekanismBlock.getBlock();
        if (block instanceof IBlockDisableable && !((IBlockDisableable) block).isEnabled()) {
            return;
        }
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), VanillaRecipeCategoryUid.CRAFTING);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(ContainerFormulaicAssemblicator.class, VanillaRecipeCategoryUid.CRAFTING, 20, 9, 35, 36);
    }

    private static <INPUT extends MachineInput<INPUT>, OUTPUT extends MachineOutput<OUTPUT>, RECIPE extends MachineRecipe<INPUT, OUTPUT, RECIPE>>
    void addRecipes(IModRegistry registry, Recipe<INPUT, OUTPUT, RECIPE> type, IRecipeWrapperFactory<RECIPE> factory, MekanismBlock mekanismBlock) {
        String recipeCategoryUid = mekanismBlock.getJEICategory();
        registry.handleRecipes(type.getRecipeClass(), factory, recipeCategoryUid);
        registry.addRecipes(type.get().values().stream().map(factory::getRecipeWrapper).collect(Collectors.toList()), recipeCategoryUid);
    }

    private static void registerRecipeItem(IModRegistry registry, MekanismBlock mekanismBlock) {
        registerRecipeItem(registry, mekanismBlock, mekanismBlock.getJEICategory());
    }

    private static void registerRecipeItem(IModRegistry registry, MekanismBlock mekanismBlock, String category) {
        registry.addRecipeCatalyst(mekanismBlock.getItemStack(), category);
        FactoryType factoryType = mekanismBlock.getFactoryType();
        if (factoryType != null) {
            FactoryTier.forEnabled(tier -> registry.addRecipeCatalyst(MekanismBlock.getFactory(tier, factoryType).getItemStack(), category));
        }
    }
}