package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.util.EnumUtils;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum CableTier implements ITier {
    BASIC(BaseTier.BASIC, 8_000L),
    ADVANCED(BaseTier.ADVANCED, 128_000L),
    ELITE(BaseTier.ELITE, 1_024_000L),
    ULTIMATE(BaseTier.ULTIMATE, 8_192_000L);

    private final long baseCapacity;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue capacityReference;

    CableTier(BaseTier tier, long capacity) {
        baseCapacity = capacity;
        baseTier = tier;
    }

    public static CableTier get(BaseTier tier) {
        for (CableTier transmitter : EnumUtils.CABLE_TIERS) {
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

    public long getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public long getBaseCapacity() {
        return baseCapacity;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}