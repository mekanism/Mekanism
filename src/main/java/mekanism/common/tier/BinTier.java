package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import org.jspecify.annotations.Nullable;

//TODO - 26.2: Do we want to up the default config limits for any of these tiers?
public enum BinTier implements ITier {
    BASIC(BaseTier.BASIC, 4_096),
    ADVANCED(BaseTier.ADVANCED, 8_192),
    ELITE(BaseTier.ELITE, 32_768),
    ULTIMATE(BaseTier.ULTIMATE, 262_144),
    CREATIVE(BaseTier.CREATIVE, Long.MAX_VALUE);

    private final long baseStorage;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue storageReference;

    BinTier(BaseTier tier, long storage) {
        baseTier = tier;
        baseStorage = storage;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public long getStorage() {
        return storageReference == null ? getBaseStorage() : storageReference.getOrDefault();
    }

    public long getBaseStorage() {
        return baseStorage;
    }

    /// ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
    public void setConfigReference(CachedLongValue storageReference) {
        this.storageReference = storageReference;
    }
}