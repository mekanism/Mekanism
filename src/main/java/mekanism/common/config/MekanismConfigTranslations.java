package mekanism.common.config;

import mekanism.common.Mekanism;
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



    BASE_ENERGY_STORAGE_JOULES("storage.energy.base", "Base energy storage", "Base energy storage (Joules)."),



    GEAR_MEKA_SUIT("gear.meka_suit", "MekaSuit Settings", "Settings for configuring the MekaSuit"),
    GEAR_MEKA_SUIT_DAMAGE_ABSORPTION("gear.meka_suit.damage_absorption", "MekaSuit Damage Absorption Settings", "Settings for configuring damage absorption of the MekaSuit"),

    ;

    private final String key;
    private final String title;
    private final String tooltip;

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
}