package mekanism.common.resource;

public enum BlockResourceInfo implements INamedResource {
    OSMIUM("osmium", "Osmium", 7.5F, 20.0F),
    TIN("tin", "Tin", 5.0F, 10.0F),
    COPPER("copper", "Copper", 5.0F, 10.0F),
    CHARCOAL("charcoal", "Charcoal", 5.0F, 10.0F, 0, false),
    BRONZE("bronze", "Bronze", 5.0F, 15.0F),
    STEEL("steel", "Steel", 5.0F, 15.0F),
    REFINED_OBSIDIAN("refined_obsidian", "RefinedObsidian", 50.0F, 4000.0F, 8),
    REFINED_GLOWSTONE("refined_glowstone", "RefinedGlowstone", 5.0F, 10.0F, 15);

    private final String registrySuffix;
    private final String oreSuffix;
    private final boolean beaconBase;
    private final float resistance;
    private final float hardness;
    //Number between 0 and 15
    private final int lightValue;

    BlockResourceInfo(String registrySuffix, String oreSuffix, float hardness, float resistance) {
        this(registrySuffix, null, hardness, resistance, 0);
    }

    BlockResourceInfo(String registrySuffix, String oreSuffix, float hardness, float resistance, int lightValue) {
        this(registrySuffix, null, hardness, resistance, lightValue, true);
    }

    BlockResourceInfo(String registrySuffix, String oreSuffix, float hardness, float resistance, int lightValue, boolean beaconBase) {
        this.registrySuffix = registrySuffix;
        this.oreSuffix = oreSuffix;
        this.beaconBase = beaconBase;
        this.lightValue = lightValue;
        this.resistance = resistance;
        this.hardness = hardness;
    }

    @Override
    public String getRegistrySuffix() {
        return registrySuffix;
    }

    @Override
    public String getOreSuffix() {
        return oreSuffix;
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

    public boolean isBeaconBase() {
        return beaconBase;
    }
}