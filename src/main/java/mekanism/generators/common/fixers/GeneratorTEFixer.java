package mekanism.generators.common.fixers;

import mekanism.common.fixers.TEFixer;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorTEFixer extends TEFixer {

    public GeneratorTEFixer() {
        super(MekanismGenerators.MODID);
        putEntry("ReactorFrame", "reactor_frame");
        putEntry("ReactorGlass", "reactor_glass");
        putEntry("ReactorLaserFocus", "reactor_laser_focus");
        putEntry("ReactorPort", "reactor_port");
        putEntry("ReactorLogicAdapter", "reactor_logic_adapter");
        putEntry("RotationalComplex", "rotational_complex");
        putEntry("ElectromagneticCoil", "electromagnetic_coil");
        putEntry("SaturatingCondenser", "saturating_condenser");
        putEntry("AdvancedSolarGenerator", "advanced_solar_generator");
        putEntry("SolarGenerator", "solar_generator");
        putEntry("BioGenerator", "bio_generator");
        putEntry("HeatGenerator", "heat_generator");
        putEntry("GasGenerator", "gas_generator");
        putEntry("WindTurbine", "wind_turbine");
        putEntry("ReactorController", "reactor_controller");
        putEntry("TurbineRod", "turbine_rod");
        putEntry("TurbineCasing", "turbine_casing");
        putEntry("TurbineValve", "turbine_valve");
        putEntry("TurbineVent", "turbine_vent");
    }
}