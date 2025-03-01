package mekanism.generators.common.config;

import mekanism.common.config.IConfigTranslation;
import mekanism.common.config.TranslationPreset;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum GeneratorsConfigTranslations implements IConfigTranslation {
    SERVER_HOHLRAUM("server.hohlraum", "Hohlraum", "Settings for configuring Hohlraum", true),
    SERVER_HOHLRAUM_CAPACITY("server.hohlraum.capacity", "Capacity", "Hohlraum capacity in mB."),
    SERVER_HOHLRAUM_FILL_RATE("server.hohlraum.fill_rate", "Fill Rate", "Rate in mB/t at which Hohlraum can accept D-T Fuel."),

    SERVER_GENERATOR_SOLAR("server.generator.solar", "Solar Generator", "Settings for configuring Solar Generators", true),
    SERVER_SOLAR_GENERATION("server.generator.solar.gen", "Energy Generation",
          "Peak energy generation in Joules/t for the Solar Generator. Note: It can go higher than this value in some extreme environments."),
    SERVER_SOLAR_GENERATION_ADVANCED("server.generator.solar.gen.advanced", "Advanced Solar Energy Generation",
          "Peak energy generation in Joules/t for the Advanced Solar Generator. Note: It can go higher than this value in some extreme environments."),

    SERVER_GENERATOR_BIO("server.generator.bio", "Bio Generator", "Settings for configuring Bio Generators", true),
    SERVER_GENERATOR_BIO_GENERATION("server.generator.bio.gen", "Energy Generation", "Energy produced by the Bio Generator in Joules/t."),
    SERVER_GENERATOR_BIO_TANK_CAPACITY("server.generator.bio.tank_capacity", "Tank Capacity", "The capacity in mB of the fluid tank in the Bio Generator."),

    SERVER_GENERATOR_HEAT("server.generator.heat", "Heat Generator", "Settings for configuring Heat Generators", true),
    SERVER_GENERATOR_HEAT_GENERATION("server.generator.heat.gen", "Energy Generation",
          "Amount of energy in Joules the Heat Generator produces per tick. heatGeneration + heatGenerationLava * lavaSides + heatGenerationNether. "
          + "Note: lavaSides is how many sides are adjacent to lava, this includes the block itself if it is lava logged allowing for a max of 7 \"sides\"."),
    SERVER_GENERATOR_HEAT_GEN_LAVA("server.generator.heat.gen.lava", "Submerged Energy Generation",
          "Multiplier of effectiveness of Lava that is adjacent to the Heat Generator."),
    SERVER_GENERATOR_HEAT_GEN_NETHER("server.generator.heat.gen.nether", "Nether Energy Generation",
          "Add this amount of Joules to the energy produced by a heat generator if it is in an 'ultrawarm' dimension, in vanilla this is just the Nether."),
    SERVER_GENERATOR_HEAT_TANK_CAPACITY("server.generator.heat.tank_capacity", "Tank Capacity", "The capacity in mB of the fluid tank in the Heat Generator."),
    SERVER_GENERATOR_HEAT_FLUID_RATE("server.generator.heat.fluid_rate", "Fluid Rate",
          "The amount of lava in mB that gets consumed to transfer heatGeneration Joules to the Heat Generator."),

    SERVER_GENERATOR_GAS("server.generator.gas", "Gas-Burning Generator", "Settings for configuring Gas-Burning Generators", true),
    SERVER_GENERATOR_GAS_TANK_CAPACITY("server.generator.gas.tank_capacity", "Tank Capacity", "The capacity in mB of the chemical tank in the Gas-Burning Generator."),

    SERVER_GENERATOR_WIND("server.generator.wind", "Wind Generator", "Settings for configuring Wind Generators", true),
    SERVER_GENERATOR_WIND_GEN_MIN("server.generator.wind.gen.min", "Min Energy Generation", "Minimum energy generation in Joules/t that the Wind Generator can produce."),
    SERVER_GENERATOR_WIND_GEN_MAX("server.generator.wind.gen.max", "Max Energy Generation", "Maximum energy generation in Joules/t that the Wind Generator can produce."),
    SERVER_GENERATOR_WIND_GEN_MIN_Y("server.generator.wind.gen.min.height", "Min Y Value",
          "The minimum Y value that affects the Wind Generators Power generation. This value gets clamped at the world's minimum height."),
    SERVER_GENERATOR_WIND_GEN_MAX_Y("server.generator.wind.gen.max.height", "Max Y Value",
          "The maximum Y value that affects the Wind Generators Power generation. This value gets clamped at the world's logical height. For example for worlds like "
          + "the nether that are 256 blocks tall, but have a ceiling at 128 blocks, this would be clamped at 128."),

    SERVER_TURBINE("server.turbine", "Industrial Turbine", "Settings for configuring Industrial Turbines", "Edit Turbine Settings"),
    SERVER_TURBINE_BLADES("server.turbine.blades", "Blades Per Coil", "The number of Turbine Blades supported by each Electromagnetic Coil."),
    SERVER_TURBINE_RATE_VENT("server.turbine.rate.vent", "Vent Rate", "The rate in mB/t at which steam is vented into the turbine."),
    SERVER_TURBINE_RATE_DISPERSER("server.turbine.rate.disperser", "Dispersion Rate", "The rate in mB/t at which steam is dispersed into the turbine."),
    SERVER_TURBINE_RATE_CONDENSER("server.turbine.rate.condenser", "Condensation Rate", "The rate in mB/t at which steam is condensed into water in the turbine."),
    SERVER_TURBINE_ENERGY_CAPACITY("server.turbine.capacity.energy", "Energy Capacity Per Volume",
          "Amount of energy in Joules that each block of the turbine contributes to the total energy capacity. Max = volume * energyCapacityPerVolume"),
    SERVER_TURBINE_CHEMICAL_CAPACITY("server.turbine.capacity.chemical", "Chemical Per Tank",
          "Amount of chemical (mB) that each block of the turbine's steam cavity contributes to the volume. Max = volume * chemicalPerTank"),

    SERVER_FISSION("server.fission", "Fission Reactor", "Settings for configuring Fission Reactors", "Edit Reactor Settings"),
    SERVER_FISSION_FUEL_ENERGY("server.fission.fuel_energy", "Energy Per Fissile Fuel",
          "Amount of energy created (in heat) from each whole mB of fission fuel."),
    SERVER_FISSION__CASING_HEAT_CAPACITY("server.fission.casing_heat_capacity", "Casing Heat Capacity",
          "The heat capacity added to a Fission Reactor by a single casing block. Increase to require more energy to raise the reactor temperature."),
    SERVER_FISSION_SURFACE_AREA("server.fission.surface_area", "Surface Area Target",
          "The average surface area of a Fission Reactor's fuel assemblies to reach 100% boil efficiency. Higher values make it harder to cool the reactor."),
    SERVER_FISSION_DEFAULT_BURN_RATE("server.fission.burn_rate.default", "Default Burn Rate", "The default burn rate of the fission reactor."),
    SERVER_FISSION_BURN_PER_ASSEMBLY("server.fission.burn_rate.assembly", "Burn Rate Per Fuel Assembly",
          "The burn rate increase each fuel assembly provides. Max Burn Rate = fuelAssemblies * burnPerAssembly"),
    SERVER_FISSION_FUEL_CAPACITY("server.fission.assembly_fuel_capacity", "Assembly Fuel Capacity",
          "Amount of fuel (mB) that each assembly contributes to the fuel and waste capacity. Max = fuelAssemblies * maxFuelPerAssembly"),
    SERVER_FISSION_COOLED_COOLANT_CAPACITY("server.fission.coolant_capacity.cooled", "Cooled Coolant Capacity",
          "Amount of cooled coolant (mB) that each block of the fission reactor contributes to the volume. Max = volume * cooledCoolantPerTank"),
    SERVER_FISSION_HEATED_COOLANT_CAPACITY("server.fission.coolant_capacity.heated", "Heated Coolant Capacity",
          "Amount of heated coolant (mB) that each block of the fission reactor contributes to the volume. Max = volume * heatedCoolantPerTank"),
    SERVER_FISSION_EXCESS_WASTE("server.fission.excess_waste", "Excess Waste Percentage",
          "The percentage of waste in a fission reactor's output waste tank that is necessary to trigger the excess waste."),

    SERVER_FISSION_MELTDOWNS("server.fission.meltdowns", "Meltdowns", "Settings for configuring Fission Reactor Meltdowns.", "Edit Meltdown Settings"),
    SERVER_FISSION_MELTDOWNS_ENABLED("server.fission.meltdowns.enabled", "Enabled",
          "Whether catastrophic meltdowns can occur from Fission Reactors. If this is disabled, instead of melting down the reactor will force turn itself off "
          + "and not be able to be turned back on until the damage level has returned to safe levels."),
    SERVER_FISSION_MELTDOWNS_RADIUS("server.fission.meltdowns.radius", "Explosion Radius", "The radius of the explosion that occurs from a meltdown."),
    SERVER_FISSION_MELTDOWNS_CHANCE("server.fission.meltdowns.chance", "Meltdown Chance",
          "The chance of a catastrophic meltdown occurring once the reactor's damage passes 100%. This will linearly scale as damage continues increasing passed 100%."),
    SERVER_FISSION_MELTDOWNS_RADIATION_MULTIPLIER("server.fission.meltdowns.radiation_multiplier", "Radiation Multiplier",
          "How much radioactivity of fuel/waste contents are multiplied during a meltdown."),
    SERVER_FISSION_POST_MELTDOWN_DAMAGE("server.fission.meltdowns.damage", "Post Meltdown Damage",
          "Percent damage that a reactor will start at after being reconstructed after a meltdown."),

    SERVER_FUSION("server.fusion", "Fusion Reactor", "Settings for configuring Fusion Reactors", "Edit Reactor Settings"),
    SERVER_FUSION_FUEL_ENERGY("server.fusion.fuel_energy", "Energy Per D-T Fuel", "Affects the Injection Rate, Max Temp, and Ignition Temp."),
    SERVER_FUSION_THERMOCOUPLE_EFFICIENCY("server.fusion.thermocouple_efficiency", "Thermocouple Efficiency",
          "The fraction of the heat dissipated from the case that is converted to Joules."),
    SERVER_FUSION_THERMAL_CONDUCTIVITY("server.fusion.casing_thermal_conductivity", "Casing Thermal Conductivity",
          "The fraction of heat from the casing that can be transferred to all sources that are not water. Will impact max heat, heat transfer to "
          + "thermodynamic conductors, and power generation."),
    SERVER_FUSION_HEATING_RATE("server.fusion.water_heating_ratio", "Water Heating Ratio",
          "The fraction of the heat from the casing that is dissipated to water when water cooling is in use. Will impact max heat, and steam generation."),
    SERVER_FUSION_FUEL_CAPACITY("server.fusion.capacity.fuel", "Fuel Capacity", "Amount of fuel (mB) that the fusion reactor can store."),
    SERVER_FUSION_ENERGY_CAPACITY("server.fusion.capacity.energy", "Energy Capacity", "Amount of energy (Joules) the fusion reactor can store."),
    SERVER_FUSION_WATER_INJECTION("server.fusion.injection.water", "Water Per Injection",
          "Amount of water (mB) per injection rate that the fusion reactor can store. Max = injectionRate * waterPerInjection"),
    SERVER_FUSION_STEAM_INJECTION("server.fusion.injection.steam", "Steam Per Injection",
          "Amount of steam (mB) per injection rate that the fusion reactor can store. Max = injectionRate * steamPerInjection"),

    //STORAGE CONFIG FILE

    ENERGY_STORAGE_GENERATOR_HEAT(TranslationPreset.ENERGY_STORAGE, "Heat Generator"),
    ENERGY_STORAGE_GENERATOR_BIO(TranslationPreset.ENERGY_STORAGE, "Bio-Generator"),
    ENERGY_STORAGE_GENERATOR_SOLAR(TranslationPreset.ENERGY_STORAGE, "Solar Generator"),
    ENERGY_STORAGE_GENERATOR_SOLAR_ADVANCED(TranslationPreset.ENERGY_STORAGE, "Advanced Solar Generator"),
    ENERGY_STORAGE_GENERATOR_WIND(TranslationPreset.ENERGY_STORAGE, "Wind Generator"),

    //GEAR CONFIG FILE

    GEAR_MEKA_SUIT_GEOTHERMAL("gear.meka_suit.charge_rate.geothermal", "Geothermal Charging Rate",
          "Geothermal charging rate in Joules per tick, per degree above ambient, per upgrade installed. This value scales down based on how much of "
          + "the MekaSuit Pants is submerged. Fire is treated as having a temperature of ~200K above ambient, lava has a temperature of 1,000K above ambient."),

    GEAR_MEKA_SUIT_HEAT_DAMAGE("gear.meka_suit.damage_absorption.heat", "Heat Damage Reduction",
          "Percent of heat damage negated by MekaSuit Pants with maximum geothermal generator units installed. This number scales down linearly based on how many "
          + "units are actually installed."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    GeneratorsConfigTranslations(TranslationPreset preset, String type) {
        this(preset.path(type), preset.title(type), preset.tooltip(type));
    }

    GeneratorsConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    GeneratorsConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
    }

    GeneratorsConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = Util.makeDescriptionId("configuration", MekanismGenerators.rl(path));
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Nullable
    @Override
    public String button() {
        return button;
    }
}