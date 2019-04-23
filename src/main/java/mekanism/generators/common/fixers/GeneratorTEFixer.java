package mekanism.generators.common.fixers;

import mekanism.common.fixers.TEFixer;
import mekanism.generators.common.MekanismGenerators;

public class GeneratorTEFixer extends TEFixer {

    public GeneratorTEFixer() {
        super(MekanismGenerators.MODID);
        putEntry("AdvancedSolarGenerator", "advanced_solar_generator");
        putEntry("BioGenerator", "bio_generator");
        putEntry("ElectromagneticCoil", "electromagnetic_coil");
        putEntry("GasGenerator", "gas_generator");
        putEntry("HeatGenerator", "heat_generator");
        putEntry("ReactorController", "reactor_controller");
        putEntry("ReactorFrame", "reactor_frame");
        putEntry("ReactorGlass", "reactor_glass");
        putEntry("ReactorLaserFocus", "reactor_laser_focus");
        putEntry("ReactorLogicAdapter", "reactor_logic_adapter");
        putEntry("ReactorPort", "reactor_port");
        putEntry("RotationalComplex", "rotational_complex");
        putEntry("SaturatingCondenser", "saturating_condenser");
        putEntry("SolarGenerator", "solar_generator");
        putEntry("TurbineCasing", "turbine_casing");
        putEntry("TurbineRod", "turbine_rod");
        putEntry("TurbineValve", "turbine_valve");
        putEntry("TurbineVent", "turbine_vent");
        putEntry("WindTurbine", "wind_turbine");
    }
}