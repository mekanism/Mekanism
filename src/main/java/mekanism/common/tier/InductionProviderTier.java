package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum InductionProviderTier implements ITier {
    BASIC(64000),
    ADVANCED(512000),
    ELITE(4096000),
    ULTIMATE(32768000);

    private final double baseOutput;
    private final BaseTier baseTier;

    InductionProviderTier(double out) {
        baseOutput = out;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getOutput() {
        return MekanismConfig.current().general.tiers.get(baseTier).InductionProviderOutput.val();
    }

    public double getBaseOutput() {
        return baseOutput;
    }
}