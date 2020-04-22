package mekanism.common.tier;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.config.value.CachedFloatingLongValue;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum InductionProviderTier implements ITier {
    BASIC(BaseTier.BASIC, FloatingLong.createConst(256_000)),
    ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(2_048_000)),
    ELITE(BaseTier.ELITE, FloatingLong.createConst(16_384_000)),
    ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(131_072_000));

    private final FloatingLong baseOutput;
    private final BaseTier baseTier;
    private CachedFloatingLongValue outputReference;

    InductionProviderTier(BaseTier tier, FloatingLong out) {
        baseOutput = out;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public FloatingLong getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public FloatingLong getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionProviderTier a reference to the actual config value object
     */
    public void setConfigReference(CachedFloatingLongValue outputReference) {
        this.outputReference = outputReference;
    }
}