package mekanism.common.config;

import java.util.HashMap;
import java.util.Map;
import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer.ListSortType;
import mekanism.common.inventory.container.QIOItemViewerContainer.SortDirection;
import mekanism.common.inventory.container.SelectedWindowData.CachedWindowPosition;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig extends BaseMekanismConfig {

    private static final String PARTICLE_CATEGORY = "particle";
    private static final String GUI_CATEGORY = "gui";
    private static final String GUI_WINDOW_CATEGORY = "window";
    private static final String QIO_CATEGORY = "qio";

    private final ModConfigSpec configSpec;

    public final CachedBooleanValue enablePlayerSounds;
    public final CachedBooleanValue enableMachineSounds;
    public final CachedBooleanValue whiteRadialText;
    public final CachedFloatValue baseSoundVolume;
    public final CachedBooleanValue opaqueTransmitters;
    public final CachedBooleanValue allowModeScroll;
    public final CachedBooleanValue reverseHUD;
    public final CachedFloatValue hudScale;
    public final CachedBooleanValue enableHUD;
    public final CachedIntValue energyColor;
    public final CachedIntValue terRange;

    public final CachedBooleanValue enableMultiblockFormationParticles;
    public final CachedBooleanValue machineEffects;
    public final CachedIntValue radiationParticleRadius;
    public final CachedIntValue radiationParticleCount;
    public final CachedBooleanValue renderMagneticAttractionParticles;
    public final CachedBooleanValue renderToolAOEParticles;

    public final CachedFloatValue hudOpacity;
    public final CachedIntValue hudColor;
    public final CachedIntValue hudWarningColor;
    public final CachedIntValue hudDangerColor;
    public final CachedFloatValue hudJitter;
    public final CachedBooleanValue hudCompassEnabled;
    public final Map<String, CachedWindowPosition> lastWindowPositions = new HashMap<>();

    public final CachedEnumValue<ListSortType> qioItemViewerSortType;
    public final CachedEnumValue<SortDirection> qioItemViewerSortDirection;
    public final CachedIntValue qioItemViewerSlotsX;
    public final CachedIntValue qioItemViewerSlotsY;
    public final CachedBooleanValue qioRejectsToInventory;
    public final CachedBooleanValue qioAutoFocusSearchBar;

    ClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Client Config. This config only exists on the client").push("client");

        enablePlayerSounds = CachedBooleanValue.wrap(this, builder.comment("Play sounds for Jetpack/Gas Mask/Flamethrower/Radiation (all players).")
              .define("enablePlayerSounds", true));
        enableMachineSounds = CachedBooleanValue.wrap(this, builder.comment("If enabled machines play their sounds while running.")
              .define("enableMachineSounds", true));
        whiteRadialText = CachedBooleanValue.wrap(this, builder.comment("If enabled tries to force all radial menu text to be white.")
              .define("whiteRadialText", false));
        baseSoundVolume = CachedFloatValue.wrap(this, builder.comment("Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder.")
              .defineInRange("baseSoundVolume", 1D, 0, 10));
        opaqueTransmitters = CachedBooleanValue.wrap(this, builder.comment("If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents.")
              .define("opaqueTransmitters", false));
        allowModeScroll = CachedBooleanValue.wrap(this, builder.comment("Allow sneak + scroll to change item modes.")
              .define("allowModeScroll", true));
        reverseHUD = CachedBooleanValue.wrap(this, builder.comment("If true will move HUD text alignment and compass rendering to the right side of the screen, and move the MekaSuit module rendering to the left side.")
              .define("reverseHUD", false));
        hudScale = CachedFloatValue.wrap(this, builder.comment("Scale of the text displayed on the HUD.")
              .defineInRange("hudScale", 0.6, 0.25, 1));
        enableHUD = CachedBooleanValue.wrap(this, builder.comment("Enable item information HUD during gameplay")
              .define("enableHUD", true));
        energyColor = CachedIntValue.wrap(this, builder.comment("Color of energy in item durability display.")
              .define("energyColor", 0x3CFE9A));
        terRange = CachedIntValue.wrap(this, builder.comment("Range at which Tile Entity Renderer's added by Mekanism can render at, for example the contents of multiblocks. Vanilla defaults the rendering range for TERs to 64 for most blocks, but uses a range of 256 for beacons and end gateways.")
              .defineInRange("terRange", 256, 1, 1_024));

        builder.comment("Particle Config").push(PARTICLE_CATEGORY);
        enableMultiblockFormationParticles = CachedBooleanValue.wrap(this, builder.comment("Set to false to prevent particle spam when loading multiblocks (notification message will display instead).")
              .define("enableMultiblockFormationParticles", true));
        machineEffects = CachedBooleanValue.wrap(this, builder.comment("Show particles when machines active.")
              .define("machineEffects", true));
        radiationParticleRadius = CachedIntValue.wrap(this, builder.comment("How far (in blocks) from the player radiation particles can spawn.")
              .defineInRange("radiationParticleRadius", 30, 2, 64));
        radiationParticleCount = CachedIntValue.wrap(this, builder.comment("How many particles spawn when rendering radiation effects (scaled by radiation level).")
              .defineInRange("radiationParticleCount", 100, 0, 1_000));
        renderMagneticAttractionParticles = CachedBooleanValue.wrap(this, builder.comment("Show bolts when the Magnetic Attraction Unit is pulling items.")
              .define("magneticAttraction", true));
        renderToolAOEParticles = CachedBooleanValue.wrap(this, builder.comment("Show bolts for various AOE tool behaviors such as tilling, debarking, and vein mining.")
              .define("toolAOE", true));
        builder.pop();

        builder.comment("GUI Config").push(GUI_CATEGORY);
        hudOpacity = CachedFloatValue.wrap(this, builder.comment("Opacity of HUD used by MekaSuit.")
              .defineInRange("hudOpacity", 0.4F, 0, 1));
        hudColor = CachedIntValue.wrap(this, builder.comment("Color (RGB) of HUD used by MekaSuit.")
              .defineInRange("hudColor", 0x40F5F0, 0, 0xFFFFFF));
        hudWarningColor = CachedIntValue.wrap(this, builder.comment("Color (RGB) of warning HUD elements used by MekaSuit.")
              .defineInRange("hudWarningColor", 0xFFDD4F, 0, 0xFFFFFF));
        hudDangerColor = CachedIntValue.wrap(this, builder.comment("Color (RGB) of danger HUD elements used by MekaSuit.")
              .defineInRange("hudDangerColor", 0xFF383C, 0, 0xFFFFFF));
        hudJitter = CachedFloatValue.wrap(this, builder.comment("Visual jitter of MekaSuit HUD, seen when moving the player's head. Bigger value = more jitter.")
              .defineInRange("hudJitter", 6F, 1F, 100F));
        hudCompassEnabled = CachedBooleanValue.wrap(this, builder.comment("Display a fancy compass when the MekaSuit is worn.")
              .define("mekaSuitHelmetCompass", true));
        builder.comment("Last Window Positions. In general these values should not be modified manually.").push(GUI_WINDOW_CATEGORY);
        for (WindowType windowType : WindowType.values()) {
            for (String savePath : windowType.getSavePaths()) {
                builder.push(savePath);
                lastWindowPositions.put(savePath, new CachedWindowPosition(
                      CachedIntValue.wrap(this, builder.define("x", Integer.MAX_VALUE)),
                      CachedIntValue.wrap(this, builder.define("y", Integer.MAX_VALUE)),
                      windowType.canPin() ? CachedBooleanValue.wrap(this, builder.define("pinned", false)) : null
                ));
                builder.pop();
            }
        }
        builder.pop(2);

        builder.comment("QIO Config").push(QIO_CATEGORY);
        qioItemViewerSortType = CachedEnumValue.wrap(this, builder.comment("Sorting strategy when viewing items in a QIO Item Viewer.")
              .defineEnum("itemViewerSortType", ListSortType.NAME));
        qioItemViewerSortDirection = CachedEnumValue.wrap(this, builder.comment("Sorting direction when viewing items in a QIO Item Viewer.")
              .defineEnum("itemViewerSortDirection", SortDirection.ASCENDING));
        qioItemViewerSlotsX = CachedIntValue.wrap(this, builder.comment("Number of slots to view horizontally on a QIO Item Viewer.")
              .defineInRange("itemViewerSlotsX", 8, QIOItemViewerContainer.SLOTS_X_MIN, QIOItemViewerContainer.SLOTS_X_MAX));
        qioItemViewerSlotsY = CachedIntValue.wrap(this, builder.comment("Number of slots to view vertically on a QIO Item Viewer.")
              .defineInRange("itemViewerSlotsY", 4, QIOItemViewerContainer.SLOTS_Y_MIN, QIOItemViewerContainer.SLOTS_Y_MAX));
        qioRejectsToInventory = CachedBooleanValue.wrap(this, builder.comment("Determines if items in a QIO crafting window should be moved to the player's inventory or frequency first when replacing the items with a recipe viewer.")
              .define("rejectsToInventory", false));
        qioAutoFocusSearchBar = CachedBooleanValue.wrap(this, builder.comment("Determines whether the search bar is automatically focused when a QIO Dashboard is opened.")
              .define("autoFocusSearchBar", true));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "client";
    }

    @Override
    public String getTranslation() {
        return "Client Config";
    }

    @Override
    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}