package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedUnsignedLongValue;
import mekanism.common.util.EnumUtils;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum CableTier implements ITier {
    BASIC(BaseTier.BASIC, 8_000L),
    ADVANCED(BaseTier.ADVANCED, 128_000L),
    ELITE(BaseTier.ELITE, 1_024_000L),
    ULTIMATE(BaseTier.ULTIMATE, 8_192_000L);

    private final @Unsigned long baseCapacity;
    private final BaseTier baseTier;
    @Nullable
    private CachedUnsignedLongValue capacityReference;

    CableTier(BaseTier tier, @Unsigned long capacity) {
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

    public @Unsigned long getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.getOrDefault();
    }

    public @Unsigned long getBaseCapacity() {
        return baseCapacity;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
     */
    public void setConfigReference(CachedUnsignedLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}