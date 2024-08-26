package mekanism.client.recipe_viewer.alias;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.Mekanism;
import net.minecraft.Util;

@NothingNullByDefault
public enum MekanismAliases implements IAliasedTranslation {
    BIN_DRAWER("bin.drawer", "Drawer"),
    CHUNK_LOADER("chunk_loader", "Chunk Loader"),
    ITEM_CHARGER("item_charger", "Item Charger"),
    PERSONAL_BACKPACK("personal_storage.backpack", "Backpack"),
    REDSTONE_CONTROL("redstone_control", "Redstone Control"),
    ROUND_ROBIN("round_robin", "Round-Robin"),
    TAG_CONVERTER("tag_converter", "Tag Converter"),
    //Storage Block
    BLOCK_BRONZE("block.bronze", "Bronze Block"),
    BLOCK_CHARCOAL("block.charcoal", "Charcoal Block"),
    BLOCK_STEEL("block.steel", "Steel Block"),
    BLOCK_FLUORITE("block.fluorite", "Fluorite Block"),
    //Canteen
    CANTEEN_EDIBLE("canteen.edible", "Edible"),
    CANTEEN_FOOD_STORAGE("canteen.storage", "Food Storage"),
    //Crafting
    PORTABLE_CRAFTING_TABLE("crafting.portable", "Portable Crafting Table"),
    CRAFTING_PATTERN("crafting.pattern", "Crafting Pattern"),
    AUTO_CRAFTER("crafting.auto", "Auto-Crafter"),
    //Flight
    FLIGHT("flight", "Flight"),
    CREATIVE_FLIGHT("flight.creative", "Creative Flight"),
    //Chemicals
    ETHENE_ETHYLENE("ethene.ethylene", "Ethylene"),
    //Machine
    MACHINE_SMELTING("machine.smelting", "Smelting"),
    MACHINE_ENRICHING("machine.enriching", "Enriching"),
    MACHINE_CRUSHING("machine.crushing", "Crushing"),
    MACHINE_COMPRESSING("machine.compressing", "Compressing"),
    MACHINE_COMBINING("machine.combining", "Combining"),
    MACHINE_PURIFYING("machine.purifying", "Purifying"),
    MACHINE_INJECTING("machine.injecting", "Injecting"),
    MACHINE_INFUSING("machine.infusing", "Infusing"),
    MACHINE_SAWING("machine.sawing", "Sawing"),
    //Fluidic Plenisher
    PLENISHER_PLACER("plenisher.placer", "Fluid Placer"),
    PLENISHER_REVERSE("plenisher.reverse", "Reverse Pump"),
    //Rotary
    ROTARY_DECONDENSENTRATOR("rotary.decondensentrator", "Rotary Decondensentrator"),
    ROTARY_CHEMICAL_TO_FLUID("rotary.chemical_to_fluid", "Chemical To Fluid"),
    ROTARY_GAS_TO_FLUID("rotary.gas_to_fluid", "Gas To Fluid"),
    ROTARY_FLUID_TO_CHEMICAL("rotary.fluid_to_chemical", "Fluid To Chemical"),
    ROTARY_FLUID_TO_GAS("rotary.fluid_to_gas", "Fluid To Gas"),
    //Quantum Entangloporter
    QE_TESSERACT("qe.tesseract", "Tesseract"),
    QE_ENDER_TANK("qe.ender.tank", "Ender Tank"),
    //Gear
    FREE_RUNNER_LONG_FALL("free_runner.long_fall", "Long Fall Boots"),
    FREE_RUNNER_FALL_PROTECTION("free_runner.fall_protection", "Fall Protection"),
    AUTO_STEP("auto_step", "Auto-Step"),
    STEP_ASSIST("step_assist", "Step Assist"),
    RADIATION_PROTECTION("radiation_protection", "Radiation Protection"),
    MEKA_SUIT_POWER_ARMOR("mekasuit", "Power Armor"),
    //Tools
    TOOL_MULTI("tool.multi", "Multi-Tool"),
    TOOL_AXE("tool.axe", "Axe"),
    TOOL_HOE("tool.hoe", "Hoe"),
    TOOL_PICKAXE("tool.pickaxe", "Pickaxe"),
    TOOL_SHOVEL("tool.shovel", "Shovel"),
    TOOL_SWORD("tool.sword", "Sword"),
    TOOL_WEAPON("tool.weapon", "Weapon"),
    TOOL_HAMMER("tool.hammer", "Hammer"),
    TOOL_DIAGNOSTIC("tool.diagnostic", "Diagnostic Tool"),
    TOOL_WRENCH("tool.wrench", "Wrench"),
    //Storage
    STORAGE_PORTABLE("storage.portable", "Portable Storage"),
    ENERGY_STORAGE("storage.energy", "Energy Storage"),
    ENERGY_STORAGE_BATTERY("storage.energy.battery", "Battery"),
    FLUID_STORAGE("storage.fluid", "Fluid Storage"),
    ITEM_STORAGE("storage.item", "Item Storage"),
    CHEMICAL_STORAGE("storage.chemical", "Chemical Storage"),
    GAS_STORAGE("storage.gas", "Gas Storage"),
    INFUSE_TYPE_STORAGE("storage.infuse_type", "Infuse Type Storage"),
    INFUSION_STORAGE("storage.infusion", "Infusion Storage"),
    PIGMENT_STORAGE("storage.pigment", "Pigment Storage"),
    SLURRY_STORAGE("storage.slurry", "Slurry Storage"),
    //Transfer
    ENERGY_TRANSFER("transfer.energy", "Energy Transfer"),
    ENERGY_THROUGHPUT("transfer.energy.throughput", "Energy Throughput"),
    HEAT_TRANSFER("transfer.heat", "Heat Transfer"),
    FLUID_TRANSFER("transfer.fluid", "Fluid Transfer"),
    ITEM_TRANSFER("transfer.item", "Item Transfer"),
    CHEMICAL_TRANSFER("transfer.chemical", "Chemical Transfer"),
    GAS_TRANSFER("transfer.gas", "Gas Transfer"),
    INFUSE_TYPE_TRANSFER("transfer.infuse_type", "Infuse Type Transfer"),
    INFUSION_TRANSFER("transfer.infusion", "Infusion Transfer"),
    PIGMENT_TRANSFER("transfer.pigment", "Pigment Transfer"),
    SLURRY_TRANSFER("transfer.slurry", "Slurry Transfer"),
    //Transmitters (Relies on the assumption the transmitters have the corresponding transfer attached so then EMI will natively allow the pairing of Type + Pipe etc
    TRANSMITTER("transmitter", "Transmitter"),
    TRANSMITTER_CONDUIT("transmitter.conduit", "Conduit"),
    TRANSMITTER_PIPE("transmitter.pipe", "Pipe"),
    TRANSMITTER_TUBE("transmitter.tube", "Tube"),
    //Voiding
    STORAGE_TRASHCAN("storage.trash", "Trashcan"),
    STORAGE_VOID("storage.void", "Void's Input"),
    //QIO
    QIO_FULL("qio.full", "Quantum Item Orchestration"),
    QIO_DRIVE_CELL("qio.drive.cell", "Item Cell"),
    QIO_DRIVE_DISK("qio.drive.disk", "Item Disk"),
    QIO_ADAPTER_EMITTER("qio.adapter.emitter", "QIO Level Emitter"),
    QIO_DASHBOARD_TERMINAL("qio.dashboard.terminal", "QIO Crafting Terminal"),//Note: This will catch the case of QIO Terminal as well
    QIO_DASHBOARD_GRID("qio.dashboard.grid", "QIO Crafting Grid"),//Note: This will catch the case of QIO Grid as well
    QIO_DASHBOARD_WIRELESS_TERMINAL("qio.dashboard.wireless", "Wireless QIO Crafting Terminal"),//Note: We don't need a wireless grid one, as EMI search merged appropriately
    QIO_DRIVE_BAY("qio.drive.bay", "QIO Drive Bay"),
    //Upgrades
    UPGRADE_AUGMENT("upgrade.augment", "Augment"),
    UPGRADE_OVERCLOCK("upgrade.overclock", "Overclocker"),
    UPGRADE_MUFFLER("upgrade.muffler", "Sound Muffler"),
    UPGRADE_HOLE_FILLER("upgrade.hole_filler", "Hole Filler"),
    INSTALLER_FACTORY("installer.factory", "Factory Installer"),
    INSTALLER_UPGRADE("installer.upgrade", "Machine Upgrade"),
    //Units
    UNIT_INSTALLER("unit.installer", "Unit Installer"),
    UNIT_INSTALLER_MODULE("unit.installer.module", "Module Installer"),
    UNIT_DAMAGE("unit.damage", "Damage"),
    UNIT_FEEDER("unit.feeder", "Auto-Feeder"),
    UNIT_AOE("unit.aoe", "AOE"),
    UNIT_AOE_LONG("unit.aoe.long", "Area of Effect"),
    UNIT_DIG_SPEED("unit.speed.dig", "Dig Speed"),
    UNIT_HYDROSTATIC_SPEED("unit.speed.hydrostatic", "Swim Speed"),
    //Multiblock
    BOILER_COMPONENT("multiblock.boiler", "Boiler Multiblock Component"),
    EVAPORATION_COMPONENT("multiblock.evaporation", "Thermal Evaporation Plant Multiblock Component"),
    MATRIX_COMPONENT("multiblock.matrix", "Induction Matrix Multiblock Component"),
    SPS_COMPONENT("multiblock.sps", "SPS Multiblock Component"),
    SPS_FULL_COMPONENT("multiblock.sps.full", "Supercritical Phase Shifter Multiblock Component"),
    TANK_COMPONENT("multiblock.tank", "Dynamic Tank Multiblock Component");

    private final String key;
    private final String alias;

    MekanismAliases(String path, String alias) {
        this.key = Util.makeDescriptionId("alias", Mekanism.rl(path));
        this.alias = alias;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String getAlias() {
        return alias;
    }
}