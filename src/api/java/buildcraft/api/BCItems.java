package buildcraft.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

/** Stores all of BuildCraft's items, from all of its modules. If any of them have been disabled by the user (or it the
 * module is not installed) then they will be null. This is the equivalent of {@link Items} */
public class BCItems {
    private static final boolean DEBUG = BCDebugging.shouldDebugLog("api.items");

    // The (optional) items in BC LIB, and has other rules for when they are enabled.
    public static final Item LIB_GUIDE;
    public static final Item LIB_DEBUGGER;

    // BC Core
    public static final Item CORE_WRENCH;
    public static final Item CORE_DIAMOND_SHARD;
    public static final Item CORE_LIST;
    public static final Item CORE_MAP_LOCATION;
    public static final Item CORE_PAINTBRUSH;
    public static final Item CORE_GEAR_WOOD;
    public static final Item CORE_GEAR_STONE;
    public static final Item CORE_GEAR_IRON;
    public static final Item CORE_GEAR_GOLD;
    public static final Item CORE_GEAR_DIAMOND;
    public static final Item CORE_MARKER_CONNECTOR;
    public static final Item CORE_GOGGLES;

    // BC Builders
    public static final Item BUILDERS_SINGLE_SCHEMATIC;
    public static final Item BUILDERS_SNAPSHOT;

    // BC Energy
    public static final Item ENERGY_GLOB_OF_OIL;

    // BC Factory
    public static final Item FACTORY_PLASTIC_SHEET;

    // BC Robotics
    public static final Item ROBOTICS_REDSTONE_BOARD;
    public static final Item ROBOTICS_ROBOT;
    public static final Item ROBOTICS_ROBOT_GOGGLES;
    public static final Item ROBOTICS_PLUGGABLE_ROBOT_STATION;

    // BC Silicon
    public static final Item SILICON_REDSTONE_CHIPSET;

    // BC Transport
    public static final Item TRANSPORT_WATERPROOF;
    public static final Item TRANSPORT_GATE_COPIER;
    public static final Item TRANSPORT_PLUGGABLE_GATE;
    public static final Item TRANSPORT_PLUGGABLE_WIRE;
    public static final Item TRANSPORT_PLUGGABLE_BLOCKER;
    public static final Item TRANSPORT_PLUGGABLE_LENS;
    public static final Item TRANSPORT_PLUGGABLE_POWER_ADAPTOR;
    public static final Item TRANSPORT_PLUGGABLE_FACADE;

    public static final Item TRANSPORT_PIPE_WOOD_ITEM;
    public static final Item TRANSPORT_PIPE_WOOD_FLUID;
    public static final Item TRANSPORT_PIPE_WOOD_POWER;

    // ... a few more needed...

    public static final Item TRANSPORT_PIPE_DIAMOND_ITEM;

    // Set of items scanned
    private static final Set<String> SCANNED = DEBUG ? new HashSet<>() : null;

    static {
        if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("Accessed BC items too early! You can only use them from init onwards!");
        }
        final String lib = "lib";
        LIB_GUIDE = getRegisteredItem(lib, "guide");
        LIB_DEBUGGER = getRegisteredItem(lib, "debugger");

        final String core = "core";
        CORE_WRENCH = getRegisteredItem(core, "wrench");
        CORE_DIAMOND_SHARD = getRegisteredItem(core, "diamond_shard");
        CORE_LIST = getRegisteredItem(core, "list");
        CORE_MAP_LOCATION = getRegisteredItem(core, "map_location");
        CORE_PAINTBRUSH = getRegisteredItem(core, "paintbrush");
        CORE_GEAR_WOOD = getRegisteredItem(core, "gear_wood");
        CORE_GEAR_STONE = getRegisteredItem(core, "gear_stone");
        CORE_GEAR_IRON = getRegisteredItem(core, "gear_iron");
        CORE_GEAR_GOLD = getRegisteredItem(core, "gear_gold");
        CORE_GEAR_DIAMOND = getRegisteredItem(core, "gear_diamond");
        CORE_MARKER_CONNECTOR = getRegisteredItem(core, "marker_connector");
        CORE_GOGGLES = getRegisteredItem(core, "goggles");

        final String builders = "builders";
        BUILDERS_SINGLE_SCHEMATIC = getRegisteredItem(builders, "single_schematic");
        BUILDERS_SNAPSHOT = getRegisteredItem(builders, "snapshot");

        final String energy = "energy";
        ENERGY_GLOB_OF_OIL = getRegisteredItem(energy, "glob_oil");

        final String factory = "factory";
        FACTORY_PLASTIC_SHEET = getRegisteredItem(factory, "plastic_sheet");

        final String robotics = "robotics";
        ROBOTICS_REDSTONE_BOARD = getRegisteredItem(robotics, "redstone_board");
        ROBOTICS_ROBOT = getRegisteredItem(robotics, "robot");
        ROBOTICS_PLUGGABLE_ROBOT_STATION = getRegisteredItem(robotics, "robot_station");
        ROBOTICS_ROBOT_GOGGLES = getRegisteredItem(robotics, "robot_goggles");

        final String silicon = "silicon";
        SILICON_REDSTONE_CHIPSET = getRegisteredItem(silicon, "redstone_chipset");

        final String transport = "transport";
        TRANSPORT_WATERPROOF = getRegisteredItem(transport, "waterproof");
        TRANSPORT_GATE_COPIER = getRegisteredItem(transport, "gate_copier");
        TRANSPORT_PLUGGABLE_GATE = getRegisteredItem(transport, "plug_gate");
        TRANSPORT_PLUGGABLE_WIRE = getRegisteredItem(transport, "plug_wire");
        TRANSPORT_PLUGGABLE_BLOCKER = getRegisteredItem(transport, "plug_blocker");
        TRANSPORT_PLUGGABLE_LENS = getRegisteredItem(transport, "plug_lens");
        TRANSPORT_PLUGGABLE_FACADE = getRegisteredItem(transport, "plug_facade");
        TRANSPORT_PLUGGABLE_POWER_ADAPTOR = getRegisteredItem(transport, "plug_power_adapter");
        TRANSPORT_PIPE_WOOD_ITEM = getRegisteredItem(transport, "pipe_wood_item");
        TRANSPORT_PIPE_WOOD_FLUID = getRegisteredItem(transport, "pipe_wood_fluid");
        TRANSPORT_PIPE_WOOD_POWER = getRegisteredItem(transport, "pipe_wood_power");

        TRANSPORT_PIPE_DIAMOND_ITEM = getRegisteredItem(transport, "pipe_diamond_item");

        if (DEBUG) {
            for (Item item : Item.REGISTRY) {
                ResourceLocation id = item.getRegistryName();
                if (id.getResourceDomain().startsWith("buildcraft")) {
                    if (!SCANNED.contains(id.toString())) {
                        BCLog.logger.warn("[api.items] Found an item " + id.toString() + " that was not registered with the API! Is this a bug?");
                    }
                }
            }
        }
    }

    private static Item getRegisteredItem(String module, String regName) {
        String modid = "buildcraft" + module;
        Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, regName));
        if (item != null) {
            if (DEBUG) {
                BCLog.logger.info("[api.items] Found the item " + regName + " from the module " + module);
                SCANNED.add(modid + ":" + regName);
            }
            return item;
        }
        if (DEBUG) {
            if (Loader.isModLoaded(modid)) {
                BCLog.logger.info("[api.items] Did not find the item " + regName + " despite the appropriate mod being loaded (" + modid + ")");
            } else {
                BCLog.logger.info("[api.items] Did not find the item " + regName + " probably because the mod is not loaded (" + modid + ")");
            }
        }

        return null;
    }
}
