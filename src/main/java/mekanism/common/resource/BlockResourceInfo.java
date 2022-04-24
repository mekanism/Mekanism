package mekanism.common.resource;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;

public enum BlockResourceInfo implements IResource {
    OSMIUM("osmium", 7.5F, 12, MaterialColor.COLOR_CYAN),
    RAW_OSMIUM("raw_osmium", 7.5F, 12, MaterialColor.COLOR_CYAN, Material.STONE),
    TIN("tin", 5, 6, MaterialColor.TERRACOTTA_WHITE),
    RAW_TIN("raw_tin", 5, 6, MaterialColor.TERRACOTTA_WHITE, Material.STONE),
    LEAD("lead", 5, 9, MaterialColor.COLOR_LIGHT_GRAY),
    RAW_LEAD("raw_lead", 5, 9, MaterialColor.COLOR_LIGHT_GRAY, Material.STONE),
    URANIUM("uranium", 5, 9, MaterialColor.GRASS),
    RAW_URANIUM("raw_uranium", 5, 9, MaterialColor.GRASS, Material.STONE),
    CHARCOAL("charcoal", 5, 6, MaterialColor.COLOR_BLACK, Material.STONE, 16_000),
    FLUORITE("fluorite", 5, 9, MaterialColor.SNOW),
    BRONZE("bronze", 5, 9, MaterialColor.COLOR_ORANGE),
    STEEL("steel", 5, 9, MaterialColor.STONE),
    REFINED_OBSIDIAN("refined_obsidian", 50, 2_400, MaterialColor.COLOR_PURPLE, Material.STONE, -1, 8, false, true, PushReaction.BLOCK),
    REFINED_GLOWSTONE("refined_glowstone", 5, 6, MaterialColor.COLOR_YELLOW, Material.STONE, -1, 15);

    private final String registrySuffix;
    private final MaterialColor materialColor;
    private final PushReaction pushReaction;
    private final boolean portalFrame;
    private final boolean burnsInFire;
    private final Material material;
    private final float resistance;
    private final float hardness;
    private final int burnTime;
    //Number between 0 and 15
    private final int lightValue;

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MaterialColor materialColor) {
        this(registrySuffix, hardness, resistance, materialColor, Material.METAL);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MaterialColor materialColor, Material material) {
        this(registrySuffix, hardness, resistance, materialColor, material, -1);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MaterialColor materialColor, Material material, int burnTime) {
        this(registrySuffix, hardness, resistance, materialColor, material, burnTime, 0);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MaterialColor materialColor, Material material, int burnTime, int lightValue) {
        this(registrySuffix, hardness, resistance, materialColor, material, burnTime, lightValue, true, false, PushReaction.NORMAL);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, MaterialColor materialColor, Material material, int burnTime, int lightValue,
          boolean burnsInFire, boolean portalFrame, PushReaction pushReaction) {
        this.registrySuffix = registrySuffix;
        this.pushReaction = pushReaction;
        this.portalFrame = portalFrame;
        this.burnsInFire = burnsInFire;
        this.burnTime = burnTime;
        this.lightValue = lightValue;
        this.resistance = resistance;
        this.hardness = hardness;
        this.material = material;
        this.materialColor = materialColor;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }

    public float getHardness() {
        return hardness;
    }

    public float getResistance() {
        return resistance;
    }

    public int getLightValue() {
        return lightValue;
    }

    public boolean isPortalFrame() {
        return portalFrame;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean burnsInFire() {
        return burnsInFire;
    }

    public PushReaction getPushReaction() {
        return pushReaction;
    }

    public Material getMaterial() {
        return material;
    }

    public MaterialColor getMaterialColor() {
        return materialColor;
    }
}