package mekanism.generators.common;

import mekanism.common.base.ILangEntry;
import net.minecraft.util.Util;

//TODO: Figure out some good way to organize this file
//TODO: Also go through and convert all keys to lower case?
//TODO: Also potentially make better names for various things
public enum GeneratorsLang implements ILangEntry {
    REACTOR_PORT_EJECT("tooltip.mekanism.configurator.reactor_port_eject"),

    PRODUCING("gui.mekanism.producing"),

    STORED_BIO_FUEL("gui.mekanism.bioGenerator.bioFuel"),
    INSUFFICIENT_FUEL("tooltip.mekanism.insufficientFuel"),
    READY_FOR_REACTION("tooltip.mekanism.readyForReaction"),

    BURN_RATE("gui.mekanism.burn_rate"),

    POWER("gui.mekanism.power"),

    TOGGLE_COOLING("gui.mekanism.toggleCooling"),

    SKY_BLOCKED("gui.mekanism.skyBlocked"),
    NO_WIND("gui.mekanism.noWind"),

    TURBINE_STATS("gui.mekanism.turbineStats"),
    LIMITING("gui.mekanism.limiting"),
    TANK_VOLUME("gui.mekanism.tankVolume"),
    STEAM_FLOW("gui.mekanism.steamFlow"),
    TURBINE_DISPERSERS("gui.mekanism.dispersers"),
    TURBINE_VENTS("gui.mekanism.vents"),
    TURBINE_BLADES("gui.mekanism.blades"),
    TURBINE_COILS("gui.mekanism.coils"),
    MAX_WATER_OUTPUT("gui.mekanism.maxWaterOutput"),
    TURBINE_MAX_FLOW("gui.mekanism.max_flow"),
    TURBINE_FLOW_RATE("gui.mekanism.flow_rate"),
    STEAM_INPUT_RATE("gui.mekanism.steamInput"),
    TURBINE_CAPACITY("gui.mekanism.capacity"),
    TURBINE_MAX_PRODUCTION("gui.mekanism.maxProduction"),
    PRODUCTION_AMOUNT("gui.mekanism.production_amount"),
    PRODUCTION("gui.mekanism.production"),
    TURBINE("gui.mekanism.industrial_turbine"),

    TRANSFERRED_RATE("gui.mekanism.transferred"),

    REACTOR_INJECTION_RATE("gui.mekanism.reactor.injectionRate"),
    REACTOR_PASSIVE("gui.mekanism.passive"),
    REACTOR_MIN_INJECTION("gui.mekanism.minInject"),
    REACTOR_IGNITION("gui.mekanism.ignition"),
    REACTOR_MAX_PLASMA("gui.mekanism.maxPlasma"),
    REACTOR_MAX_CASING("gui.mekanism.maxCasing"),
    REACTOR_PASSIVE_RATE("gui.mekanism.passiveGeneration"),
    REACTOR_STEAM_PRODUCTION("gui.mekanism.steamProduction"),
    REACTOR_ACTIVE("gui.mekanism.active"),
    REACTOR_PLASMA("gui.mekanism.reactor.plasma"),
    REACTOR_CASE("gui.mekanism.reactor.case"),
    REACTOR_EDIT_RATE("gui.mekanism.reactor.edit_rate"),

    REDSTONE_OUTPUT_MODE("gui.mekanism.redstoneOutputMode"),
    ACTIVE_COOLING("gui.mekanism.coolingMeasurements"),

    OUTPUTTING("gui.mekanism.outputting"),


    REACTOR_DISABLED("mekanism.reactor.disabled"),
    REACTOR_READY("mekanism.reactor.ready"),
    REACTOR_CAPACITY("mekanism.reactor.capacity"),
    REACTOR_DEPLETED("mekanism.reactor.depleted"),

    //TODO: Should we remove this in favor of just having one output rate?
    OUTPUT_RATE_SHORT("gui.mekanism.out"),

    FUEL_TAB("gui.mekanism.fuel_tab"),
    HEAT_TAB("gui.mekanism.heat"),
    //Descriptions
    REACTOR_DISABLED_DESCRIPTION("mekanism.reactor.disabled.desc"),
    REACTOR_READY_DESCRIPTION("mekanism.reactor.ready.desc"),
    REACTOR_CAPACITY_DESCRIPTION("mekanism.reactor.capacity.desc"),
    REACTOR_DEPLETED_DESCRIPTION("mekanism.reactor.depleted.desc"),

    DESCRIPTION_HEAT_GENERATOR("tooltip.mekanism.description.heat_generator"),
    DESCRIPTION_SOLAR_GENERATOR("tooltip.mekanism.description.solar_generator"),
    DESCRIPTION_GAS_BURNING_GENERATOR("tooltip.mekanism.description.gas_burning_generator"),
    DESCRIPTION_BIO_GENERATOR("tooltip.mekanism.description.bio_generator"),
    DESCRIPTION_ADVANCED_SOLAR_GENERATOR("tooltip.mekanism.description.advanced_solar_generator"),
    DESCRIPTION_WIND_GENERATOR("tooltip.mekanism.description.wind_generator"),
    DESCRIPTION_TURBINE_ROTOR("tooltip.mekanism.description.turbine_rotor"),
    DESCRIPTION_ROTATIONAL_COMPLEX("tooltip.mekanism.description.rotational_complex"),
    DESCRIPTION_ELECTROMAGNETIC_COIL("tooltip.mekanism.description.electromagnetic_coil"),
    DESCRIPTION_TURBINE_CASING("tooltip.mekanism.description.turbine_casing"),
    DESCRIPTION_TURBINE_VALVE("tooltip.mekanism.description.turbine_valve"),
    DESCRIPTION_TURBINE_VENT("tooltip.mekanism.description.turbine_vent"),
    DESCRIPTION_SATURATING_CONDENSER("tooltip.mekanism.description.saturating_condenser"),

    DESCRIPTION_REACTOR_GLASS("tooltip.mekanism.description.reactor_glass"),
    DESCRIPTION_LASER_FOCUS_MATRIX("tooltip.mekanism.description.laser_focus_matrix"),
    DESCRIPTION_REACTOR_CONTROLLER("tooltip.mekanism.description.reactor_controller"),
    DESCRIPTION_REACTOR_FRAME("tooltip.mekanism.description.reactor_frame"),
    DESCRIPTION_REACTOR_PORT("tooltip.mekanism.description.reactor_port"),
    DESCRIPTION_REACTOR_LOGIC_ADAPTER("tooltip.mekanism.description.reactor_logic_adapter"),
    ;

    private final String key;

    //TODO: Use this?
    GeneratorsLang(String type, String path) {
        this(Util.makeTranslationKey(type, MekanismGenerators.rl(path)));
    }

    GeneratorsLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}