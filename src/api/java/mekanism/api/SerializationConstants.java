package mekanism.api;

/**
 * Class for storing constants that are used in various serialization related storage, to reduce the chances of typos
 *
 * @since 10.6.0 - Previously was split between NBTConstants and JsonConstants
 */
public final class SerializationConstants {

    private SerializationConstants() {
    }

    //Ones that also are used for interacting with neo/vanilla
    public static final String COUNT = "count";
    public static final String CUSTOM_NAME = "CustomName";
    public static final String ENTITY = "entity";
    public static final String ID = "id";
    /**
     * Used for TrialSpawnerBlock and other newer spawners
     */
    public static final String SPAWN_DATA = "spawn_data";
    /**
     * Used for SpawnerBlock
     */
    public static final String SPAWN_DATA_LEGACY = "SpawnData";
    public static final String X = "x";
    public static final String Y = "y";
    public static final String Z = "z";

    //Ingredients
    public static final String AMOUNT = "amount";
    public static final String BASE = "base";
    /**
     * @since 10.7.0
     */
    public static final String CHEMICAL = "chemical";
    public static final String CHILDREN = "children";
    public static final String FLUID = "fluid";
    public static final String INGREDIENT = "ingredient";
    public static final String INGREDIENTS = "ingredients";
    public static final String ITEM = "item";
    /**
     * @since 10.6.9
     */
    public static final String ITEM_OVERSIZED = "item_oversized";
    public static final String SUBTRACTED = "subtracted";
    public static final String TAG = "tag";

    //Recipes
    public static final String TYPE = "type";
    public static final String ENERGY_MULTIPLIER = "energy_multiplier";
    public static final String ENERGY_REQUIRED = "energy_required";
    public static final String DURATION = "duration";
    public static final String INPUT = "input";
    public static final String MAIN_INPUT = "main_input";
    public static final String EXTRA_INPUT = "extra_input";
    public static final String LEFT_INPUT = "left_input";
    public static final String RIGHT_INPUT = "right_input";
    public static final String CHEMICAL_INPUT = "chemical_input";
    public static final String FLUID_INPUT = "fluid_input";
    public static final String ITEM_INPUT = "item_input";
    public static final String OUTPUT = "output";
    /**
     * @since 10.7.0
     */
    public static final String PER_TICK_USAGE = "per_tick_usage";
    public static final String MAIN_OUTPUT = "main_output";
    public static final String SECONDARY_OUTPUT = "secondary_output";
    public static final String SECONDARY_CHANCE = "secondary_chance";
    /**
     * @since 10.7.0
     */
    public static final String CHEMICAL_OUTPUT = "chemical_output";
    /**
     * @since 10.7.0
     */
    public static final String LEFT_CHEMICAL_OUTPUT = "left_chemical_output";
    /**
     * @since 10.7.0
     */
    public static final String RIGHT_CHEMICAL_OUTPUT = "right_chemical_output";
    public static final String ITEM_OUTPUT = "item_output";
    public static final String FLUID_OUTPUT = "fluid_output";

    //Transmitter model
    public static final String GLASS = "glass";

    //Advancement Triggers
    public static final String ACTION = "action";
    public static final String COPY = "copy";
    public static final String PLAYER = "player";
    public static final String DAMAGE = "damage";
    public static final String KILLED = "killed";
    public static final String SKIN = "skin";

    //Server to Client specific sync constants
    public static final String ACTIVE = "active";
    public static final String COMPLEX = "complex";
    public static final String CURRENT_ACCEPTORS = "acceptors";
    public static final String CURRENT_CONNECTIONS = "connections";
    public static final String HAS_STRUCTURE = "has_structure";
    public static final String HEIGHT = "height";
    public static final String INVENTORY_ID = "inventory_id";
    public static final String LOWER_VOLUME = "lower_volume";
    public static final String MUFFLING_COUNT = "muffling";
    public static final String NETWORK = "network";
    public static final String RENDERING = "rendering";
    public static final String RENDER_LOCATION = "render_location";
    public static final String RENDER_Y = "render_y";
    public static final String ROTATION = "rotation";
    public static final String SCALE = "scale";
    public static final String SCALE_ALT = "scale_alt";
    public static final String SCALE_ALT_2 = "scale_2";
    public static final String SCALE_ALT_3 = "scale_3";
    public static final String SOUND_SCALE = "sound_scale";
    public static final String VALVE = "valve";
    public static final String VOLUME = "volume";

    //Generic constants
    /**
     * @since 10.7.11
     */
    public static final String ABSORPTION = "absorption";
    public static final String ACTIVE_COOLED = "active_cooled";
    public static final String ACTIVE_NODES = "active_nodes";
    public static final String ACTIVE_STATE = "active_state";
    public static final String ADVANCEMENT = "advancement";
    public static final String ALLOW_DEFAULT = "allow_default";
    public static final String AGE = "age";
    /**
     * @since 10.7.4
     */
    public static final String ALIASES = "aliases";
    public static final String ASSEMBLIES = "assemblies";
    public static final String AUTO = "auto";
    public static final String BABY_TYPE = "baby_type";
    public static final String BLADES = "blades";
    public static final String BLOCK = "block";
    public static final String BLOCK_ENTITY_TAG = "block_entity_tag";
    public static final String BOXED_CHEMICAL = "boxed_chemical";
    public static final String BUILT_IN_TABLES = "built_in_tables";
    public static final String BURNING = "burning";
    public static final String BURN_TIME = "burn_time";
    public static final String CACHE = "cache";
    public static final String CHANCE = "chance";
    public static final String CHANNEL = "channel";
    /**
     * @since 10.7.0
     */
    public static final String CHEMICAL_TANKS = "chemical_tanks";
    public static final String CHUNK_SET = "chunk_set";
    public static final String COILS = "coils";
    public static final String COLOR = "color";
    public static final String COMPONENT_CONFIG = "component_config";
    public static final String COMPONENT_EJECTOR = "component_ejector";
    public static final String COMPONENT_FREQUENCY = "component_frequency";
    public static final String COMPONENT_SECURITY = "component_security";
    public static final String COMPONENT_UPGRADE = "component_upgrade";
    public static final String COMPONENTS = "components";
    public static final String CONDITION = "condition";
    /**
     * @since 10.7.11
     */
    public static final String CONDUCTIVITY = "conductivity";
    public static final String CONFIG = "config";
    public static final String CONNECTION = "connection";
    public static final String CONTAINER = "container";
    public static final String CONTROL_TYPE = "control_type";
    /**
     * @since 10.7.11
     */
    public static final String COOL_VARIANT = "cool_variant";
    public static final String COULD_OPERATE = "could_operate";
    public static final String CURRENT_REDSTONE = "current_redstone";
    public static final String CUSTOM_MODEL = "custom_model";
    public static final String DATA = "data";
    public static final String DATA_NAME = "data_name";
    public static final String DATA_TYPE = "data_type";
    public static final String DELAY = "delay";
    public static final String DESCRIPTION = "description";
    public static final String DIMENSION = "dimension";
    public static final String DIRECTION = "direction";
    public static final String DISABLED = "disabled";
    public static final String DISTANCE = "distance";
    public static final String DRAINING = "draining";
    public static final String DRIVES = "drives";
    public static final String DUMP_LEFT = "dump_left";
    public static final String DUMP_MODE = "dumping";
    public static final String DUMP_RIGHT = "dump_right";
    public static final String EDIT_MODE = "edit_mode";
    public static final String EJECT = "eject";
    public static final String EMITTING = "emitting";
    public static final String ENABLED = "enabled";
    public static final String ENCHANTMENTS = "enchantments";
    public static final String ENERGY_CONTAINERS = "energy_containers";
    public static final String ENERGY = "energy";
    public static final String ENERGY_USAGE = "energy_usage";
    public static final String ENUMS = "enums";
    public static final String EXTENDS = "extends";
    public static final String FIELDS = "fields";
    public static final String FILLING = "filling";
    public static final String FILTER = "filter";
    public static final String FILTERS = "filters";
    public static final String FINISHED = "finished";
    public static final String FORMED = "formed";
    public static final String FLUID_TANKS = "fluid_tanks";
    public static final String FOLLOW = "follow";
    public static final String FREQUENCY_LIST = "freq_list";
    public static final String FUZZY = "fuzzy";
    /**
     * @since 10.7.0
     */
    public static final String CHEMICAL_STORED_ALT = "chemical_1";
    /**
     * @since 10.7.0
     */
    public static final String CHEMICAL_STORED_ALT_2 = "chemical_2";
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final String GAS_TANKS = "gas_tanks";
    public static final String GUI = "gui";
    public static final String HANDLE_SOUND = "handle_sound";
    public static final String HEAT_CAPACITORS = "heat_capacitors";
    public static final String HEAT_CAPACITY = "heat_capacity";
    public static final String HEAT_STORED = "heat";
    public static final String HOME_LOCATION = "home_location";
    /**
     * @since 10.7.11
     */
    public static final String HOT_VARIANT = "hot_variant";
    public static final String HUMAN_NAME = "human_name";
    public static final String IDENTITY = "identity";
    public static final String IDLE_DIR = "idle_dir";
    public static final String INDEX = "index";
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final String INFUSION_TANKS = "infusion_tanks";
    public static final String INJECTION_RATE = "injection_rate";
    public static final String INPUT_COLOR = "input_color";
    public static final String INSERT_INTO_FREQUENCY = "insert_into_frequency";
    public static final String INVALID = "invalid";
    public static final String INVERSE = "inverse";
    public static final String INVERSE_REQUIRES_REPLACE = "inverse_replace";
    public static final String ITEMS = "items";
    public static final String JAVA_EXTRA = "java_extra";
    public static final String JAVA_TYPE = "java_type";
    public static final String KEY = "key";
    public static final String LAST_FIRED = "last_fired";
    public static final String LAST_PROCESSED = "last_processed";
    public static final String LATCHED = "latched";
    public static final String LEVEL = "level";
    public static final String LOCK_STACK = "lock_stack";
    public static final String LOGIC_TYPE = "logic_type";
    public static final String MAGNITUDE = "magnitude";
    public static final String MAIN = "main";
    public static final String MAX = "max";
    public static final String MAX_BURN_TIME = "max_burn_time";
    public static final String MEK_DATA = "mek_data";
    public static final String MELTDOWNS = "meltdowns";
    public static final String METHOD_NAME = "methodName";
    public static final String METHODS = "methods";
    public static final String MIN = "min";
    public static final String MODE = "mode";
    public static final String MODID = "modid";
    public static final String MODULES = "modules";
    public static final String NAME = "name";
    public static final String NEXT = "next";
    public static final String NUM_POWERING = "num_powering";
    public static final String ORE_TYPE = "ore_type";
    public static final String ORIGINAL_LOCATION = "original_location";
    public static final String OUTPUT_MODE = "output_mode";
    public static final String OVERFLOW = "overflow";
    public static final String OVERRIDE = "override";
    public static final String OWNER_NAME = "owner_name";
    public static final String OWNER_UUID = "owner";
    public static final String PARAMETERS = "params";
    public static final String PARTIAL_WASTE = "partial_waste";
    public static final String PATH_TYPE = "path_type";
    public static final String PERSONAL_STORAGE_ID = "personal_storage_id";
    public static final String PICKUP_DROPS = "pickup_drops";
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final String PIGMENT_TANKS = "pigment_tanks";
    public static final String PLASMA_TEMP = "plasma_temp";
    /**
     * @since 10.7.15
     */
    public static final String POS = "pos";
    public static final String POSITION = "position";
    public static final String PREVIOUS = "previous";
    public static final String PROCESSED = "processed";
    public static final String PROGRESS = "progress";
    public static final String PULL = "pull";
    public static final String PULSE = "pulse";
    public static final String RADIATION = "radiation";
    public static final String RADIATION_LIST = "radList";
    /**
     * @since 10.7.11
     */
    public static final String RADIOACTIVITY = "radioactivity";
    public static final String RADIUS = "radius";
    public static final String REACTOR_DAMAGE = "reactor_damage";
    @Deprecated(forRemoval = true, since = "10.7.15")
    public static final String RECEIVED_COORDS = "received_coords";
    public static final String RECURRING_NODES = "recurring_nodes";
    public static final String REDSTONE = "redstone";
    public static final String REPLACE_TARGET = "replace_target";
    /**
     * @since 10.7.11
     */
    public static final String REPRESENTATION = "representation";
    public static final String REQUIRES_REPLACEMENT = "requires_replacement";
    public static final String REQUIRES_PUBLIC_SECURITY = "requires_public_security";
    public static final String RESTRICTION = "restriction";
    public static final String RETRO_GEN = "retro_gen";
    public static final String RETURNS = "returns";
    public static final String ROUND_ROBIN = "round_robin";
    public static final String ROUND_ROBIN_TARGET = "rr_target";
    public static final String RUNNING = "running";
    public static final String SECURITY_MODE = "security_mode";
    public static final String SELECTED = "selected";
    public static final String SIDE = "side";
    public static final String SILK_TOUCH = "silk_touch";
    public static final String SINGLE_ITEM = "single_item";
    public static final String SIZE = "size";
    public static final String SLOT = "slot";
    @Deprecated(forRemoval = true, since = "10.7.0")
    public static final String SLURRY_TANKS = "slurry_tanks";
    public static final String SORTING = "sorting";
    public static final String STABILIZER_CHUNKS_TO_LOAD = "stabilizer_chunks_to_load";
    public static final String STATE = "state";
    public static final String STATE_PROVIDER = "state_provider";
    public static final String STOCK_CONTROL = "stock_control";
    public static final String STORED = "stored";
    public static final String STRICT_INPUT = "strict_input";
    public static final String TANK = "tank";
    public static final String TARGET = "target";
    public static final String TARGET_STACK = "target_stack";
    public static final String TARGETS = "targets";
    public static final String TEMPERATURE = "temperature";
    public static final String TEXT = "text";
    public static final String TEXTURES = "textures";
    /**
     * @since 10.7.11
     */
    public static final String THERMAL_ENTHALPY = "thermal_enthalpy";
    public static final String TIME = "time";
    public static final String TRUSTED = "trusted";
    public static final String TYPES = "types";
    public static final String UPDATE_DELAY = "update_delay";
    public static final String UPGRADES = "upgrades";
    public static final String USED_NODES = "used_nodes";
    public static final String USED_SO_FAR = "used_so_far";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String VERSION = "version";
    public static final String WORLD_GEN_VERSION = "mek_world_gen_version";
}