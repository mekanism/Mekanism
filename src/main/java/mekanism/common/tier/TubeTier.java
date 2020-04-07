package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;

public enum TubeTier implements ITier {
    BASIC(BaseTier.BASIC, 256, 64),
    ADVANCED(BaseTier.ADVANCED, 4_096, 1_024),
    ELITE(BaseTier.ELITE, 32_768, 8_192),
    ULTIMATE(BaseTier.ULTIMATE, 131_072, 24_576);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private CachedIntValue capacityReference;
    private CachedIntValue pullReference;

    TubeTier(BaseTier tier, int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = tier;
    }

    public static TubeTier get(BaseTier tier) {
        for (TubeTier transmitter : EnumUtils.TUBE_TIERS) {
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
    public void setConfigReference(CachedIntValue capacityReference, CachedIntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}