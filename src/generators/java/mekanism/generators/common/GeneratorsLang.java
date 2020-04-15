package mekanism.generators.common;

import mekanism.common.base.ILangEntry;
import net.minecraft.util.Util;

public enum GeneratorsLang implements ILangEntry {
    PRODUCING_AMOUNT("gui", "producing.amount"),
    STORED_BIO_FUEL("gui", "bio_generator.bio_fuel"),
    INSUFFICIENT_FUEL("tooltip", "hohlraum.insufficient_fuel"),
    READY_FOR_REACTION("tooltip", "hohlraum.ready_for_reaction"),
    GAS_BURN_RATE("gui", "gas_burning_generator.burn_rate"),
    POWER("gui", "power"),
    OUTPUT_RATE_SHORT("gui", "output_rate.short"),
    SKY_BLOCKED("wind_generator", "sky_blocked"),
    NO_WIND("wind_generator", "no_wind"),

    REACTOR_PASSIVE("reactor", "stats.passive"),
    REACTOR_MIN_INJECTION("reactor", "stats.min_inject"),
    REACTOR_IGNITION("reactor", "stats.ignition"),
    REACTOR_MAX_PLASMA("reactor", "stats.max_plasma"),
    REACTOR_MAX_CASING("reactor", "stats.max_casing"),
    REACTOR_PASSIVE_RATE("reactor", "stats.passive_generation"),
    REACTOR_STEAM_PRODUCTION("reactor", "stats.steam_production"),
    REACTOR_ACTIVE("reactor", "stats.active"),
    HEAT_TAB("reactor", "heat"),
    STATS_TAB("reactor", "stats"),
    REACTOR_PLASMA("reactor", "heat.plasma"),
    REACTOR_CASE("reactor", "heat.case"),
    FUEL_TAB("reactor", "fuel"),
    REACTOR_INJECTION_RATE("reactor", "fuel.injection_rate"),
    REACTOR_EDIT_RATE("reactor", "fuel.edit_rate"),
    REACTOR_PORT_EJECT("reactor", "configurator.port_eject"),
    REACTOR_LOGIC_TOGGLE_COOLING("reactor", "logic.toggle_cooling"),
    REACTOR_LOGIC_REDSTONE_OUTPUT_MODE("reactor", "logic.redstone_output_mode"),
    REACTOR_LOGIC_ACTIVE_COOLING("reactor", "logic.active_cooling"),
    REACTOR_LOGIC_OUTPUTTING("reactor", "logic.outputting"),
    REACTOR_LOGIC_DISABLED("reactor", "logic.disabled"),
    REACTOR_LOGIC_READY("reactor", "logic.ready"),
    REACTOR_LOGIC_CAPACITY("reactor", "logic.capacity"),
    REACTOR_LOGIC_DEPLETED("reactor", "logic.depleted"),

    TURBINE("turbine", "industrial_turbine"),
    TURBINE_FLOW_RATE("turbine", "flow_rate"),
    TURBINE_STEAM_INPUT_RATE("turbine", "steam_input"),
    TURBINE_CAPACITY("turbine", "capacity"),
    TURBINE_PRODUCTION_AMOUNT("turbine", "production_amount"),
    TURBINE_STATS("turbine", "stats"),
    IS_LIMITING("turbine", "stats.limiting"),
    TURBINE_TANK_VOLUME("turbine", "stats.tank_volume"),
    TURBINE_STEAM_FLOW("turbine", "stats.steam_flow"),
    TURBINE_DISPERSERS("turbine", "stats.dispersers"),
    TURBINE_VENTS("turbine", "stats.vents"),
    TURBINE_BLADES("turbine", "stats.blades"),
    TURBINE_COILS("turbine", "stats.coils"),
    TURBINE_MAX_WATER_OUTPUT("turbine", "stats.max_water_output"),
    TURBINE_MAX_FLOW("turbine", "stats.max_flow"),
    TURBINE_MAX_PRODUCTION("turbine", "stats.max_production"),
    TURBINE_PRODUCTION("turbine", "stats.production"),

    FISSION_REACTOR("fission", "fission_reactor"),
    FISSION_ACTIVATE("fission", "activate"),
    FISSION_SCRAM("fission", "scram"),
    FISSION_DAMAGE("fission", "damage"),
    FISSION_WATER_TANK("fission", "water_tank"),
    FISSION_FUEL_TANK("fission", "fuel_tank"),
    FISSION_STEAM_TANK("fission", "steam_tank"),
    FISSION_WASTE_TANK("fission", "waste_tank"),
    FISSION_HEAT_GRAPH("fission", "heat_graph"),
    FISSION_PORT_MODE_CHANGE("fission", "port_mode_change"),
    FISSION_PORT_MODE_INPUT("fission", "port_mode_input"),
    FISSION_PORT_MODE_OUTPUT_WASTE("fission", "port_mode_output_waste"),
    FISSION_PORT_MODE_OUTPUT_STEAM("fission", "port_mode_output_steam"),
    //Descriptions
    DESCRIPTION_REACTOR_DISABLED("description", "reactor.logic.disabled"),
    DESCRIPTION_REACTOR_READY("description", "reactor.logic.ready"),
    DESCRIPTION_REACTOR_CAPACITY("description", "reactor.logic.capacity"),
    DESCRIPTION_REACTOR_DEPLETED("description", "reactor.logic.depleted"),

    DESCRIPTION_HEAT_GENERATOR("description", "heat_generator"),
    DESCRIPTION_SOLAR_GENERATOR("description", "solar_generator"),
    DESCRIPTION_GAS_BURNING_GENERATOR("description", "gas_burning_generator"),
    DESCRIPTION_BIO_GENERATOR("description", "bio_generator"),
    DESCRIPTION_ADVANCED_SOLAR_GENERATOR("description", "advanced_solar_generator"),
    DESCRIPTION_WIND_GENERATOR("description", "wind_generator"),
    DESCRIPTION_TURBINE_ROTOR("description", "turbine_rotor"),
    DESCRIPTION_ROTATIONAL_COMPLEX("description", "rotational_complex"),
    DESCRIPTION_ELECTROMAGNETIC_COIL("description", "electromagnetic_coil"),
    DESCRIPTION_TURBINE_CASING("description", "turbine_casing"),
    DESCRIPTION_TURBINE_VALVE("description", "turbine_valve"),
    DESCRIPTION_TURBINE_VENT("description", "turbine_vent"),
    DESCRIPTION_SATURATING_CONDENSER("description", "saturating_condenser"),

    DESCRIPTION_FISSION_REACTOR_CASING("description", "fission_reactor_casing"),
    DESCRIPTION_FISSION_REACTOR_PORT("description", "fission_reactor_port"),
    DESCRIPTION_FISSION_REACTOR_LOGIC_ADAPTER("description", "fission_reactor_logic_adapter"),
    DESCRIPTION_FISSION_FUEL_ASSEMBLY("description", "fission_fuel_assembly"),
    DESCRIPTION_CONTROL_ROD_ASSEMBLY("description", "control_rod_assembly"),

    DESCRIPTION_FUSION_REACTOR_GLASS("description", "fusion_reactor_glass"),
    DESCRIPTION_FUSION_REACTOR_FRAME("description", "fusion_reactor_frame"),
    DESCRIPTION_FUSION_REACTOR_PORT("description", "fusion_reactor_port"),
    DESCRIPTION_FUSION_REACTOR_LOGIC_ADAPTER("description", "fusion_reactor_logic_adapter"),
    DESCRIPTION_FUSION_REACTOR_CONTROLLER("description", "fusion_reactor_controller"),
    DESCRIPTION_LASER_FOCUS_MATRIX("description", "laser_focus_matrix");

    private final String key;

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