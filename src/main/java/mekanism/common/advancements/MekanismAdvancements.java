package mekanism.common.advancements;

import mekanism.common.Mekanism;
import org.jetbrains.annotations.Nullable;

public class MekanismAdvancements {

    private MekanismAdvancements() {
    }

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, Mekanism.rl(name));
    }

    public static final MekanismAdvancement ROOT = advancement(null, "root");
    public static final MekanismAdvancement MATERIALS = advancement(ROOT, "materials");

    public static final MekanismAdvancement CLEANING_GAUGES = advancement(MATERIALS, "cleaning_gauges");

    public static final MekanismAdvancement METALLURGIC_INFUSER = advancement(MATERIALS, "metallurgic_infuser");
    public static final MekanismAdvancement STEEL_INGOT = advancement(METALLURGIC_INFUSER, "steel_ingot");
    public static final MekanismAdvancement STEEL_CASING = advancement(STEEL_INGOT, "steel_casing");

    public static final MekanismAdvancement INFUSED_ALLOY = advancement(METALLURGIC_INFUSER, "infused_alloy");
    public static final MekanismAdvancement REINFORCED_ALLOY = advancement(INFUSED_ALLOY, "reinforced_alloy");
    public static final MekanismAdvancement ATOMIC_ALLOY = advancement(REINFORCED_ALLOY, "atomic_alloy");

    public static final MekanismAdvancement BASIC_CONTROL_CIRCUIT = advancement(METALLURGIC_INFUSER, "basic_control_circuit");
    public static final MekanismAdvancement ADVANCED_CONTROL_CIRCUIT = advancement(INFUSED_ALLOY, "advanced_control_circuit");
    public static final MekanismAdvancement ELITE_CONTROL_CIRCUIT = advancement(REINFORCED_ALLOY, "elite_control_circuit");
    public static final MekanismAdvancement ULTIMATE_CONTROL_CIRCUIT = advancement(ATOMIC_ALLOY, "ultimate_control_circuit");

    public static final MekanismAdvancement ALLOY_UPGRADING = advancement(INFUSED_ALLOY, "alloy_upgrading");
    public static final MekanismAdvancement LASER = advancement(REINFORCED_ALLOY, "laser");
    public static final MekanismAdvancement LASER_DEATH = advancement(LASER, "laser_death");
    public static final MekanismAdvancement STOPPING_LASERS = advancement(LASER, "stopping_lasers");
    public static final MekanismAdvancement AUTO_COLLECTION = advancement(LASER, "auto_collection");

    public static final MekanismAdvancement ALARM = advancement(BASIC_CONTROL_CIRCUIT, "alarm");
    public static final MekanismAdvancement INSTALLER = advancement(BASIC_CONTROL_CIRCUIT, "installer");
    public static final MekanismAdvancement FACTORY = advancement(BASIC_CONTROL_CIRCUIT, "factory");
    public static final MekanismAdvancement CONFIGURATION_COPYING = advancement(BASIC_CONTROL_CIRCUIT, "configuration_copying");
    public static final MekanismAdvancement RUNNING_FREE = advancement(BASIC_CONTROL_CIRCUIT, "running_free");
    public static final MekanismAdvancement PLAYING_WITH_FIRE = advancement(ADVANCED_CONTROL_CIRCUIT, "playing_with_fire");
    public static final MekanismAdvancement MACHINE_SECURITY = advancement(ELITE_CONTROL_CIRCUIT, "machine_security");
    public static final MekanismAdvancement SOLAR_NEUTRON_ACTIVATOR = advancement(ELITE_CONTROL_CIRCUIT, "sna");
    public static final MekanismAdvancement STABILIZING_CHUNKS = advancement(ULTIMATE_CONTROL_CIRCUIT, "stabilizing_chunks");

    public static final MekanismAdvancement PERSONAL_STORAGE = advancement(BASIC_CONTROL_CIRCUIT, "personal_storage");
    public static final MekanismAdvancement SIMPLE_MASS_STORAGE = advancement(BASIC_CONTROL_CIRCUIT, "simple_mass_storage");

    public static final MekanismAdvancement CONFIGURATOR = advancement(INFUSED_ALLOY, "configurator");
    public static final MekanismAdvancement NETWORK_READER = advancement(INFUSED_ALLOY, "network_reader");
    public static final MekanismAdvancement FLUID_TANK = advancement(INFUSED_ALLOY, "fluid_tank");
    public static final MekanismAdvancement CHEMICAL_TANK = advancement(INFUSED_ALLOY, "chemical_tank");

    public static final MekanismAdvancement BREATHING_ASSISTANCE = advancement(CHEMICAL_TANK, "breathing_assistance");
    public static final MekanismAdvancement HYDROGEN_POWERED_FLIGHT = advancement(CHEMICAL_TANK, "hydrogen_powered_flight");

    public static final MekanismAdvancement WASTE_REMOVAL = advancement(CHEMICAL_TANK, "waste_removal");
    public static final MekanismAdvancement ENVIRONMENTAL_RADIATION = advancement(WASTE_REMOVAL, "environmental_radiation");
    public static final MekanismAdvancement PERSONAL_RADIATION = advancement(ENVIRONMENTAL_RADIATION, "personal_radiation");
    public static final MekanismAdvancement RADIATION_PREVENTION = advancement(WASTE_REMOVAL, "radiation_prevention");
    public static final MekanismAdvancement RADIATION_POISONING = advancement(PERSONAL_RADIATION, "radiation_poisoning");
    public static final MekanismAdvancement RADIATION_POISONING_DEATH = advancement(RADIATION_POISONING, "radiation_poisoning_death");

    public static final MekanismAdvancement PLUTONIUM = advancement(WASTE_REMOVAL, "plutonium");
    public static final MekanismAdvancement SPS = advancement(PLUTONIUM, "sps");
    public static final MekanismAdvancement ANTIMATTER = advancement(SPS, "antimatter");
    public static final MekanismAdvancement NUCLEOSYNTHESIZER = advancement(ANTIMATTER, "nucleosynthesizer");

    public static final MekanismAdvancement SPS_EXPERIMENT_CREEPER = advancement(SPS, "sps_experiment_creeper");
    public static final MekanismAdvancement SPS_EXPERIMENT_MOOSHROOM = advancement(SPS, "sps_experiment_mooshroom");
    public static final MekanismAdvancement SPS_EXPERIMENT_PIG = advancement(SPS, "sps_experiment_pig");
    public static final MekanismAdvancement SPS_EXPERIMENT_VILLAGER = advancement(SPS, "sps_experiment_villager");

    public static final MekanismAdvancement POLONIUM = advancement(WASTE_REMOVAL, "polonium");

    public static final MekanismAdvancement TELEPORTATION_CORE = advancement(ATOMIC_ALLOY, "teleportation_core");
    public static final MekanismAdvancement QUANTUM_ENTANGLOPORTER = advancement(TELEPORTATION_CORE, "quantum_entangloporter");
    public static final MekanismAdvancement TELEPORTER = advancement(TELEPORTATION_CORE, "teleporter");
    public static final MekanismAdvancement PORTABLE_TELEPORTER = advancement(TELEPORTER, "portable_teleporter");

    public static final MekanismAdvancement QIO_DRIVE_ARRAY = advancement(TELEPORTATION_CORE, "qio_drive_array");
    public static final MekanismAdvancement QIO_EXPORTER = advancement(QIO_DRIVE_ARRAY, "qio_exporter");
    public static final MekanismAdvancement QIO_IMPORTER = advancement(QIO_DRIVE_ARRAY, "qio_importer");
    public static final MekanismAdvancement QIO_REDSTONE_ADAPTER = advancement(QIO_DRIVE_ARRAY, "qio_redstone_adapter");
    public static final MekanismAdvancement QIO_DASHBOARD = advancement(QIO_DRIVE_ARRAY, "qio_dashboard");
    public static final MekanismAdvancement PORTABLE_QIO_DASHBOARD = advancement(QIO_DASHBOARD, "portable_qio_dashboard");
    public static final MekanismAdvancement BASIC_QIO_DRIVE = advancement(QIO_DRIVE_ARRAY, "basic_qio_drive");
    public static final MekanismAdvancement ADVANCED_QIO_DRIVE = advancement(BASIC_QIO_DRIVE, "advanced_qio_drive");
    public static final MekanismAdvancement ELITE_QIO_DRIVE = advancement(ADVANCED_QIO_DRIVE, "elite_qio_drive");
    public static final MekanismAdvancement ULTIMATE_QIO_DRIVE = advancement(ELITE_QIO_DRIVE, "ultimate_qio_drive");

    public static final MekanismAdvancement ROBIT = advancement(ATOMIC_ALLOY, "robit");
    public static final MekanismAdvancement ROBIT_AESTHETICS = advancement(ROBIT, "robit_aesthetics");
    public static final MekanismAdvancement DIGITAL_MINER = advancement(ROBIT, "digital_miner");
    public static final MekanismAdvancement DICTIONARY = advancement(DIGITAL_MINER, "dictionary");
    public static final MekanismAdvancement STONE_GENERATOR = advancement(DIGITAL_MINER, "stone_generator");

    public static final MekanismAdvancement DISASSEMBLER = advancement(ATOMIC_ALLOY, "disassembler");
    public static final MekanismAdvancement MEKASUIT = advancement(DISASSEMBLER, "mekasuit");
    public static final MekanismAdvancement MODIFICATION_STATION = advancement(DISASSEMBLER, "modification_station");
    public static final MekanismAdvancement UPGRADED_MEKASUIT = advancement(MODIFICATION_STATION, "upgraded_mekasuit");

    public static final MekanismAdvancement FLUID_TRANSPORT = advancement(STEEL_INGOT, "fluid_transport");
    public static final MekanismAdvancement CHEMICAL_TRANSPORT = advancement(STEEL_INGOT, "chemical_transport");
    public static final MekanismAdvancement ENERGY_TRANSPORT = advancement(STEEL_INGOT, "energy_transport");
    public static final MekanismAdvancement HEAT_TRANSPORT = advancement(STEEL_INGOT, "heat_transport");
    public static final MekanismAdvancement ITEM_TRANSPORT = advancement(STEEL_INGOT, "item_transport");
    public static final MekanismAdvancement RESTRICTIVE_ITEM_TRANSPORT = advancement(ITEM_TRANSPORT, "restrictive_item_transport");
    public static final MekanismAdvancement DIVERSION_ITEM_TRANSPORT = advancement(ITEM_TRANSPORT, "diversion_item_transport");
    public static final MekanismAdvancement SORTER = advancement(ITEM_TRANSPORT, "logistical_sorter");

    public static final MekanismAdvancement ENERGY_CUBE = advancement(STEEL_CASING, "energy_cube");

    public static final MekanismAdvancement AUTOMATED_CRAFTING = advancement(STEEL_CASING, "automated_crafting");
    public static final MekanismAdvancement SEISMIC_VIBRATIONS = advancement(STEEL_CASING, "seismic_vibrations");
    public static final MekanismAdvancement PAINTING_MACHINE = advancement(ADVANCED_CONTROL_CIRCUIT, "painting_machine");

    public static final MekanismAdvancement ENRICHER = advancement(STEEL_CASING, "enricher");
    public static final MekanismAdvancement INFUSING_EFFICIENCY = advancement(ENRICHER, "infusing_efficiency");
    public static final MekanismAdvancement YELLOW_CAKE = advancement(ENRICHER, "yellow_cake");

    public static final MekanismAdvancement PURIFICATION_CHAMBER = advancement(ENRICHER, "purification_chamber");
    public static final MekanismAdvancement INJECTION_CHAMBER = advancement(PURIFICATION_CHAMBER, "injection_chamber");
    public static final MekanismAdvancement CHEMICAL_CRYSTALLIZER = advancement(INJECTION_CHAMBER, "chemical_crystallizer");

    public static final MekanismAdvancement SAWMILL = advancement(STEEL_CASING, "sawmill");
    public static final MekanismAdvancement MOVING_BLOCKS = advancement(SAWMILL, "moving_blocks");

    public static final MekanismAdvancement PUMP = advancement(STEEL_CASING, "pump");
    public static final MekanismAdvancement PLENISHER = advancement(PUMP, "plenisher");

    public static final MekanismAdvancement LIQUIFIER = advancement(STEEL_CASING, "liquifier");
    public static final MekanismAdvancement FULL_CANTEEN = advancement(LIQUIFIER, "full_canteen");
}