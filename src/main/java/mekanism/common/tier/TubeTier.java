package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.EnumUtils;

public enum TubeTier implements ITier {
    BASIC(BaseTier.BASIC, 4_000, 750),
    ADVANCED(BaseTier.ADVANCED, 16_000, 2_000),
    ELITE(BaseTier.ELITE, 256_000, 64_000),
    ULTIMATE(BaseTier.ULTIMATE, 1_024_000, 256_000);

    private final long baseCapacity;
    private final long basePull;
    private final BaseTier baseTier;
    private CachedLongValue capacityReference;
    private CachedLongValue pullReference;

    TubeTier(BaseTier tier, long capacity, long pullAmount) {
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

    public long getTubeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public long getTubePullAmount() {
        return pullReference == null ? getBasePull() : pullReference.get();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    public long getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the TubeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue capacityReference, CachedLongValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}