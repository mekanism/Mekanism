package mekanism.common.config;

import mekanism.common.config.value.CachedBooleanValue;
import mekanism.common.config.value.CachedEnumValue;
import mekanism.common.config.value.CachedFloatValue;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.QIOItemViewerContainer.ListSortType;
import mekanism.common.inventory.container.QIOItemViewerContainer.SortDirection;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ClientConfig extends BaseMekanismConfig {

    private static final String GUI_CATEGORY = "gui";
    private static final String QIO_CATEGORY = "qio";

    private final ForgeConfigSpec configSpec;

    public final CachedBooleanValue enablePlayerSounds;
    public final CachedBooleanValue enableMachineSounds;
    public final CachedBooleanValue holidays;
    public final CachedFloatValue baseSoundVolume;
    public final CachedBooleanValue machineEffects;
    public final CachedBooleanValue enableAmbientLighting;
    public final CachedIntValue ambientLightingLevel;
    public final CachedBooleanValue opaqueTransmitters;
    public final CachedBooleanValue allowModeScroll;
    public final CachedBooleanValue enableMultiblockFormationParticles;
    public final CachedBooleanValue alignHUDLeft;
    public final CachedBooleanValue enableHUD;
    public final CachedIntValue radiationParticleRadius;
    public final CachedIntValue radiationParticleCount;
    public final CachedIntValue energyColor;

    public final CachedFloatValue hudOpacity;
    public final CachedIntValue hudColor;
    public final CachedIntValue hudWarningColor;
    public final CachedIntValue hudDangerColor;
    public final CachedFloatValue hudJitter;
    public final CachedBooleanValue hudCompassEnabled;

    public final CachedEnumValue<ListSortType> qioItemViewerSortType;
    public final CachedEnumValue<SortDirection> qioItemViewerSortDirection;
    public final CachedIntValue qioItemViewerSlotsX;
    public final CachedIntValue qioItemViewerSlotsY;

    ClientConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client Config. This config only exists on the client").push("client");

        enablePlayerSounds = CachedBooleanValue.wrap(this, builder.comment("Play sounds for Jetpack/Gas Mask/Flamethrower/Radiation (all players).")
              .define("enablePlayerSounds", true));
        enableMachineSounds = CachedBooleanValue.wrap(this, builder.comment("If enabled machines play their sounds while running.")
              .define("enableMachineSounds", true));
        holidays = CachedBooleanValue.wrap(this, builder.comment("Christmas/New Years greetings in chat.")
              .define("holidays", true));
        baseSoundVolume = CachedFloatValue.wrap(this, builder.comment("Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder.")
              .defineInRange("baseSoundVolume", 1, 0, Float.MAX_VALUE));
        machineEffects = CachedBooleanValue.wrap(this, builder.comment("Show particles when machines active.")
              .define("machineEffects", true));
        enableAmbientLighting = CachedBooleanValue.wrap(this, builder.comment("Should active machines produce block light.")
              .define("enableAmbientLighting", true));
        ambientLightingLevel = CachedIntValue.wrap(this, builder.comment("How much light to produce if ambient lighting is enabled.")
              .defineInRange("ambientLightingLevel", 15, 1, 15));
        opaqueTransmitters = CachedBooleanValue.wrap(this, builder.comment("If true, don't render Cables/Pipes/Tubes as transparent and don't render their contents.")
              .define("opaqueTransmitters", false));
        allowModeScroll = CachedBooleanValue.wrap(this, builder.comment("Allow sneak + scroll to change item modes.")
              .define("allowModeScroll", true));
        enableMultiblockFormationParticles = CachedBooleanValue.wrap(this, builder.comment("Set to false to prevent particle spam when loading multiblocks (notification message will display instead).")
              .define("enableMultiblockFormationParticles", true));
        alignHUDLeft = CachedBooleanValue.wrap(this, builder.comment("Align HUD with left (if true) or right (if false)")
              .define("alignHUDLeft", true));
        enableHUD = CachedBooleanValue.wrap(this, builder.comment("Enable item information HUD during gameplay")
              .define("enableHUD", true));
        radiationParticleRadius = CachedIntValue.wrap(this, builder.comment("How far (in blocks) from the player radiation particles can spawn.")
              .define("radiationParticleRadius", 30));
        radiationParticleCount = CachedIntValue.wrap(this, builder.comment("How many particles spawn when rendering radiation effects (scaled by radiation level).")
              .define("radiationParticleCount", 100));
        energyColor = CachedIntValue.wrap(this, builder.comment("Color of energy in item durability display.")
              .define("energyColor", 0x3CFE9A));

        builder.comment("GUI Config").push(GUI_CATEGORY);
        hudOpacity = CachedFloatValue.wrap(this, builder.comment("Opacity of HUD used by MekaSuit.")
              .defineInRange("hudOpacity", 0.4F, 0, 1));
        hudColor = CachedIntValue.wrap(this, builder.comment("Color of HUD used by MekaSuit.")
              .define("hudColor", 0x40F5F0));
        hudWarningColor = CachedIntValue.wrap(this, builder.comment("Color of warning HUD elements used by MekaSuit.")
              .define("hudWarningColor", 0xFFDD4F));
        hudDangerColor = CachedIntValue.wrap(this, builder.comment("Color of danger HUD elements used by MekaSuit.")
              .define("hudDangerColor", 0xFF383C));
        hudJitter = CachedFloatValue.wrap(this, builder.comment("Visual jitter of MekaSuit HUD, seen when moving the player's head. Bigger value = more jitter.")
              .defineInRange("hudJitter", 6F, 1F, 100F));
        hudCompassEnabled = CachedBooleanValue.wrap(this, builder.comment("Display a fancy compass when the MekaSuit is worn.")
              .define("mekaSuitHelmetCompass", true));
        builder.pop();

        builder.comment("QIO Config").push(QIO_CATEGORY);
        qioItemViewerSortType = CachedEnumValue.wrap(this, builder.comment("Sorting strategy when viewing items in a QIO Item Viewer.")
              .defineEnum("itemViewerSortType", ListSortType.NAME));
        qioItemViewerSortDirection = CachedEnumValue.wrap(this, builder.comment("Sorting direction when viewing items in a QIO Item Viewer.")
              .defineEnum("itemViewerSortDirection", SortDirection.ASCENDING));
        qioItemViewerSlotsX = CachedIntValue.wrap(this, builder.comment("Number of slots to view horizontally on a QIO Item Viewer.")
              .defineInRange("itemViewerSlotsX", 8, QIOItemViewerContainer.SLOTS_X_MIN, QIOItemViewerContainer.SLOTS_X_MAX));
        qioItemViewerSlotsY = CachedIntValue.wrap(this, builder.comment("Number of slots to view vertically on a QIO Item Viewer.")
              .defineInRange("itemViewerSlotsY", 4, QIOItemViewerContainer.SLOTS_Y_MIN, QIOItemViewerContainer.SLOTS_Y_MAX));
        builder.pop();

        builder.pop();
        configSpec = builder.build();
    }

    @Override
    public String getFileName() {
        return "client";
    }

    @Override
    public ForgeConfigSpec getConfigSpec() {
        return configSpec;
    }

    @Override
    public Type getConfigType() {
        return Type.CLIENT;
    }
}