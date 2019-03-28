package mekanism.common.util;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

/**
 * Created by Thiakil on 14/03/2019.
 */
public abstract class TEFixer implements IFixableData {

    // array only needs to cover legacy tile entity ids, no need to add future tile entity ids to list.
    private final Map<String, String> tileEntityNames = new HashMap<>();

    protected abstract void lazyInit();

    protected void putEntry(String oldName, String newName) {
        tileEntityNames.put(oldName, newName);
        tileEntityNames.put("minecraft:" + oldName.toLowerCase(), newName);
    }

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    @Nonnull
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
        if (tileEntityNames.isEmpty()) {
            lazyInit();
        }

        String tileEntityLocation = compound.getString("id");

        if (tileEntityLocation.equals("mcmultipart:multipart.ticking") || tileEntityLocation
              .equals("mcmultipart:multipart.nonticking")) {
            if (compound.hasKey("parts")) {
                NBTTagCompound parts = compound.getCompoundTag("parts");
                for (String sID : parts.getKeySet()) {
                    NBTTagCompound part = parts.getCompoundTag(sID);
                    if (part.hasKey("tile")) {
                        fixTagCompound(part.getCompoundTag("tile"));
                    }
                }
            }
        } else {
            compound.setString("id", tileEntityNames.getOrDefault(tileEntityLocation, tileEntityLocation));
        }

        return compound;
    }

    public static class Mekanism extends TEFixer {

        @Override
        protected void lazyInit() {
            putEntry("BoundingBlock", "mekanism:bounding_block");
            putEntry("AdvancedBoundingBlock", "mekanism:advanced_bounding_block");
            putEntry("CardboardBox", "mekanism:cardboard_box");
            putEntry("ThermalEvaporationValve", "mekanism:thermal_evaporation_valve");
            putEntry("ThermalEvaporationBlock", "mekanism:thermal_evaporation_block");
            putEntry("PressureDisperser", "mekanism:pressure_disperser");
            putEntry("SuperheatingElement", "mekanism:superheating_element");
            putEntry("Laser", "mekanism:laser");
            putEntry("AmbientAccumulator", "mekanism:ambient_accumulator");
            putEntry("InductionCasing", "mekanism:induction_casing");
            putEntry("InductionPort", "mekanism:induction_port");
            putEntry("InductionCell", "mekanism:induction_cell");
            putEntry("InductionProvider", "mekanism:induction_provider");
            putEntry("Oredictionificator", "mekanism:oredictionificator");
            putEntry("StructuralGlass", "mekanism:structural_glass");
            putEntry("FuelwoodHeater", "mekanism:fuelwood_heater");
            putEntry("LaserAmplifier", "mekanism:laser_amplifier");
            putEntry("LaserTractorBeam", "mekanism:laser_tractor_beam");
            putEntry("ChemicalWasher", "mekanism:chemical_washer");
            putEntry("ElectrolyticSeparator", "mekanism:electrolytic_separator");
            putEntry("ChemicalOxidizer", "mekanism:chemical_oxidizer");
            putEntry("ChemicalInfuser", "mekanism:chemical_infuser");
            putEntry("RotaryCondensentrator", "mekanism:rotary_condensentrator");
            putEntry("ElectricPump", "mekanism:electric_pump");
            putEntry("FluidicPlenisher", "mekanism:fluidic_plenisher");
            putEntry("GlowPanel", "mekanism:glow_panel");
            putEntry("EnrichmentChamber", "mekanism:enrichment_chamber");
            putEntry("OsmiumCompressor", "mekanism:osmium_compressor");
            putEntry("Combiner", "mekanism:combiner");
            putEntry("Crusher", "mekanism:crusher");
            putEntry("SmeltingFactory", "mekanism:smelting_factory");
            putEntry("AdvancedSmeltingFactory", "mekanism:advanced_smelting_factory");
            putEntry("UltimateSmeltingFactory", "mekanism:ultimate_smelting_factory");
            putEntry("PurificationChamber", "mekanism:purification_chamber");
            putEntry("EnergizedSmelter", "mekanism:energized_smelter");
            putEntry("MetallurgicInfuser", "mekanism:metallurgic_infuser");
            putEntry("GasTank", "mekanism:gas_tank");
            putEntry("EnergyCube", "mekanism:energy_cube");
            putEntry("PersonalChest", "mekanism:personal_chest");
            putEntry("DynamicTank", "mekanism:dynamic_tank");
            putEntry("DynamicValve", "mekanism:dynamic_valve");
            putEntry("Chargepad", "mekanism:chargepad");
            putEntry("LogisticalSorter", "mekanism:logistical_sorter");
            putEntry("Bin", "mekanism:bin");
            putEntry("DigitalMiner", "mekanism:digital_miner");
            putEntry("MekanismTeleporter", "mekanism:mekanism_teleporter");
            putEntry("ChemicalInjectionChamber", "mekanism:chemical_injection_chamber");
            putEntry("ThermalEvaporationController", "mekanism:thermal_evaporation_controller");
            putEntry("PrecisionSawmill", "mekanism:precision_sawmill");
            putEntry("ChemicalCrystallizer", "mekanism:chemical_crystallizer");
            putEntry("SeismicVibrator", "mekanism:seismic_vibrator");
            putEntry("PressurizedReactionChamber", "mekanism:pressurized_reaction_chamber");
            putEntry("FluidTank", "mekanism:fluid_tank");
            putEntry("SolarNeutronActivator", "mekanism:solar_neutron_activator");
            putEntry("FormulaicAssemblicator", "mekanism:formulaic_assemblicator");
            putEntry("ResistiveHeater", "mekanism:resistive_heater");
            putEntry("BoilerCasing", "mekanism:boiler_casing");
            putEntry("BoilerValve", "mekanism:boiler_valve");
            putEntry("SecurityDesk", "mekanism:security_desk");
            putEntry("QuantumEntangloporter", "mekanism:quantum_entangloporter");
            putEntry("ChemicalDissolutionChamber", "mekanism:chemical_dissolution_chamber");
            putEntry("MechanicalPipe", "mekanism:mechanical_pipe");
            putEntry("UniversalCable", "mekanism:universal_cable");
            putEntry("ThermodynamicConductor", "mekanism:thermodynamic_conductor");
            putEntry("LogisticalTransporter", "mekanism:logistical_transporter");
            putEntry("PressurizedTube", "mekanism:pressurized_tube");
            putEntry("DiversionTransporter", "mekanism:diversion_transporter");
            putEntry("RestrictiveTransporter", "mekanism:restrictive_transporter");
        }
    }

    public static class Generators extends TEFixer {

        @Override
        protected void lazyInit() {
            putEntry("ReactorFrame", "mekanismgenerators:reactor_frame");
            putEntry("ReactorGlass", "mekanismgenerators:reactor_glass");
            putEntry("ReactorLaserFocus", "mekanismgenerators:reactor_laser_focus");
            putEntry("ReactorPort", "mekanismgenerators:reactor_port");
            putEntry("ReactorLogicAdapter", "mekanismgenerators:reactor_logic_adapter");
            putEntry("RotationalComplex", "mekanismgenerators:rotational_complex");
            putEntry("ElectromagneticCoil", "mekanismgenerators:electromagnetic_coil");
            putEntry("SaturatingCondenser", "mekanismgenerators:saturating_condenser");
            putEntry("AdvancedSolarGenerator", "mekanismgenerators:advanced_solar_generator");
            putEntry("SolarGenerator", "mekanismgenerators:solar_generator");
            putEntry("BioGenerator", "mekanismgenerators:bio_generator");
            putEntry("HeatGenerator", "mekanismgenerators:heat_generator");
            putEntry("GasGenerator", "mekanismgenerators:gas_generator");
            putEntry("WindTurbine", "mekanismgenerators:wind_turbine");
            putEntry("ReactorController", "mekanismgenerators:reactor_controller");
            putEntry("TurbineRod", "mekanismgenerators:turbine_rod");
            putEntry("TurbineCasing", "mekanismgenerators:turbine_casing");
            putEntry("TurbineValve", "mekanismgenerators:turbine_valve");
            putEntry("TurbineVent", "mekanismgenerators:turbine_vent");
        }
    }
}
