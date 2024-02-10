package mekanism.common.resource;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

public enum BlockResourceInfo implements IResource {
    OSMIUM("osmium", 7.5F, 12, MapColor.COLOR_CYAN),
    RAW_OSMIUM("raw_osmium", 7.5F, 12, MapColor.COLOR_CYAN, NoteBlockInstrument.BASEDRUM),
    TIN("tin", 5, 6, MapColor.TERRACOTTA_WHITE),
    RAW_TIN("raw_tin", 5, 6, MapColor.TERRACOTTA_WHITE, NoteBlockInstrument.BASEDRUM),
    LEAD("lead", 5, 9, MapColor.COLOR_LIGHT_GRAY),
    RAW_LEAD("raw_lead", 5, 9, MapColor.COLOR_LIGHT_GRAY, NoteBlockInstrument.BASEDRUM),
    URANIUM("uranium", 5, 9, MapColor.GRASS),
    RAW_URANIUM("raw_uranium", 5, 9, MapColor.GRASS, NoteBlockInstrument.BASEDRUM),
    CHARCOAL("charcoal", 5, 6, MapColor.COLOR_BLACK, NoteBlockInstrument.BASEDRUM),
    FLUORITE("fluorite", 5, 9, MapColor.SNOW),
    BRONZE("bronze", 5, 9, MapColor.COLOR_ORANGE),
    //Note: Deepslate is closer to steel than stone or metal
    STEEL("steel", 5, 9, MapColor.DEEPSLATE),
    REFINED_OBSIDIAN("refined_obsidian", 50, 2_400, MapColor.COLOR_PURPLE, NoteBlockInstrument.BASEDRUM, 8, false, true, PushReaction.BLOCK),
    REFINED_GLOWSTONE("refined_glowstone", 5, 6, MapColor.COLOR_YELLOW, NoteBlockInstrument.BASEDRUM, 15);

    private final String registrySuffix;
    private final MapColor mapColor;
    private final PushReaction pushReaction;
    private final boolean portalFrame;
    private final boolean burnsInFire;
    private final NoteBlockInstrument instrument;
    private final float resistance;
    private final float hardness;
    //Number between 0 and 15
    private final int lightValue;

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor) {
        this(registrySuffix, hardness, resistance, mapColor, null);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument) {
        this(registrySuffix, hardness, resistance, mapColor, instrument, 0);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument, int lightValue) {
        this(registrySuffix, hardness, resistance, mapColor, instrument, lightValue, true, false, PushReaction.NORMAL);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MapColor mapColor, @Nullable NoteBlockInstrument instrument, int lightValue,
          boolean burnsInFire, boolean portalFrame, PushReaction pushReaction) {
        this.registrySuffix = registrySuffix;
        this.pushReaction = pushReaction;
        this.portalFrame = portalFrame;
        this.burnsInFire = burnsInFire;
        this.lightValue = lightValue;
        this.resistance = resistance;
        this.hardness = hardness;
        this.instrument = instrument;
        this.mapColor = mapColor;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }

    public boolean isPortalFrame() {
        return portalFrame;
    }

    public boolean burnsInFire() {
        return burnsInFire;
    }

    public MapColor getMapColor() {
        return mapColor;
    }

    public BlockBehaviour.Properties modifyProperties(BlockBehaviour.Properties properties) {
        if (instrument != null) {
            properties.instrument(instrument);
        }
        return properties.mapColor(mapColor).strength(hardness, resistance).lightLevel(state -> lightValue).pushReaction(pushReaction);
    }
}