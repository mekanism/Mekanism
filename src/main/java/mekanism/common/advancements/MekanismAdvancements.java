package mekanism.common.advancements;

public class MekanismAdvancements {

    private MekanismAdvancements() {
    }

    public static final MekanismAdvancement ROOT = new MekanismAdvancement(null, "root");
    public static final MekanismAdvancement MATERIALS = new MekanismAdvancement(ROOT, "materials");
    public static final MekanismAdvancement FLUID_TANK = new MekanismAdvancement(ROOT, "fluid_tank");
    public static final MekanismAdvancement CHEMICAL_TANK = new MekanismAdvancement(ROOT, "chemical_tank");
    public static final MekanismAdvancement FULL_CANTEEN = new MekanismAdvancement(MATERIALS, "full_canteen");
    public static final MekanismAdvancement METALLURGIC_INFUSER = new MekanismAdvancement(MATERIALS, "metallurgic_infuser");
    public static final MekanismAdvancement STEEL_INGOT = new MekanismAdvancement(METALLURGIC_INFUSER, "steel_ingot");
    public static final MekanismAdvancement STEEL_CASING = new MekanismAdvancement(STEEL_INGOT, "steel_casing");
    public static final MekanismAdvancement INFUSED_ALLOY = new MekanismAdvancement(METALLURGIC_INFUSER, "infused_alloy");
    public static final MekanismAdvancement REINFORCED_ALLOY = new MekanismAdvancement(INFUSED_ALLOY, "reinforced_alloy");
    public static final MekanismAdvancement ATOMIC_ALLOY = new MekanismAdvancement(REINFORCED_ALLOY, "atomic_alloy");
    public static final MekanismAdvancement PLUTONIUM = new MekanismAdvancement(ATOMIC_ALLOY, "plutonium");
    public static final MekanismAdvancement SPS = new MekanismAdvancement(PLUTONIUM, "sps");
    public static final MekanismAdvancement ANTIMATTER = new MekanismAdvancement(SPS, "antimatter");
    public static final MekanismAdvancement NUCLEOSYNTHESIZER = new MekanismAdvancement(ANTIMATTER, "nucleosynthesizer");
    public static final MekanismAdvancement POLONIUM = new MekanismAdvancement(ATOMIC_ALLOY, "polonium");
    public static final MekanismAdvancement QIO_DRIVE_ARRAY = new MekanismAdvancement(POLONIUM, "qio_drive_array");
    public static final MekanismAdvancement BASIC_QIO_DRIVE = new MekanismAdvancement(QIO_DRIVE_ARRAY, "basic_qio_drive");
    public static final MekanismAdvancement ADVANCED_QIO_DRIVE = new MekanismAdvancement(BASIC_QIO_DRIVE, "advanced_qio_drive");
    public static final MekanismAdvancement ELITE_QIO_DRIVE = new MekanismAdvancement(ADVANCED_QIO_DRIVE, "elite_qio_drive");
    public static final MekanismAdvancement ULTIMATE_QIO_DRIVE = new MekanismAdvancement(ELITE_QIO_DRIVE, "ultimate_qio_drive");
    public static final MekanismAdvancement QIO_DASHBOARD = new MekanismAdvancement(QIO_DRIVE_ARRAY, "qio_dashboard");
    public static final MekanismAdvancement PORTABLE_QIO_DASHBOARD = new MekanismAdvancement(QIO_DASHBOARD, "portable_qio_dashboard");
    public static final MekanismAdvancement TELEPORTATION_CORE = new MekanismAdvancement(ATOMIC_ALLOY, "teleportation_core");
    public static final MekanismAdvancement TELEPORTER = new MekanismAdvancement(TELEPORTATION_CORE, "teleporter");
    public static final MekanismAdvancement PORTABLE_TELEPORTER = new MekanismAdvancement(TELEPORTER, "portable_teleporter");
    public static final MekanismAdvancement QUANTUM_ENTANGLOPORTER = new MekanismAdvancement(TELEPORTATION_CORE, "quantum_entangloporter");
    public static final MekanismAdvancement BASIC_CONTROL = new MekanismAdvancement(METALLURGIC_INFUSER, "basic_control");
    public static final MekanismAdvancement ADVANCED_CONTROL = new MekanismAdvancement(BASIC_CONTROL, "advanced_control");
    public static final MekanismAdvancement ELITE_CONTROL = new MekanismAdvancement(ADVANCED_CONTROL, "elite_control");
    public static final MekanismAdvancement ULTIMATE_CONTROL = new MekanismAdvancement(ELITE_CONTROL, "ultimate_control");
    public static final MekanismAdvancement ROBIT = new MekanismAdvancement(ULTIMATE_CONTROL, "robit");
    //TODO: Set a skin on a robit, hidden advancement
    public static final MekanismAdvancement ROBIT_AESTHETICS = new MekanismAdvancement(ROBIT, "robit_aesthetics");
    public static final MekanismAdvancement DIGITAL_MINER = new MekanismAdvancement(ROBIT, "digital_miner");
    //TODO
    public static final MekanismAdvancement DICTIONARY = new MekanismAdvancement(DIGITAL_MINER, "dictionary");
    //TODO: preventing random holes in the ground since 2021
    public static final MekanismAdvancement STONE_GENERATOR = new MekanismAdvancement(DIGITAL_MINER, "stone_generator");
    public static final MekanismAdvancement DISASSEMBLER = new MekanismAdvancement(MATERIALS, "disassembler");
    public static final MekanismAdvancement MEKASUIT = new MekanismAdvancement(DISASSEMBLER, "mekasuit");
    public static final MekanismAdvancement UPGRADED_MEKASUIT = new MekanismAdvancement(MEKASUIT, "upgraded_mekasuit");

    public static final MekanismAdvancement ITEM_TRANSPORT = new MekanismAdvancement(INFUSED_ALLOY, "item_transport");
    public static final MekanismAdvancement RESTRICTIVE_ITEM_TRANSPORT = new MekanismAdvancement(ITEM_TRANSPORT, "restrictive_item_transport");
    public static final MekanismAdvancement DIVERSION_ITEM_TRANSPORT = new MekanismAdvancement(ITEM_TRANSPORT, "diversion_item_transport");
    public static final MekanismAdvancement SORTER = new MekanismAdvancement(ITEM_TRANSPORT, "logistical_sorter");

    public static final MekanismAdvancement FLUID_TRANSPORT = new MekanismAdvancement(INFUSED_ALLOY, "fluid_transport");
    public static final MekanismAdvancement CHEMICAL_TRANSPORT = new MekanismAdvancement(INFUSED_ALLOY, "chemical_transport");
    public static final MekanismAdvancement ENERGY_TRANSPORT = new MekanismAdvancement(INFUSED_ALLOY, "energy_transport");
    public static final MekanismAdvancement HEAT_TRANSPORT = new MekanismAdvancement(INFUSED_ALLOY, "heat_transport");

    public static final MekanismAdvancement CONFIGURATOR = new MekanismAdvancement(TODO, "configurator");
    public static final MekanismAdvancement NETWORK_READER = new MekanismAdvancement(TODO, "network_reader");

    public static final MekanismAdvancement ENRICHER = new MekanismAdvancement(TODO, "enricher");
    public static final MekanismAdvancement INFUSING_EFFICIENCY = new MekanismAdvancement(ENRICHER, "infusing_efficiency");

    //TODO: Security desk
    public static final MekanismAdvancement MACHINE_SECURITY = new MekanismAdvancement(TODO, "machine_security");

    //TODO: waste barrel
    public static final MekanismAdvancement WASTE_REMOVAL = new MekanismAdvancement(TODO, "waste_removal");

    public static final MekanismAdvancement PERSONAL_STORAGE = new MekanismAdvancement(TODO, "personal_storage");

    //TODO: Bins maybe name different
    public static final MekanismAdvancement SIMPLE_MASS_STORAGE = new MekanismAdvancement(TODO, "simple_mass_storage");

    //TODO - 1.19: Cardboard box
    public static final MekanismAdvancement MOVING_BLOCKS = new MekanismAdvancement(TODO, "moving_blocks");

    //TODO: Flamethrower
    public static final MekanismAdvancement PLAYING_WITH_FIRE = new MekanismAdvancement(TODO, "playing_with_fire");

    //TODO: Scuba gear
    public static final MekanismAdvancement BREATHING_ASSISTANCE = new MekanismAdvancement(TODO, "breathing_assistance");

    //TODO - 1.19: Add advancements for various machines? Not sure where is a good breaking point


}