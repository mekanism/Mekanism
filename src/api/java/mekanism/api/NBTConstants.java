package mekanism.api;

/**
 * Class for storing constants that are used in various NBT related storage, to reduce the chances of typos
 */
public final class NBTConstants {

    private NBTConstants() {
    }

    //Ones that also are used for interacting with forge/vanilla
    public static final String BASE = "Base";
    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    public static final String COUNT = "Count";
    public static final String CUSTOM_NAME = "CustomName";
    public static final String ENCHANTMENTS = "Enchantments";
    public static final String ID = "id";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";

    //Server to Client specific sync NBT tags
    public static final String ACTIVE = "active";
    public static final String CLIENT_NEXT = "clientNext";
    public static final String CLIENT_PREVIOUS = "clientPrevious";
    public static final String COMPLEX = "complex";
    public static final String CURRENT_ACCEPTORS = "acceptors";
    public static final String CURRENT_CONNECTIONS = "connections";
    public static final String HAS_STRUCTURE = "hasStructure";
    public static final String HEIGHT = "height";
    public static final String INVENTORY_ID = "inventoryID";
    public static final String LOWER_VOLUME = "lowerVolume";
    public static final String MUFFLING_COUNT = "muffling";
    public static final String NETWORK = "network";
    public static final String OWNER_NAME = "ownerName";
    public static final String RENDERING = "rendering";
    public static final String RENDER_LOCATION = "renderLocation";
    public static final String RENDER_Y = "renderY";
    public static final String ROTATION = "rotation";
    public static final String SCALE = "scale";
    public static final String SCALE_ALT = "scaleAlt";
    public static final String SCALE_ALT_2 = "scale2";
    public static final String SCALE_ALT_3 = "scale3";
    public static final String SOUND_SCALE = "soundScale";
    public static final String TAG = "tag";
    public static final String VALVE = "valve";
    public static final String VOLUME = "volume";

    //Generic constants
    public static final String ACTIVE_COOLED = "activeCooled";
    public static final String ACTIVE_NODES = "activeNodes";
    public static final String ACTIVE_STATE = "activeState";
    public static final String ALLOW_DEFAULT = "allowDefault";
    public static final String AGE = "age";
    public static final String AMOUNT = "amount";
    public static final String AUTO = "auto";
    public static final String BLADES = "blades";
    public static final String BLOCK_STATE = "blockState";
    public static final String BOXED_CHEMICAL = "boxedChemical";
    public static final String BUCKET_MODE = "bucketMode";
    public static final String BURNING = "burning";
    public static final String BURN_TIME = "burnTime";
    public static final String CACHE = "cache";
    public static final String CHANCE = "chance";
    public static final String CHANNEL = "channel";
    public static final String CHEMICAL_TYPE = "chemicalType";
    public static final String CHUNK_SET = "chunkSet";
    public static final String COILS = "coils";
    public static final String COLOR = "color";
    public static final String COMPONENT_CONFIG = "componentConfig";
    public static final String COMPONENT_EJECTOR = "componentEjector";
    public static final String COMPONENT_FREQUENCY = "componentFrequency";
    public static final String COMPONENT_SECURITY = "componentSecurity";
    public static final String COMPONENT_UPGRADE = "componentUpgrade";
    public static final String CONFIG = "config";
    public static final String CONNECTION = "connection";
    public static final String CONTAINER = "Container";
    public static final String CONTROL_TYPE = "controlType";
    public static final String COULD_OPERATE = "couldOperate";
    public static final String CURRENT_REDSTONE = "currentRedstone";
    public static final String DATA = "data";
    public static final String DATA_NAME = "dataName";
    public static final String DATA_TYPE = "dataType";
    public static final String DELAY = "delay";
    public static final String DIMENSION = "dimension";
    public static final String DISABLED = "disabled";
    public static final String DRAINING = "draining";
    public static final String DRIVES = "drives";
    public static final String DUMP_LEFT = "dumpLeft";
    public static final String DUMP_MODE = "dumping";
    public static final String DUMP_RIGHT = "dumpRight";
    public static final String EDIT_MODE = "editMode";
    public static final String EJECT = "eject";
    /**
     * @since 10.3.6
     */
    public static final String ENABLED = "enabled";
    public static final String ENERGY_CONTAINERS = "EnergyContainers";
    public static final String ENERGY_STORED = "energy";
    public static final String ENERGY_USAGE = "energyUsage";
    public static final String FILLING = "filling";
    public static final String FILTER = "filter";
    public static final String FILTERS = "filters";
    public static final String FINISHED = "finished";
    public static final String FLUID_STORED = "fluid";
    public static final String FLUID_TANKS = "FluidTanks";
    public static final String FOLLOW = "follow";
    public static final String FREQUENCY = "frequency";
    public static final String FREQUENCY_LIST = "freqList";
    public static final String FROM_RECIPE = "fromRecipe";
    public static final String ASSEMBLIES = "assemblies";
    public static final String FUZZY_MODE = "fuzzyMode";
    public static final String GAS_NAME = "gasName";
    public static final String GAS_STORED = "gas";
    public static final String GAS_STORED_ALT = "gas1";
    public static final String GAS_STORED_ALT_2 = "gas2";
    public static final String GAS_TANKS = "GasTanks";
    public static final String HANDLE_SOUND = "handleSound";
    public static final String HEAT_CAPACITORS = "HeatCapacitors";
    public static final String HEAT_CAPACITY = "heatCapacity";
    public static final String HEAT_STORED = "heat";
    public static final String HOME_LOCATION = "homeLocation";
    public static final String IDLE_DIR = "idleDir";
    public static final String INDEX = "index";
    public static final String INFUSE_TYPE_NAME = "infuseTypeName";
    public static final String INFUSE_TYPE_STORED = "infuseType";
    public static final String INFUSION_TANKS = "InfusionTanks";
    public static final String INJECTION_RATE = "injectionRate";
    public static final String INVALID = "invalid";
    public static final String INVERSE = "inverse";
    public static final String INVERSE_REQUIRES_REPLACE = "inverseReplace";
    public static final String ITEM = "Item";
    public static final String ITEMS = "Items";
    public static final String LAST_FIRED = "lastFired";
    public static final String LAST_PROCESSED = "lastProcessed";
    public static final String LATCHED = "latched";
    public static final String LEVEL = "level";
    public static final String LOCK_STACK = "lockStack";
    public static final String LOGIC_TYPE = "logicType";
    public static final String MAGNITUDE = "magnitude";
    public static final String MAIN = "main";
    public static final String MAX = "max";
    public static final String MAX_BURN_TIME = "maxBurnTime";
    public static final String MEK_DATA = "mekData";
    public static final String MELTDOWNS = "meltdowns";
    public static final String MIN = "min";
    public static final String MODE = "mode";
    public static final String MODID = "modID";
    public static final String MODULES = "modules";
    public static final String NAME = "name";
    public static final String NUM_POWERING = "numPowering";
    public static final String ORIGINAL_LOCATION = "originalLocation";
    public static final String OUTPUT_MODE = "outputMode";
    /**
     * @since 10.3.9
     */
    public static final String OVERFLOW = "overflow";
    public static final String OVERRIDE = "override";
    public static final String OWNER_UUID = "owner";
    public static final String PARTIAL_WASTE = "partialWaste";
    public static final String PATH_TYPE = "pathType";
    /** @since 10.4.0 */
    public static final String PERSONAL_STORAGE_ID = "personalStorageId";
    public static final String PICKUP_DROPS = "dropPickup";
    public static final String PIGMENT_NAME = "pigmentName";
    public static final String PIGMENT_STORED = "pigment";
    public static final String PIGMENT_TANKS = "PigmentTanks";
    public static final String PLASMA_TEMP = "plasmaTemp";
    public static final String POSITION = "position";
    public static final String PROCESSED = "processed";
    public static final String PROGRESS = "progress";
    public static final String PUBLIC_FREQUENCY = "publicFreq";
    public static final String PULL = "pull";
    public static final String PULSE = "pulse";
    public static final String SKIN = "skin";
    public static final String QIO_ITEM_MAP = "qioItemMap";
    public static final String QIO_META_COUNT = "qioMetaCount";
    public static final String QIO_META_TYPES = "qioMetaTypes";
    public static final String RADIATION = "radiation";
    public static final String RADIATION_LIST = "radList";
    public static final String RADIUS = "radius";
    public static final String REACTOR_DAMAGE = "reactorDamage";
    public static final String RECEIVED_COORDS = "receivedCoords";
    public static final String RECURRING_NODES = "recurringNodes";
    public static final String REDSTONE = "redstone";
    public static final String REPLACE_STACK = "replaceStack";
    public static final String REQUIRE_STACK = "requireStack";
    public static final String ROUND_ROBIN = "roundRobin";
    public static final String ROUND_ROBIN_TARGET = "rrTarget";
    public static final String RUNNING = "running";
    public static final String SECURITY_MODE = "securityMode";
    public static final String SELECTED = "selected";
    public static final String SIDE = "side";
    public static final String SILK_TOUCH = "silkTouch";
    public static final String SINGLE_ITEM = "singleItem";
    public static final String SIZE_MODE = "sizeMode";
    public static final String SIZE_OVERRIDE = "SizeOverride";
    public static final String SLOT = "Slot";
    public static final String SLURRY_NAME = "slurryName";
    public static final String SLURRY_STORED = "slurry";
    public static final String SLURRY_TANKS = "SlurryTanks";
    public static final String SORTING = "sorting";
    public static final String STABILIZER_CHUNKS_TO_LOAD = "stabilizerChunksToLoad";
    public static final String STATE = "state";
    public static final String STOCK_CONTROL = "stockControl";
    public static final String STORED = "stored";
    public static final String STRICT_INPUT = "strictInput";
    public static final String TAG_NAME = "tagName";
    public static final String TANK = "Tank";
    public static final String TEMPERATURE = "temperature";
    public static final String TILE_TAG = "tileTag";
    public static final String TIME = "time";
    public static final String TRUSTED = "trusted";
    public static final String TYPE = "type";
    public static final String UPDATE_DELAY = "updateDelay";
    public static final String UPGRADES = "upgrades";
    public static final String USED_NODES = "usedNodes";
    public static final String USED_SO_FAR = "usedSoFar";
    public static final String WORLD_GEN_VERSION = "mekWorldGenVersion";
}