package mekanism.common.resource;

public enum BlockResourceInfo implements IResource {
    OSMIUM("osmium", 7.5F, 20.0F, 1),
    TIN("tin", 5.0F, 10.0F, 1),
    COPPER("copper", 5.0F, 10.0F, 1),
    LEAD("lead", 5.0F, 10.0F, 1),
    URANIUM("uranium", 5.0F, 10.0F, 1),
    CHARCOAL("charcoal", 5.0F, 10.0F, 0, 0, false),
    BRONZE("bronze", 5.0F, 15.0F, 1),
    STEEL("steel", 5.0F, 15.0F, 1),
    REFINED_OBSIDIAN("refined_obsidian", 50.0F, 4000.0F, 2, 8, true, true),
    REFINED_GLOWSTONE("refined_glowstone", 5.0F, 10.0F, 1, 15);

    private final String registrySuffix;
    private final boolean portalFrame;
    private final boolean beaconBase;
    private final float resistance;
    private final float hardness;
    //Number between 0 and 15
    private final int lightValue;
    private final int harvestLevel;

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel) {
        this(registrySuffix, hardness, resistance, harvestLevel, 0);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int lightValue) {
        this(registrySuffix, hardness, resistance, harvestLevel, lightValue, true);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int lightValue, boolean beaconBase) {
        this(registrySuffix, hardness, resistance, harvestLevel, lightValue, beaconBase, false);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int harvestLevel, int lightValue, boolean beaconBase, boolean portalFrame) {
        this.registrySuffix = registrySuffix;
        this.portalFrame = portalFrame;
        this.beaconBase = beaconBase;
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

    public boolean isBeaconBase() {
        return beaconBase;
    }

    public boolean isPortalFrame() {
        return portalFrame;
    }
}