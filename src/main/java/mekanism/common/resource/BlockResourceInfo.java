package mekanism.common.resource;

public enum BlockResourceInfo implements INamedResource {
    OSMIUM("osmium", 7.5F, 20.0F),
    TIN("tin", 5.0F, 10.0F),
    COPPER("copper", 5.0F, 10.0F),
    CHARCOAL("charcoal", 5.0F, 10.0F, 0, false),
    BRONZE("bronze", 5.0F, 15.0F),
    STEEL("steel", 5.0F, 15.0F),
    REFINED_OBSIDIAN("refined_obsidian", 50.0F, 4000.0F, 8),
    REFINED_GLOWSTONE("refined_glowstone", 5.0F, 10.0F, 15);

    private final String registrySuffix;
    private final boolean beaconBase;
    private final float resistance;
    private final float hardness;
    //Number between 0 and 15
    private final int lightValue;

    BlockResourceInfo(String registrySuffix, float hardness, float resistance) {
        this(registrySuffix, hardness, resistance, 0);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int lightValue) {
        this(registrySuffix, hardness, resistance, lightValue, true);
    }

    BlockResourceInfo(String registrySuffix, float hardness, float resistance, int lightValue, boolean beaconBase) {
        this.registrySuffix = registrySuffix;
        this.beaconBase = beaconBase;
        this.lightValue = lightValue;
        this.resistance = resistance;
        this.hardness = hardness;
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

    public boolean isBeaconBase() {
        return beaconBase;
    }
}