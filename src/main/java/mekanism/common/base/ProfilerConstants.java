package mekanism.common.base;

public class ProfilerConstants {

    private ProfilerConstants() {
    }

    //Tile
    public static final String BIN = "bin";
    public static final String CONFIGURABLE_MACHINE = "configurableMachine";
    public static final String DIGITAL_MINER = "digitalMiner";
    public static final String DIMENSIONAL_STABILIZER = "dimensionalStabilizer";
    public static final String DYNAMIC_TANK = "dynamicTank";
    public static final String ENERGY_CUBE = "energyCube";
    public static final String FLUID_TANK = "fluidTank";
    public static final String INDUSTRIAL_ALARM = "industrialAlarm";
    public static final String MEKANISM_OUTLINE = "mekOutline";
    public static final String AREA_MINE_OUTLINE = "areaMineOutline";
    public static final String NUTRITIONAL_LIQUIFIER = "nutritionalLiquifier";
    public static final String PERSONAL_CHEST = "personalChest";
    public static final String PIGMENT_MIXER = "pigmentMixer";
    public static final String RESISTIVE_HEATER = "resistiveHeater";
    public static final String SEISMIC_VIBRATOR = "seismicVibrator";
    public static final String TELEPORTER = "teleporter";
    public static final String THERMAL_EVAPORATION_CONTROLLER = "thermalEvaporationController";
    public static final String THERMOELECTRIC_BOILER = "thermoelectricBoiler";
    public static final String SPS = "supercriticalPhaseShifter";

    //Sub parts
    private static final String CORE = "core";

    //Transmitter
    public static final String LOGISTICAL_TRANSPORTER = "logisticalTransporter";
    public static final String MECHANICAL_PIPE = "mechanicalPipe";
    public static final String PRESSURIZED_TUBE = "pressurizedTube";
    public static final String THERMODYNAMIC_CONDUCTOR = "thermodynamicConductor";
    public static final String UNIVERSAL_CABLE = "universalCable";

    //Lazy/delayed rendering
    public static final String DELAYED = "delayedMekanismTranslucentBERs";
    public static final String ENERGY_CUBE_CORE = ENERGY_CUBE + "." + CORE;
    public static final String SPS_CORE = SPS + "." + CORE;
    public static final String SPS_ORBIT = SPS + ".orbitEffect";
}