package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.util.EnumUtils;

public enum TransporterTier implements ITier {
    BASIC(BaseTier.BASIC, 1, 5),
    ADVANCED(BaseTier.ADVANCED, 16, 10),
    ELITE(BaseTier.ELITE, 32, 20),
    ULTIMATE(BaseTier.ULTIMATE, 64, 50);

    private final int basePull;
    private final int baseSpeed;
    private final BaseTier baseTier;
    private CachedIntValue pullReference;
    private CachedIntValue speedReference;

    TransporterTier(BaseTier tier, int pull, int s) {
        basePull = pull;
        baseSpeed = s;
        baseTier = tier;
    }

    public static TransporterTier get(BaseTier tier) {
        for (TransporterTier transmitter : EnumUtils.TRANSPORTER_TIERS) {
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

    public int getPullAmount() {
        return pullReference == null ? getBasePull() : pullReference.getOrDefault();
    }

    //TODO - 1.21: Figure this out as speed is configured as per half second??
    public int getSpeed() {
        return speedReference == null ? getBaseSpeed() : speedReference.getOrDefault();
    }

    public int getBasePull() {
        return basePull;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the TransporterTier a reference to the actual config value object
     */
    public void setConfigReference(CachedIntValue pullReference, CachedIntValue speedReference) {
        this.pullReference = pullReference;
        this.speedReference = speedReference;
    }
}