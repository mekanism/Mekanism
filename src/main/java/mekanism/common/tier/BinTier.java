package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedIntValue;

public enum BinTier implements ITier {
    BASIC(BaseTier.BASIC, 4_096),
    ADVANCED(BaseTier.ADVANCED, 8_192),
    ELITE(BaseTier.ELITE, 32_768),
    ULTIMATE(BaseTier.ULTIMATE, 262_144),
    CREATIVE(BaseTier.CREATIVE, Integer.MAX_VALUE);

    private final int baseStorage;
    private final BaseTier baseTier;
    private CachedIntValue storageReference;

    BinTier(BaseTier tier, int s) {
        baseTier = tier;
        baseStorage = s;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.getOrDefault();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
     */
    public void setConfigReference(CachedIntValue storageReference) {
        this.storageReference = storageReference;
    }
}