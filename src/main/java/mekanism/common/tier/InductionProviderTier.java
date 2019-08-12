package mekanism.common.tier;

import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public enum InductionProviderTier implements ITier {
    BASIC(64000),
    ADVANCED(512000),
    ELITE(4096000),
    ULTIMATE(32768000);

    private final double baseOutput;
    private final BaseTier baseTier;
    private DoubleValue outputReference;

    InductionProviderTier(double out) {
        baseOutput = out;
        baseTier = BaseTier.values()[ordinal()];
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