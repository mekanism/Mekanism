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
import mekanism.common.inventory.container.SelectedWindowData.WindowType.ConfigSaveData;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig extends BaseMekanismConfig {

    private final ModConfigSpec configSpec;

    public final CachedBooleanValue whiteRadialText;
    public final CachedIntValue energyColor;
    public final CachedBooleanValue allowModeScroll;

    public final CachedBooleanValue opaqueTransmitters;
    public final CachedIntValue berRange;

    public final CachedFloatValue baseSoundVolume;
    public final CachedBooleanValue enablePlayerSounds;
    public final CachedBooleanValue enableMachineSounds;

    public final CachedBooleanValue enableMultiblockFormationParticles;
    public final CachedBooleanValue machineEffects;
    public final CachedIntValue radiationParticleRadius;
    public final CachedIntValue radiationParticleCount;
    public final CachedBooleanValue renderMagneticAttractionParticles;
    public final CachedBooleanValue renderToolAOEParticles;

    public final CachedBooleanValue enableHUD;
    public final CachedFloatValue hudScale;
    public final CachedBooleanValue reverseHUD;
    public final CachedFloatValue hudOpacity;
    public final CachedIntValue hudColor;
    public final CachedIntValue hudWarningColor;
    public final CachedIntValue hudDangerColor;
    public final CachedFloatValue hudJitter;
    public final CachedBooleanValue hudCompassEnabled;
    public final CachedBooleanValue hudAvoidSoundSubtitleOverlay;
    public final CachedBooleanValue hudRenderMekaSuitEnergyBar;

    public final CachedEnumValue<ListSortType> qioItemViewerSortType;
    public final CachedEnumValue<SortDirection> qioItemViewerSortDirection;
    public final CachedIntValue qioItemViewerSlotsX;
    public final CachedIntValue qioItemViewerSlotsY;
    public final CachedBooleanValue qioAutoFocusSearchBar;
    public final CachedBooleanValue qioRejectsToInventory;

    public final Map<String, CachedWindowPosition> lastWindowPositions = new HashMap<>();

    ClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        MekanismConfigTranslations.CLIENT_ACCESSIBILITY.applyToBuilder(builder).push("accessibility");
        whiteRadialText = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_WHITE_RADIAL_TEXT.applyToBuilder(builder)
              .define("whiteRadialText", false));
        opaqueTransmitters = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_OPAQUE_TRANSMITTERS.applyToBuilder(builder)
              .gameRestart()
              .define("opaqueTransmitters", false));
        allowModeScroll = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_SCROLL_MODE_CHANGE.applyToBuilder(builder)
              .define("allowModeScroll", true));
        builder.pop();

        MekanismConfigTranslations.CLIENT_SOUNDS.applyToBuilder(builder).push("sounds");
        baseSoundVolume = CachedFloatValue.wrap(this, MekanismConfigTranslations.CLIENT_BASE_SOUND_VOLUME.applyToBuilder(builder)
              .defineInRange("baseVolume", 1D, 0, 10));
        enablePlayerSounds = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_PLAYER_SOUNDS_ENABLED.applyToBuilder(builder)
              .define("enablePlayer", true));
        enableMachineSounds = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_MACHINE_SOUNDS_ENABLED.applyToBuilder(builder)
              .define("enableMachine", true));
        builder.pop();

        MekanismConfigTranslations.CLIENT_RENDERING.applyToBuilder(builder).push("rendering");
        energyColor = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_ENERGY_COLOR.applyToBuilder(builder)
              .define("energyColor", 0x3CFE9A));
        berRange = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_BE_RENDER_RANGE.applyToBuilder(builder)
              .defineInRange("berRange", 256, 1, 1_024));

        MekanismConfigTranslations.CLIENT_PARTICLE.applyToBuilder(builder).push("particle");
        enableMultiblockFormationParticles = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_MULTIBLOCK_FORMATION.applyToBuilder(builder)
              .define("multiblockFormation", true));
        machineEffects = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_MACHINE_EFFECTS.applyToBuilder(builder)
              .define("machineEffects", true));
        radiationParticleRadius = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_RADIATION_RADIUS.applyToBuilder(builder)
              .defineInRange("radiationParticleRadius", 30, 2, 64));
        radiationParticleCount = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_RADIATION_COUNT.applyToBuilder(builder)
              .defineInRange("radiationParticleCount", 100, 0, 1_000));
        renderMagneticAttractionParticles = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_MAGNETIC_ATTRACTION.applyToBuilder(builder)
              .define("magneticAttraction", true));
        renderToolAOEParticles = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_PARTICLE_TOOL_AOE.applyToBuilder(builder)
              .define("toolAOE", true));
        builder.pop();
        builder.pop();//pop rendering

        MekanismConfigTranslations.CLIENT_HUD.applyToBuilder(builder).push("hud");
        enableHUD = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_ENABLED.applyToBuilder(builder)
              .define("enabled", true));
        hudScale = CachedFloatValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_SCALE.applyToBuilder(builder)
              .defineInRange("scale", 0.6, 0.25, 1));
        reverseHUD = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_REVERSE.applyToBuilder(builder)
              .define("reverse", false));
        hudOpacity = CachedFloatValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_OPACITY.applyToBuilder(builder)
              .defineInRange("opacity", 0.4F, 0, 1));
        hudColor = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_COLOR.applyToBuilder(builder)
              .defineInRange("color", 0x40F5F0, 0, 0xFFFFFF));
        hudWarningColor = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_COLOR_WARNING.applyToBuilder(builder)
              .defineInRange("warningColor", 0xFFDD4F, 0, 0xFFFFFF));
        hudDangerColor = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_COLOR_DANGER.applyToBuilder(builder)
              .defineInRange("dangerColor", 0xFF383C, 0, 0xFFFFFF));
        hudJitter = CachedFloatValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_JITTER.applyToBuilder(builder)
              .defineInRange("jitter", 6F, 1F, 100F));
        hudCompassEnabled = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_COMPASS.applyToBuilder(builder)
              .define("mekaSuitHelmetCompass", true));
        hudAvoidSoundSubtitleOverlay = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_SUBTITLE_AVOID.applyToBuilder(builder)
              .define("avoidSoundSubtitleOverlap", true));
        hudRenderMekaSuitEnergyBar = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_HUD_MEKASUIT_ENERGY_BAR.applyToBuilder(builder)
              .define("renderMekaSuitEnergyBar", true));
        builder.pop();

        MekanismConfigTranslations.CLIENT_QIO.applyToBuilder(builder).push("qio");
        qioItemViewerSortType = CachedEnumValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_SORT_TYPE.applyToBuilder(builder)
              .defineEnum("sortType", ListSortType.NAME));
        qioItemViewerSortDirection = CachedEnumValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_SORT_DIRECTION.applyToBuilder(builder)
              .defineEnum("sortDirection", SortDirection.ASCENDING));
        qioItemViewerSlotsX = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_SLOTS_X.applyToBuilder(builder)
              .defineInRange("slotsWide", 8, QIOItemViewerContainer.SLOTS_X_MIN, QIOItemViewerContainer.SLOTS_X_MAX));
        qioItemViewerSlotsY = CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_SLOTS_Y.applyToBuilder(builder)
              .defineInRange("slotsTall", 4, QIOItemViewerContainer.SLOTS_Y_MIN, QIOItemViewerContainer.SLOTS_Y_MAX));
        qioAutoFocusSearchBar = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_AUTO_FOCUS.applyToBuilder(builder)
              .define("autoFocusSearchBar", true));
        qioRejectsToInventory = CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_QIO_REJECTS_DESTINATION.applyToBuilder(builder)
              .define("rejectsToInventory", false));
        builder.pop();

        MekanismConfigTranslations.CLIENT_LAST_WINDOW_POSITIONS.applyToBuilder(builder).push("window");
        for (WindowType windowType : WindowType.values()) {
            for (ConfigSaveData saveData : windowType.getSavePaths()) {
                saveData.applyToBuilder(builder).push(saveData.savePath());
                lastWindowPositions.put(saveData.savePath(), new CachedWindowPosition(
                      CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_LAST_WINDOW_POSITIONS_X.applyToBuilder(builder).define("x", Integer.MAX_VALUE)),
                      CachedIntValue.wrap(this, MekanismConfigTranslations.CLIENT_LAST_WINDOW_POSITIONS_Y.applyToBuilder(builder).define("y", Integer.MAX_VALUE)),
                      windowType.canPin() ? CachedBooleanValue.wrap(this, MekanismConfigTranslations.CLIENT_LAST_WINDOW_POSITIONS_PINNED.applyToBuilder(builder)
                            .define("pinned", false)) : null
                ));
                builder.pop();
            }
        }
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