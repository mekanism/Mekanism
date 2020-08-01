package mekanism.common.resource;

import net.minecraft.block.material.PushReaction;

public enum BlockResourceInfo implements IResource {
    OSMIUM("osmium", 7.5F, 20, 1),
    TIN("tin", 5, 10, 1),
    COPPER("copper", 5, 10, 1),
    LEAD("lead", 5, 10, 1),
    URANIUM("uranium", 5, 10, 1),
    CHARCOAL("charcoal", 5, 10, 0, 16_000),
    BRONZE("bronze", 5, 15, 1),
    STEEL("steel", 5, 15, 1),
    REFINED_OBSIDIAN("refined_obsidian", 50, 4_000, 2, -1, 8, false, true, PushReaction.BLOCK),
    REFINED_GLOWSTONE("refined_glowstone", 5, 10, 1, -1, 15);

    private final String registrySuffix;
    private final PushReaction pushReaction;
    private final boolean portalFrame;
    private final boolean burnsInFire;
    private final float resistance;
    private final float hardness;
    private final int burnTime;
    //Number between 0 and 15
    private final int lightValue;
    private final int harvestLevel;

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel) {
        this(registrySuffix, hardness, resistance, harvestLevel, -1);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int burnTime) {
        this(registrySuffix, hardness, resistance, harvestLevel, burnTime, 0);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int burnTime, int lightValue) {
        this(registrySuffix, hardness, resistance, harvestLevel, burnTime, lightValue, true, false, PushReaction.NORMAL);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int burnTime, int lightValue, boolean burnsInFire, boolean portalFrame,
          PushReaction pushReaction) {
        this.registrySuffix = registrySuffix;
        this.pushReaction = pushReaction;
        this.portalFrame = portalFrame;
        this.burnsInFire = burnsInFire;
        this.burnTime = burnTime;
        this.lightValue = lightValue;
        this.resistance = resistance;
        this.hardness = hardness;
        this.harvestLevel = harvestLevel;
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

    public int getHarvestLevel() {
        return harvestLevel;
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
}