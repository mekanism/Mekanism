package mekanism.generators.client.integration.emi;

import java.util.concurrent.CompletableFuture;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.integration.emi.BaseEmiDefaults;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@NothingNullByDefault
public class GeneratorsEmiDefaults extends BaseEmiDefaults {

    public GeneratorsEmiDefaults(PackOutput output, ExistingFileHelper existingFileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, existingFileHelper, registries, MekanismGenerators.MODID);
    }

    @Override
    protected void addDefaults(HolderLookup.Provider lookupProvider) {
        addGeneratorRecipes();
        addFissionReactorRecipes();
        addFusionReactorRecipes();
        addTurbineRecipes();
        addChemicalInfuserRecipes();
        addElectrolyticSeparatorRecipes();
        addSolarNeutronActivatorRecipes();
        addGearModuleRecipes();
        addRotaryRecipes();
        addUncheckedRecipe(RecipeViewerUtils.synthetic(MekanismGenerators.rl("water"), "fission"));
    }

    private void addRotaryRecipes() {
        addRotaryRecipe(GeneratorsChemicals.DEUTERIUM);
        addRotaryRecipe(GeneratorsChemicals.FUSION_FUEL);
        addRotaryRecipe(GeneratorsChemicals.TRITIUM);
    }

    private void addElectrolyticSeparatorRecipes() {
        String basePath = "separator/";
        addRecipe(basePath + "heavy_water");
    }

    private void addChemicalInfuserRecipes() {
        String basePath = "chemical_infusing/";
        addRecipe(basePath + "fusion_fuel");
    }

    private void addSolarNeutronActivatorRecipes() {
        String basePath = "activating/";
        addRecipe(basePath + "tritium");
    }

    private void addGeneratorRecipes() {
        addRecipe(GeneratorsItems.SOLAR_PANEL);
        addRecipe("generator/solar");
        addRecipe("generator/advanced_solar");
        addRecipe("generator/bio");
        addRecipe("generator/gas_burning");
        addRecipe("generator/heat");
        addRecipe("generator/wind");
    }

    private void addFissionReactorRecipes() {
        addRecipe("fission_reactor/casing");
        addRecipe("fission_reactor/port");
        addRecipe("fission_reactor/logic_adapter");
        addRecipe("fission_reactor/fuel_assembly");
        addRecipe("fission_reactor/control_rod_assembly");
    }

    private void addFusionReactorRecipes() {
        addRecipe(GeneratorsItems.HOHLRAUM);
        addRecipe(GeneratorsBlocks.LASER_FOCUS_MATRIX);
        addRecipe("reactor/frame");
        addRecipe("reactor/glass");
        addRecipe("reactor/port");
        addRecipe("reactor/logic_adapter");
        addRecipe("reactor/controller");
    }

    private void addTurbineRecipes() {
        addRecipe(GeneratorsBlocks.ELECTROMAGNETIC_COIL);
        addRecipe(GeneratorsBlocks.ROTATIONAL_COMPLEX);
        addRecipe(GeneratorsBlocks.SATURATING_CONDENSER);
        addRecipe("turbine/blade");
        addRecipe("turbine/rotor");
        addRecipe("turbine/casing");
        addRecipe("turbine/valve");
        addRecipe("turbine/vent");
    }

    private void addGearModuleRecipes() {
        addRecipe(GeneratorsItems.MODULE_GEOTHERMAL_GENERATOR);
        addRecipe(GeneratorsItems.MODULE_SOLAR_RECHARGING);
    }
}