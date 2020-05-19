package mekanism.common.tier;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.util.EnumUtils;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum CableTier implements ITier {
    BASIC(BaseTier.BASIC, FloatingLong.createConst(8_000)),
    ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(128_000)),
    ELITE(BaseTier.ELITE, FloatingLong.createConst(1_024_000)),
    ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(8_192_000));

    private final FloatingLong baseCapacity;
    private final BaseTier baseTier;
    private CachedFloatingLongValue capacityReference;

    CableTier(BaseTier tier, FloatingLong capacity) {
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

    public FloatingLong getCableCapacity() {
        return capacityReference == null ? getBaseCapacity() : capacityReference.get();
    }

    public FloatingLong getBaseCapacity() {
        return baseCapacity;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the CableTier a reference to the actual config value object
     */
    public void setConfigReference(CachedFloatingLongValue capacityReference) {
        this.capacityReference = capacityReference;
    }
}