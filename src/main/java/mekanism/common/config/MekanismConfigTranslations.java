package mekanism.common.config;

import mekanism.common.Mekanism;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;

public enum MekanismConfigTranslations implements IConfigTranslation {
    //Client config
    CLIENT_WHITE_RADIAL_TEXT("client.white_radial_text", "White Radial Text", "If enabled tries to force all radial menu text to be white."),
    CLIENT_OPAQUE_TRANSMITTERS("client.transmitters.opaque", "Opaque Transmitters", "If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents."),
    CLIENT_SCROLL_MODE_CHANGE("client.mode_change.scroll", "Scroll Mode Change", "Allow sneak + scroll to change item modes."),
    CLIENT_ENERGY_COLOR("client.energy.color", "Energy Color", "Color of energy in item durability display."),
    CLIENT_BE_RENDER_RANGE("client.render_range.block_entity", "BE Render Range",
          "Range at which Block Entity Renderer's added by Mekanism can render at, for example the contents of multiblocks. Vanilla defaults the rendering range for "
          + "BERs to 64 for most blocks, but uses a range of 256 for beacons and end gateways."),

    CLIENT_SOUNDS("client.sounds", "Sound Settings", "Settings for configuring Mekanism's Sounds"),
    CLIENT_PLAYER_SOUNDS_ENABLED("client.sounds.player.enable", "Enable Player Sounds", "Play sounds for Jetpack/Gas Mask/Flamethrower/Radiation (all players)."),
    CLIENT_MACHINE_SOUNDS_ENABLED("client.sounds.machine.enable", "Enable Player Sounds", "If enabled machines play their sounds while running."),
    CLIENT_BASE_SOUND_VOLUME("client.sounds.base_volume", "Base Sound Volume", "Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder."),

    CLIENT_PARTICLE("client.particle", "Particle Settings", "Settings for configuring Mekanism's Particles"),
    CLIENT_PARTICLE_MULTIBLOCK_FORMATION("client.particle.multiblock_formation", "Multiblock Formation",
          "Set to false to prevent particle spam when loading multiblocks (notification message will display instead)."),
    CLIENT_PARTICLE_MACHINE_EFFECTS("client.particle.machine_effects", "Machine Effects", "Show particles when machines active."),
    CLIENT_PARTICLE_RADIATION_RADIUS("client.particle.radiation.radius", "Radiation Particle Radius",
          "How far (in blocks) from the player radiation particles can spawn."),
    CLIENT_PARTICLE_RADIATION_COUNT("client.particle.radiation.count", "Radiation Particle Count",
          "How many particles spawn when rendering radiation effects (scaled by radiation level)."),
    CLIENT_PARTICLE_MAGNETIC_ATTRACTION("client.particle.magnetic_attraction", "Magnetic Attraction",
          "Show bolts when the Magnetic Attraction Unit is pulling items."),
    CLIENT_PARTICLE_TOOL_AOE("client.particle.tool_aoe", "Tool AOE", "Show bolts for various AOE tool behaviors such as tilling, debarking, and vein mining."),

    CLIENT_HUD("client.hud", "HUD Settings", "Settings for configuring Mekanism's HUD"),
    CLIENT_HUD_ENABLED("client.hud.enabled", "Enabled", "Enable item information HUD during gameplay."),
    CLIENT_HUD_SCALE("client.hud.scale", "Scale", "Scale of the text displayed on the HUD."),
    CLIENT_HUD_REVERSE("client.hud.reverse", "Reverse", "If true will move HUD text alignment and compass rendering to the right side of the screen, "
                                                        + "and move the MekaSuit module rendering to the left side."),
    CLIENT_HUD_OPACITY("client.hud.opacity", "Opacity", "Opacity of HUD used by MekaSuit."),
    CLIENT_HUD_COLOR("client.hud.color", "Color", "Color (RGB) of HUD used by MekaSuit."),
    CLIENT_HUD_COLOR_WARNING("client.hud.color.warning", "Warning Color", "Color (RGB) of warning HUD elements used by MekaSuit."),
    CLIENT_HUD_COLOR_DANGER("client.hud.color.danger", "Danger Color", "Color (RGB) of danger HUD elements used by MekaSuit."),
    CLIENT_HUD_JITTER("client.hud.jitter", "Jitter", "Visual jitter of MekaSuit HUD, seen when moving the player's head. Bigger value = more jitter."),
    CLIENT_HUD_COMPASS("client.hud.compass", "Compass", "Display a fancy compass when the MekaSuit is worn."),

    CLIENT_QIO("client.qio", "QIO Settings", "Settings for configuring Mekanism's QIO"),
    CLIENT_QIO_SORT_TYPE("client.qio.sort.type", "Sort Type", "Sorting strategy when viewing items in a QIO Item Viewer."),
    CLIENT_QIO_SORT_DIRECTION("client.qio.sort.direction", "Sort Direction", "Sorting direction when viewing items in a QIO Item Viewer."),
    CLIENT_QIO_SLOTS_X("client.qio.slots.x", "Slots Wide", "Number of slots to view horizontally on a QIO Item Viewer."),
    CLIENT_QIO_SLOTS_Y("client.qio.slots.y", "Slots Tall", "Number of slots to view vertically on a QIO Item Viewer."),
    CLIENT_QIO_AUTO_FOCUS("client.qio.auto_focus", "Auto-Focus", "Determines whether the search bar is automatically focused when a QIO Dashboard is opened."),
    CLIENT_QIO_REJECTS_DESTINATION("client.qio.rejects.destination", "Transfer Rejects To Inventory",
          "Determines if items in a QIO crafting window should be moved to the player's inventory or frequency first when replacing the items with a recipe viewer."),

    CLIENT_LAST_WINDOW_POSITIONS("client.last_window_positions", "Last Window Positions",
          "Stores the last position various windows were in when they were closed, and whether they are pinned. In general these values should not be modified manually."),
    CLIENT_LAST_WINDOW_POSITIONS_X("client.last_window_positions.x", "X Component", "The x component of this window's last position."),
    CLIENT_LAST_WINDOW_POSITIONS_Y("client.last_window_positions.y", "Y Component", "The y component of this window's last position."),
    CLIENT_LAST_WINDOW_POSITIONS_PINNED("client.last_window_positions.pinned", "Pinned",
          "Determines whether this window is pinned (opens automatically when the GUI is reopened)."),

    //Common Config
    COMMON_UNIT_ENERGY("common.unit.energy", "Energy Unit", "Displayed energy type in Mekanism GUIs and network reader readings."),
    COMMON_UNIT_TEMPERATURE("common.unit.temperature", "Temperature Unit", "Displayed temperature unit in Mekanism GUIs and network reader readings."),
    COMMON_DECAY_TIMERS("common.decay_timers", "Decay Timers",
          "Show time to decay radiation when readings are above safe levels. Set to false on the client side to disable MekaSuit Geiger and Dosimeter Unit timers. "
          + "Set to false on the server side to disable handheld Geiger Counter and Dosimeter timers."),
    COMMON_COPY_BLOCK_DATA("common.copy_block_data", "Copy Block Data",
          "Determines whether machine configuration data is copied when using middle click. If this is set to false no data will be copied and the default instance "
          + "of the stack will be returned."),
    COMMON_HOLIDAYS("common.holidays", "Holidays",
          "Should holiday greetings and easter eggs play for holidays (ex: Christmas and New Years) on the client. And should robit skins be randomized on the server."),

    //Storage Config
    ENERGY_STORAGE_ENRICHMENT_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Enrichment Chamber"),
    ENERGY_STORAGE_COMPRESSOR(TranslationPreset.ENERGY_STORAGE, "Osmium Compressor"),
    ENERGY_STORAGE_COMBINER(TranslationPreset.ENERGY_STORAGE, "Combiner"),
    ENERGY_STORAGE_CRUSHER(TranslationPreset.ENERGY_STORAGE, "Crusher"),
    ENERGY_STORAGE_METALLURGIC_INFUSER(TranslationPreset.ENERGY_STORAGE, "Metallurgic Infuser"),
    ENERGY_STORAGE_PURIFICATION_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Purification Chamber"),
    ENERGY_STORAGE_SMELTER(TranslationPreset.ENERGY_STORAGE, "Energized Smelter"),
    ENERGY_STORAGE_MINER(TranslationPreset.ENERGY_STORAGE, "Digital Miner"),
    ENERGY_STORAGE_PUMP(TranslationPreset.ENERGY_STORAGE, "Electric Pump"),
    ENERGY_STORAGE_CHARGEPAD(TranslationPreset.ENERGY_STORAGE, "Chargepad"),
    ENERGY_STORAGE_CONDENSENTRATOR(TranslationPreset.ENERGY_STORAGE, "Rotary Condensentrator"),
    ENERGY_STORAGE_OXIDIZER(TranslationPreset.ENERGY_STORAGE, "Chemical Oxidizer"),
    ENERGY_STORAGE_CHEMICAL_INFUSER(TranslationPreset.ENERGY_STORAGE, "Chemical Infuser"),
    ENERGY_STORAGE_INJECTION_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Chemical Injection Chamber"),
    ENERGY_STORAGE_SEPARATOR(TranslationPreset.ENERGY_STORAGE, "Electrolytic Separator"),
    ENERGY_STORAGE_SAWMILL(TranslationPreset.ENERGY_STORAGE, "Precision Sawmill"),
    ENERGY_STORAGE_CDC(TranslationPreset.ENERGY_STORAGE, "Chemical Dissolution Chamber"),
    ENERGY_STORAGE_WASHER(TranslationPreset.ENERGY_STORAGE, "Chemical Washer"),
    ENERGY_STORAGE_CRYSTALLIZER(TranslationPreset.ENERGY_STORAGE, "Chemical Crystallizer"),
    ENERGY_STORAGE_VIBRATOR(TranslationPreset.ENERGY_STORAGE, "Seismic Vibrator"),
    ENERGY_STORAGE_PRC(TranslationPreset.ENERGY_STORAGE, "Pressurized Reaction Chamber"),
    ENERGY_STORAGE_PLENISHER(TranslationPreset.ENERGY_STORAGE, "Fluidic Plenisher"),
    ENERGY_STORAGE_LASER(TranslationPreset.ENERGY_STORAGE, "Laser"),
    ENERGY_STORAGE_LASER_AMPLIFIER(TranslationPreset.ENERGY_STORAGE, "Laser Amplifier"),
    ENERGY_STORAGE_TRACTOR_BEAM(TranslationPreset.ENERGY_STORAGE, "Laser Tractor Beam"),
    ENERGY_STORAGE_ASSEMBLICATOR(TranslationPreset.ENERGY_STORAGE, "Formulaic Assemblicator"),
    ENERGY_STORAGE_TELEPORTER(TranslationPreset.ENERGY_STORAGE, "Teleporter"),
    ENERGY_STORAGE_MODIFICATION_STATION(TranslationPreset.ENERGY_STORAGE, "Modification Station"),
    ENERGY_STORAGE_CENTRIFUGE(TranslationPreset.ENERGY_STORAGE, "Isotopic Centrifuge"),
    ENERGY_STORAGE_LIQUIFIER(TranslationPreset.ENERGY_STORAGE, "Nutritional Liquifier"),
    ENERGY_STORAGE_NUCLEOSYNTHESIZER(TranslationPreset.ENERGY_STORAGE, "Nucleosynthesizer", ". Also defines max process rate."),
    ENERGY_STORAGE_PIGMENT_EXTRACTOR(TranslationPreset.ENERGY_STORAGE, "Pigment Extractor"),
    ENERGY_STORAGE_PIGMENT_MIXER(TranslationPreset.ENERGY_STORAGE, "Pigment Mixer"),
    ENERGY_STORAGE_PAINTING(TranslationPreset.ENERGY_STORAGE, "Painting Machine"),
    ENERGY_STORAGE_SPS_PORT(TranslationPreset.ENERGY_STORAGE, "SPS Port", ". Also defines max output rate."),
    ENERGY_STORAGE_DIMENSIONAL_STABILIZER(TranslationPreset.ENERGY_STORAGE, "Dimensional Stabilizer"),

    //Usage Config
    ENERGY_USAGE_ENRICHMENT_CHAMBER(TranslationPreset.ENERGY_USAGE, "Enrichment Chamber"),
    ENERGY_USAGE_COMPRESSOR(TranslationPreset.ENERGY_USAGE, "Osmium Compressor"),
    ENERGY_USAGE_COMBINER(TranslationPreset.ENERGY_USAGE, "Combiner"),
    ENERGY_USAGE_CRUSHER(TranslationPreset.ENERGY_USAGE, "Crusher"),
    ENERGY_USAGE_METALLURGIC_INFUSER(TranslationPreset.ENERGY_USAGE, "Metallurgic Infuser"),
    ENERGY_USAGE_PURIFICATION_CHAMBER(TranslationPreset.ENERGY_USAGE, "Purification Chamber"),
    ENERGY_USAGE_SMELTER(TranslationPreset.ENERGY_USAGE, "Energized Smelter"),
    ENERGY_USAGE_MINER(TranslationPreset.ENERGY_USAGE, "Digital Miner"),
    ENERGY_USAGE_PUMP(TranslationPreset.ENERGY_USAGE, "Electric Pump"),
    ENERGY_USAGE_CHARGEPAD("usage.chargepad.energy", "Chargepad Energy Usage", "Energy in Joules that can be transferred at once per charge operation."),
    ENERGY_USAGE_CONDENSENTRATOR(TranslationPreset.ENERGY_USAGE, "Rotary Condensentrator"),
    ENERGY_USAGE_OXIDIZER(TranslationPreset.ENERGY_USAGE, "Chemical Oxidizer"),
    ENERGY_USAGE_CHEMICAL_INFUSER(TranslationPreset.ENERGY_USAGE, "Chemical Infuser"),
    ENERGY_USAGE_INJECTION_CHAMBER(TranslationPreset.ENERGY_USAGE, "Chemical Injection Chamber"),
    ENERGY_USAGE_SAWMILL(TranslationPreset.ENERGY_USAGE, "Precision Sawmill"),
    ENERGY_USAGE_CDC(TranslationPreset.ENERGY_USAGE, "Chemical Dissolution Chamber"),
    ENERGY_USAGE_WASHER(TranslationPreset.ENERGY_USAGE, "Chemical Washer"),
    ENERGY_USAGE_CRYSTALLIZER(TranslationPreset.ENERGY_USAGE, "Chemical Crystallizer"),
    ENERGY_USAGE_VIBRATOR(TranslationPreset.ENERGY_USAGE, "Seismic Vibrator"),
    ENERGY_USAGE_PRC(TranslationPreset.ENERGY_USAGE, "Pressurized Reaction Chamber"),
    ENERGY_USAGE_PLENISHER(TranslationPreset.ENERGY_USAGE, "Fluidic Plenisher"),
    ENERGY_USAGE_LASER(TranslationPreset.ENERGY_USAGE, "Laser"),
    ENERGY_USAGE_ASSEMBLICATOR(TranslationPreset.ENERGY_USAGE, "Formulaic Assemblicator"),
    ENERGY_USAGE_MODIFICATION_STATION(TranslationPreset.ENERGY_USAGE, "Modification Station"),
    ENERGY_USAGE_CENTRIFUGE(TranslationPreset.ENERGY_USAGE, "Isotopic Centrifuge"),
    ENERGY_USAGE_LIQUIFIER(TranslationPreset.ENERGY_USAGE, "Nutritional Liquifier"),
    ENERGY_USAGE_NUCLEOSYNTHESIZER(TranslationPreset.ENERGY_USAGE, "Nucleosynthesizer"),
    ENERGY_USAGE_PIGMENT_EXTRACTOR(TranslationPreset.ENERGY_USAGE, "Pigment Extractor"),
    ENERGY_USAGE_PIGMENT_MIXER(TranslationPreset.ENERGY_USAGE, "Pigment Mixer"),
    ENERGY_USAGE_PAINTING(TranslationPreset.ENERGY_USAGE, "Painting Machine"),
    ENERGY_USAGE_DIMENSIONAL_STABILIZER("usage.stabilizer.energy", "Dimensional Stabilizer Energy Usage", "Energy per chunk per tick in Joules"),

    USAGE_TELEPORTER("usage.teleporter", "Teleporter Settings", "Settings for configuring Teleporter Energy Usage"),
    USAGE_TELEPORTER_BASE("usage.teleporter.base", "Base Energy Usage", "Base Joules cost for teleporting an entity."),
    USAGE_TELEPORTER_DISTANCE("usage.teleporter.distance", "Distance Energy Usage",
          "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2)."),
    USAGE_TELEPORTER_PENALTY("usage.teleporter.penalty.dimension", "Dimension Energy Penalty",
          "Flat additional cost for interdimensional teleportation. Distance is still taken into account minimizing energy cost based on dimension scales."),


    GEAR_MEKA_SUIT("gear.meka_suit", "MekaSuit Settings", "Settings for configuring the MekaSuit"),
    GEAR_MEKA_SUIT_DAMAGE_ABSORPTION("gear.meka_suit.damage_absorption", "MekaSuit Damage Absorption Settings", "Settings for configuring damage absorption of the MekaSuit"),


    //World Config
    WORLD_RETROGEN("world.retrogen", "Retrogen",
          "Allows chunks to retrogen Mekanism salt and ore blocks. In general when enabling this you also want to bump userWorldGenVersion."),
    WORLD_WORLD_VERSION("world.world_version", "User World Version", "Change this value to cause Mekanism to regen its ore in all loaded chunks."),
    WORLD_HEIGHT_RANGE_PLATEAU("world.height_range.plateau", "Plateau",
          "Half length of short side of trapezoid, only used if shape is TRAPEZOID. A value of zero means the shape is a triangle."),
    WORLD_ANCHOR_TYPE("world.height_range.anchor.type", "Anchor Type", "Type of anchor"),
    WORLD_ANCHOR_VALUE("world.height_range.anchor.value", "Value", "Value used for calculating y for the anchor based on the type."),

    WORLD_SALT("world.salt", "Salt Settings", "Generation Settings for salt."),
    WORLD_SALT_SHOULD_GENERATE("world.salt.generate", "Should Generate", "Determines if salt should be added to world generation."),
    WORLD_SALT_PER_CHUNK("world.salt.per_chunk", "Per Chunk", "Chance that salt generates in a chunk."),
    WORLD_SALT_RADIUS_MIN("world.salt.radius.min", "Min Radius", "Base radius of a vein of salt."),
    WORLD_SALT_RADIUS_MAX("world.salt.radius.max", "Min Radius", "Extended variability (spread) for the radius in a vein of salt."),
    WORLD_SALT_HALF_HEIGHT("world.salt.half_height", "Half Height", "Number of blocks to extend up and down when placing a vein of salt."),
    ;

    private final String key;
    private final String title;
    private final String tooltip;

    MekanismConfigTranslations(TranslationPreset preset, String type) {
        this(preset.path(type), preset.title(type), preset.tooltip(type));
    }

    MekanismConfigTranslations(TranslationPreset preset, String type, String tooltipSuffix) {
        this(preset.path(type), preset.title(type), preset.tooltip(type) + tooltipSuffix);
    }

    MekanismConfigTranslations(String path, String title, String tooltip) {
        this.key = Util.makeDescriptionId("configuration", Mekanism.rl(path));
        this.title = title;
        this.tooltip = tooltip;
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

    public record OreConfigTranslations(IConfigTranslation topLevel, IConfigTranslation shouldGenerate) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, shouldGenerate};
        }

        private static String getKey(String ore, String path) {
            return Util.makeDescriptionId("configuration", Mekanism.rl("world." + ore + "." + path));
        }

        public static OreConfigTranslations create(String ore) {
            String name = TextUtils.formatAndCapitalize(ore);
            return new OreConfigTranslations(
                  new ConfigTranslation(getKey(ore, "top_level"), name + " Settings", "Generation Settings for " + name + " ore."),
                  new ConfigTranslation(getKey(ore, "generate"), "Should Generate", "Determines if " + name + " ore should be added to world generation.")
            );
        }
    }

    public record OreVeinConfigTranslations(
          IConfigTranslation topLevel, IConfigTranslation shouldGenerate,
          IConfigTranslation perChunk, IConfigTranslation maxVeinSize, IConfigTranslation discardChanceOnAirExposure,
          IConfigTranslation distributionShape, IConfigTranslation minInclusive, IConfigTranslation maxInclusive
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, shouldGenerate, perChunk, maxVeinSize, discardChanceOnAirExposure, distributionShape, minInclusive,
                                            maxInclusive};
        }

        private static String getKey(String ore, String vein, String path) {
            return Util.makeDescriptionId("configuration", Mekanism.rl("world." + ore + "." + vein + "." + path));
        }

        public static OreVeinConfigTranslations create(String ore, String vein) {
            String capitalizedOre = TextUtils.formatAndCapitalize(ore);
            String capitalizedVein = TextUtils.formatAndCapitalize(vein);
            String name = capitalizedVein + " " + capitalizedOre + " Vein";
            return new OreVeinConfigTranslations(
                  new ConfigTranslation(getKey(ore, vein, "top_level"), capitalizedVein + " Vein", name + " Generation Settings."),
                  new ConfigTranslation(getKey(ore, vein, "generate"), "Should Generate",
                        "Determines if " + name + "s should be added to world generation. Note: Requires generating " + ore + " ore to be enabled."),
                  new ConfigTranslation(getKey(ore, vein, "per_chunk"), "Per Chunk", "Chance that " + name + "s generates in a chunk."),
                  new ConfigTranslation(getKey(ore, vein, "max_size"), "Max Size", "Maximum number of blocks in a " + name + "."),
                  new ConfigTranslation(getKey(ore, vein, "discard_chance"), "Discard Chance",
                        "Chance that blocks that are directly exposed to air in a " + name + " are not placed."),

                  new ConfigTranslation(getKey(ore, vein, "shape"), "Distribution shape", "Distribution shape for placing " + name + "s."),
                  new ConfigTranslation(getKey(ore, vein, "min"), "Min Anchor", "Minimum (inclusive) height anchor for " + name + "s."),
                  new ConfigTranslation(getKey(ore, vein, "max"), "Max Anchor", "Maximum (inclusive) height anchor for " + name + "s.")
            );
        }
    }
}