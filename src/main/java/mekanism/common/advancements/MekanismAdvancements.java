package mekanism.common.advancements;

import javax.annotation.Nullable;
import mekanism.common.Mekanism;

public class MekanismAdvancements {

    private MekanismAdvancements() {
    }
    //TODO - 1.19: Re-organize this class once we come up with the actual order advancements will be in

    private static MekanismAdvancement advancement(@Nullable MekanismAdvancement parent, String name) {
        return new MekanismAdvancement(parent, Mekanism.rl(name));
    }

    public static final MekanismAdvancement ROOT = advancement(null, "root");
    public static final MekanismAdvancement MATERIALS = advancement(ROOT, "materials");
    public static final MekanismAdvancement FULL_CANTEEN = advancement(MATERIALS, "full_canteen");
    public static final MekanismAdvancement METALLURGIC_INFUSER = advancement(MATERIALS, "metallurgic_infuser");
    public static final MekanismAdvancement STEEL_INGOT = advancement(METALLURGIC_INFUSER, "steel_ingot");
    public static final MekanismAdvancement STEEL_CASING = advancement(STEEL_INGOT, "steel_casing");

    public static final MekanismAdvancement INFUSED_ALLOY = advancement(METALLURGIC_INFUSER, "infused_alloy");

    public static final MekanismAdvancement FLUID_TANK = advancement(INFUSED_ALLOY, "fluid_tank");
    public static final MekanismAdvancement CHEMICAL_TANK = advancement(INFUSED_ALLOY, "chemical_tank");
    public static final MekanismAdvancement WASTE_REMOVAL = advancement(CHEMICAL_TANK, "waste_removal");

    public static final MekanismAdvancement REINFORCED_ALLOY = advancement(INFUSED_ALLOY, "reinforced_alloy");

    public static final MekanismAdvancement ATOMIC_ALLOY = advancement(REINFORCED_ALLOY, "atomic_alloy");

    public static final MekanismAdvancement PLUTONIUM = advancement(WASTE_REMOVAL, "plutonium");
    public static final MekanismAdvancement SPS = advancement(PLUTONIUM, "sps");
    public static final MekanismAdvancement ANTIMATTER = advancement(SPS, "antimatter");
    public static final MekanismAdvancement NUCLEOSYNTHESIZER = advancement(ANTIMATTER, "nucleosynthesizer");
    public static final MekanismAdvancement POLONIUM = advancement(WASTE_REMOVAL, "polonium");
    public static final MekanismAdvancement QIO_DRIVE_ARRAY = advancement(POLONIUM, "qio_drive_array");
    public static final MekanismAdvancement BASIC_QIO_DRIVE = advancement(QIO_DRIVE_ARRAY, "basic_qio_drive");
    public static final MekanismAdvancement ADVANCED_QIO_DRIVE = advancement(BASIC_QIO_DRIVE, "advanced_qio_drive");
    public static final MekanismAdvancement ELITE_QIO_DRIVE = advancement(ADVANCED_QIO_DRIVE, "elite_qio_drive");
    public static final MekanismAdvancement ULTIMATE_QIO_DRIVE = advancement(ELITE_QIO_DRIVE, "ultimate_qio_drive");
    public static final MekanismAdvancement QIO_DASHBOARD = advancement(QIO_DRIVE_ARRAY, "qio_dashboard");
    public static final MekanismAdvancement PORTABLE_QIO_DASHBOARD = advancement(QIO_DASHBOARD, "portable_qio_dashboard");
    public static final MekanismAdvancement TELEPORTATION_CORE = advancement(ATOMIC_ALLOY, "teleportation_core");
    public static final MekanismAdvancement TELEPORTER = advancement(TELEPORTATION_CORE, "teleporter");
    public static final MekanismAdvancement PORTABLE_TELEPORTER = advancement(TELEPORTER, "portable_teleporter");
    public static final MekanismAdvancement QUANTUM_ENTANGLOPORTER = advancement(TELEPORTATION_CORE, "quantum_entangloporter");
    public static final MekanismAdvancement BASIC_CONTROL_CIRCUIT = advancement(METALLURGIC_INFUSER, "basic_control_circuit");
    public static final MekanismAdvancement ADVANCED_CONTROL_CIRCUIT = advancement(INFUSED_ALLOY, "advanced_control_circuit");
    public static final MekanismAdvancement ELITE_CONTROL_CIRCUIT = advancement(REINFORCED_ALLOY, "elite_control_circuit");
    public static final MekanismAdvancement ULTIMATE_CONTROL_CIRCUIT = advancement(ATOMIC_ALLOY, "ultimate_control_circuit");
    public static final MekanismAdvancement ROBIT = advancement(ATOMIC_ALLOY, "robit");
    public static final MekanismAdvancement ROBIT_AESTHETICS = advancement(ROBIT, "robit_aesthetics");
    public static final MekanismAdvancement DIGITAL_MINER = advancement(ROBIT, "digital_miner");
    public static final MekanismAdvancement DICTIONARY = advancement(DIGITAL_MINER, "dictionary");
    public static final MekanismAdvancement STONE_GENERATOR = advancement(DIGITAL_MINER, "stone_generator");


    public static final MekanismAdvancement DISASSEMBLER = advancement(ATOMIC_ALLOY, "disassembler");
    public static final MekanismAdvancement MEKASUIT = advancement(DISASSEMBLER, "mekasuit");
    public static final MekanismAdvancement UPGRADED_MEKASUIT = advancement(MEKASUIT, "upgraded_mekasuit");

    //TODO
    public static final MekanismAdvancement ITEM_TRANSPORT = advancement(STEEL_INGOT, "item_transport");
    public static final MekanismAdvancement RESTRICTIVE_ITEM_TRANSPORT = advancement(ITEM_TRANSPORT, "restrictive_item_transport");
    public static final MekanismAdvancement DIVERSION_ITEM_TRANSPORT = advancement(ITEM_TRANSPORT, "diversion_item_transport");
    public static final MekanismAdvancement SORTER = advancement(ITEM_TRANSPORT, "logistical_sorter");

    public static final MekanismAdvancement FLUID_TRANSPORT = advancement(STEEL_INGOT, "fluid_transport");
    public static final MekanismAdvancement CHEMICAL_TRANSPORT = advancement(STEEL_INGOT, "chemical_transport");
    public static final MekanismAdvancement ENERGY_TRANSPORT = advancement(STEEL_INGOT, "energy_transport");
    public static final MekanismAdvancement HEAT_TRANSPORT = advancement(STEEL_INGOT, "heat_transport");

    /*public static final MekanismAdvancement CONFIGURATOR = advancement(TODO, "configurator");
    public static final MekanismAdvancement NETWORK_READER = advancement(TODO, "network_reader");

    public static final MekanismAdvancement ENRICHER = advancement(TODO, "enricher");
    public static final MekanismAdvancement INFUSING_EFFICIENCY = advancement(ENRICHER, "infusing_efficiency");

    //TODO: Security desk
    public static final MekanismAdvancement MACHINE_SECURITY = advancement(TODO, "machine_security");*/

    /*public static final MekanismAdvancement PERSONAL_STORAGE = advancement(TODO, "personal_storage");

    //TODO: Bins maybe name different
    public static final MekanismAdvancement SIMPLE_MASS_STORAGE = advancement(TODO, "simple_mass_storage");

    //TODO - 1.19: Cardboard box
    public static final MekanismAdvancement MOVING_BLOCKS = advancement(TODO, "moving_blocks");

    //TODO: Flamethrower
    public static final MekanismAdvancement PLAYING_WITH_FIRE = advancement(TODO, "playing_with_fire");

    //TODO: Scuba gear
    public static final MekanismAdvancement BREATHING_ASSISTANCE = advancement(TODO, "breathing_assistance");*/

    //TODO - 1.19: Add advancements for various machines? Not sure where is a good breaking point
    //TODO - 1.19: Advancements:
    // Geiger Counter
    // Dosimeter
    // Formulaic Assemblicator (atomated crafting)
    // Gauge dropper
    // installers?
    // configuration card
    // qio importer exporter and level emitter if not already there


}