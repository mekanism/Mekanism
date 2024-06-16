package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.Unsigned;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.config.value.CachedUnsignedLongValue;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum InductionCellTier implements ITier {
    BASIC(BaseTier.BASIC, 8_000_000_000L),
    ADVANCED(BaseTier.ADVANCED, 64_000_000_000L),
    ELITE(BaseTier.ELITE, 512_000_000_000L),
    ULTIMATE(BaseTier.ULTIMATE, 4_000_000_000_000L);

    private final @Unsigned long baseMaxEnergy;
    private final BaseTier baseTier;
    @Nullable
    private CachedUnsignedLongValue storageReference;

    InductionCellTier(BaseTier tier, @Unsigned long max) {
        baseMaxEnergy = max;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public @Unsigned long getMaxEnergy() {
        return storageReference == null ? getBaseMaxEnergy() : storageReference.getOrDefault();
    }

    public @Unsigned long getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionCellTier a reference to the actual config value object
     */
    public void setConfigReference(CachedUnsignedLongValue storageReference) {
        this.storageReference = storageReference;
    }
}