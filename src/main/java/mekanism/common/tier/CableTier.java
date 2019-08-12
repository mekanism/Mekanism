package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.IntValue;

//TODO: Should cable capacity be upped to a double
public enum CableTier implements ITier {
    BASIC(3_200),
    ADVANCED(12_800),
    ELITE(64_000),
    ULTIMATE(320_000);

    private final int baseCapacity;
    private final BaseTier baseTier;
    private IntValue capacityReference;

    CableTier(int capacity) {
        baseCapacity = capacity;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static CableTier get(BaseTier tier) {
        for (CableTier transmitter : values()) {
            if (transmitter.getBaseTier() == tier) {
                return transmitter;
            }
        }
        return BASIC;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}