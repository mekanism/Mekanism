package mekanism.generators.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.gear.ModuleData;
import mekanism.client.integration.emi.BaseEmiDefaults;
import mekanism.common.util.RegistryUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mekanism.generators.common.registries.GeneratorsModules;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GeneratorsEmiDefaults extends BaseEmiDefaults {

    public GeneratorsEmiDefaults(PackOutput output, CompletableFuture<HolderLookup.Provider> reloadableLookupProvider) {
        super(output, reloadableLookupProvider, MekanismGenerators.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider reloadableLookupProvider) {
        addGeneratorRecipes(reloadableLookupProvider);
        addFissionReactorRecipes(reloadableLookupProvider);
        addFusionReactorRecipes(reloadableLookupProvider);
        addTurbineRecipes(reloadableLookupProvider);
        addChemicalInfuserRecipes(reloadableLookupProvider);
        addElectrolyticSeparatorRecipes(reloadableLookupProvider);
        addSolarNeutronActivatorRecipes(reloadableLookupProvider);
        addGearModuleRecipes(reloadableLookupProvider);
        addRotaryRecipes();
        addUncheckedRecipe(RegistryUtils.synthetic(MekanismGenerators.rl("water"), "fission"));
    }

    private void addRotaryRecipes() {
        addRotaryRecipe(ChemicalIds.DEUTERIUM);
        addRotaryRecipe(ChemicalIds.FUSION_FUEL);
        addRotaryRecipe(ChemicalIds.TRITIUM);
    }

    private void addElectrolyticSeparatorRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "separator/";
        addRecipe(reloadableLookupProvider, basePath + "heavy_water");
    }

    private void addChemicalInfuserRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "chemical_infusing/";
        addRecipe(reloadableLookupProvider, basePath + "fusion_fuel");
    }

    private void addSolarNeutronActivatorRecipes(HolderLookup.Provider reloadableLookupProvider) {
        String basePath = "activating/";
        addRecipe(reloadableLookupProvider, basePath + "tritium");
    }

    private void addGeneratorRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, GeneratorsItems.SOLAR_PANEL);
        addRecipe(reloadableLookupProvider, "generator/solar");
        addRecipe(reloadableLookupProvider, "generator/advanced_solar");
        addRecipe(reloadableLookupProvider, "generator/bio");
        addRecipe(reloadableLookupProvider, "generator/gas_burning");
        addRecipe(reloadableLookupProvider, "generator/heat");
        addRecipe(reloadableLookupProvider, "generator/wind");
    }

    private void addFissionReactorRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, "fission_reactor/casing");
        addRecipe(reloadableLookupProvider, "fission_reactor/port");
        addRecipe(reloadableLookupProvider, "fission_reactor/logic_adapter");
        addRecipe(reloadableLookupProvider, "fission_reactor/fuel_assembly");
        addRecipe(reloadableLookupProvider, "fission_reactor/control_rod_assembly");
    }

    private void addFusionReactorRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, GeneratorsItems.HOHLRAUM);
        addRecipe(reloadableLookupProvider, GeneratorsBlocks.LASER_FOCUS_MATRIX);
        addRecipe(reloadableLookupProvider, "reactor/frame");
        addRecipe(reloadableLookupProvider, "reactor/glass");
        addRecipe(reloadableLookupProvider, "reactor/port");
        addRecipe(reloadableLookupProvider, "reactor/logic_adapter");
        addRecipe(reloadableLookupProvider, "reactor/controller");
    }

    private void addTurbineRecipes(HolderLookup.Provider reloadableLookupProvider) {
        addRecipe(reloadableLookupProvider, GeneratorsBlocks.ELECTROMAGNETIC_COIL);
        addRecipe(reloadableLookupProvider, GeneratorsBlocks.ROTATIONAL_COMPLEX);
        addRecipe(reloadableLookupProvider, GeneratorsBlocks.SATURATING_CONDENSER);
        addRecipe(reloadableLookupProvider, "turbine/blade");
        addRecipe(reloadableLookupProvider, "turbine/rotor");
        addRecipe(reloadableLookupProvider, "turbine/casing");
        addRecipe(reloadableLookupProvider, "turbine/valve");
        addRecipe(reloadableLookupProvider, "turbine/vent");
    }

    private void addGearModuleRecipes(HolderLookup.Provider reloadableLookupProvider) {
        for (DeferredHolder<ModuleData<?>, ? extends ModuleData<?>> module : GeneratorsModules.MODULES.getEntries()) {
            addRecipe(reloadableLookupProvider, module);
        }
    }
}