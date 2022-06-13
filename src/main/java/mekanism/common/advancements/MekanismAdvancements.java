package mekanism.common.advancements;

public class MekanismAdvancements {

    private MekanismAdvancements() {
    }

    public static final MekanismAdvancement ROOT = new MekanismAdvancement("root", null);
    public static final MekanismAdvancement MATERIALS = new MekanismAdvancement("materials", ROOT);
    public static final MekanismAdvancement FLUID_TANK = new MekanismAdvancement("fluid_tank", ROOT);
    public static final MekanismAdvancement CHEMICAL_TANK = new MekanismAdvancement("chemical_tank", ROOT);
    public static final MekanismAdvancement FULL_CANTEEN = new MekanismAdvancement("full_canteen", MATERIALS);
    public static final MekanismAdvancement METALLURGIC_INFUSER = new MekanismAdvancement("metallurgic_infuser", MATERIALS);
    public static final MekanismAdvancement STEEL_INGOT = new MekanismAdvancement("steel_ingot", METALLURGIC_INFUSER);
    public static final MekanismAdvancement STEEL_CASING = new MekanismAdvancement("steel_casing", STEEL_INGOT);
    public static final MekanismAdvancement INFUSED_ALLOY = new MekanismAdvancement("infused_alloy", METALLURGIC_INFUSER);
    public static final MekanismAdvancement REINFORCED_ALLOY = new MekanismAdvancement("reinforced_alloy", INFUSED_ALLOY);
    public static final MekanismAdvancement ATOMIC_ALLOY = new MekanismAdvancement("atomic_alloy", REINFORCED_ALLOY);
    public static final MekanismAdvancement PLUTONIUM = new MekanismAdvancement("plutonium", ATOMIC_ALLOY);
    public static final MekanismAdvancement SPS = new MekanismAdvancement("sps", PLUTONIUM);
    public static final MekanismAdvancement ANTIMATTER = new MekanismAdvancement("antimatter", SPS);
    public static final MekanismAdvancement NUCLEOSYNTHESIZER = new MekanismAdvancement("nucleosynthesizer", ANTIMATTER);
    public static final MekanismAdvancement POLONIUM = new MekanismAdvancement("polonium", ATOMIC_ALLOY);
    public static final MekanismAdvancement QIO_DRIVE_ARRAY = new MekanismAdvancement("qio_drive_array", POLONIUM);
    public static final MekanismAdvancement BASIC_QIO_DRIVE = new MekanismAdvancement("basic_qio_drive", QIO_DRIVE_ARRAY);
    public static final MekanismAdvancement ADVANCED_QIO_DRIVE = new MekanismAdvancement("advanced_qio_drive", BASIC_QIO_DRIVE);
    public static final MekanismAdvancement ELITE_QIO_DRIVE = new MekanismAdvancement("elite_qio_drive", ADVANCED_QIO_DRIVE);
    public static final MekanismAdvancement ULTIMATE_QIO_DRIVE = new MekanismAdvancement("ultimate_qio_drive", ELITE_QIO_DRIVE);
    public static final MekanismAdvancement QIO_DASHBOARD = new MekanismAdvancement("qio_dashboard", QIO_DRIVE_ARRAY);
    public static final MekanismAdvancement PORTABLE_QIO_DASHBOARD = new MekanismAdvancement("portable_qio_dashboard", QIO_DASHBOARD);
    public static final MekanismAdvancement TELEPORTATION_CORE = new MekanismAdvancement("teleportation_core", ATOMIC_ALLOY);
    public static final MekanismAdvancement TELEPORTER = new MekanismAdvancement("teleporter", TELEPORTATION_CORE);
    public static final MekanismAdvancement PORTABLE_TELEPORTER = new MekanismAdvancement("portable_teleporter", TELEPORTER);
    public static final MekanismAdvancement QUANTUM_ENTANGLOPORTER = new MekanismAdvancement("quantum_entangloporter", TELEPORTATION_CORE);
    public static final MekanismAdvancement BASIC_CONTROL = new MekanismAdvancement("basic_control", METALLURGIC_INFUSER);
    public static final MekanismAdvancement ADVANCED_CONTROL = new MekanismAdvancement("advanced_control", BASIC_CONTROL);
    public static final MekanismAdvancement ELITE_CONTROL = new MekanismAdvancement("elite_control", ADVANCED_CONTROL);
    public static final MekanismAdvancement ULTIMATE_CONTROL = new MekanismAdvancement("ultimate_control", ELITE_CONTROL);
    public static final MekanismAdvancement ROBIT = new MekanismAdvancement("robit", ULTIMATE_CONTROL);
    public static final MekanismAdvancement DIGITAL_MINER = new MekanismAdvancement("digital_miner", ROBIT);
    public static final MekanismAdvancement DISASSEMBLER = new MekanismAdvancement("disassembler", MATERIALS);
    public static final MekanismAdvancement MEKASUIT = new MekanismAdvancement("mekasuit", DISASSEMBLER);
    public static final MekanismAdvancement UPGRADED_MEKASUIT = new MekanismAdvancement("upgraded_mekasuit", MEKASUIT);
}