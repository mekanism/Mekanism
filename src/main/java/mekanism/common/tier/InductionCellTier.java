package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedDoubleValue;

public enum InductionCellTier implements ITier {
    BASIC(BaseTier.BASIC, 1E9D),
    ADVANCED(BaseTier.ADVANCED, 8E9D),
    ELITE(BaseTier.ELITE, 64E9D),
    ULTIMATE(BaseTier.ULTIMATE, 512E9D);

    private final double baseMaxEnergy;
    private final BaseTier baseTier;
    private CachedDoubleValue storageReference;

    InductionCellTier(BaseTier tier, double max) {
        baseMaxEnergy = max;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.get();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionCellTier a reference to the actual config value object
     */
    public void setConfigReference(CachedDoubleValue storageReference) {
        this.storageReference = storageReference;
    }
}