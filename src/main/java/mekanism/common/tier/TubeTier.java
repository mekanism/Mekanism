package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public enum TubeTier implements ITier {
    BASIC(256, 64),
    ADVANCED(1024, 256),
    ELITE(4096, 1024),
    ULTIMATE(16384, 4096);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private IntValue capacityReference;
    private IntValue pullReference;

    TubeTier(int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static TubeTier get(BaseTier tier) {
        for (TubeTier transmitter : values()) {
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

    public int getTubeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getTubePullAmount() {
        return pullReference == null ? getBasePull() : pullReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the TubeTier a reference to the actual config value object
     */
    public void setConfigReference(IntValue capacityReference, IntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}