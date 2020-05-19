package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;

public enum PipeTier implements ITier {
    BASIC(BaseTier.BASIC, 4_000, 512),
    ADVANCED(BaseTier.ADVANCED, 8_000, 1_000),
    ELITE(BaseTier.ELITE, 64_000, 16_000),
    ULTIMATE(BaseTier.ULTIMATE, 512_000, 64_000);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;
    private CachedIntValue capacityReference;
    private CachedIntValue pullReference;

    PipeTier(BaseTier tier, int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = tier;
    }

    public static PipeTier get(BaseTier tier) {
        for (PipeTier transmitter : EnumUtils.PIPE_TIERS) {
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

    public int getPipeCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public int getPipePullAmount() {
        return pullReference == null ? getBasePull() : pullReference.get();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the PipeTier a reference to the actual config value object
     */
    public void setConfigReference(CachedIntValue capacityReference, CachedIntValue pullReference) {
        this.capacityReference = capacityReference;
        this.pullReference = pullReference;
    }
}