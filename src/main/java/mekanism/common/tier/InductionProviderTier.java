package mekanism.common.tier;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedLongValue;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public enum InductionProviderTier implements ITier {
    BASIC(BaseTier.BASIC, 256_000L),
    ADVANCED(BaseTier.ADVANCED, 2_048_000L),
    ELITE(BaseTier.ELITE, 16_384_000L),
    ULTIMATE(BaseTier.ULTIMATE, 131_072_000L);

    private final long baseOutput;
    private final BaseTier baseTier;
    @Nullable
    private CachedLongValue outputReference;

    InductionProviderTier(BaseTier tier, long out) {
        baseOutput = out;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public long getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.getOrDefault();
    }

    public long getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionProviderTier a reference to the actual config value object
     */
    public void setConfigReference(CachedLongValue outputReference) {
        this.outputReference = outputReference;
    }
}