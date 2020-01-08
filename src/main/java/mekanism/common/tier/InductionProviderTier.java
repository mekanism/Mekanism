package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public enum InductionProviderTier implements ITier {
    BASIC(BaseTier.BASIC, 64_000),
    ADVANCED(BaseTier.ADVANCED, 512_000),
    ELITE(BaseTier.ELITE, 4_096_000),
    ULTIMATE(BaseTier.ULTIMATE, 32_768_000);

    private final double baseOutput;
    private final BaseTier baseTier;
    private DoubleValue outputReference;

    InductionProviderTier(BaseTier tier, double out) {
        baseOutput = out;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getOutput() {
        return outputReference == null ? getBaseOutput() : outputReference.get();
    }

    public double getBaseOutput() {
        return baseOutput;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the InductionProviderTier a reference to the actual config value object
     */
    public void setConfigReference(DoubleValue outputReference) {
        this.outputReference = outputReference;
    }
}