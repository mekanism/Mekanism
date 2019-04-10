package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum InductionCellTier implements ITier {
    BASIC(1E9D),
    ADVANCED(8E9D),
    ELITE(64E9D),
    ULTIMATE(512E9D);

    private final double baseMaxEnergy;
    private final BaseTier baseTier;

    InductionCellTier(double max) {
        baseMaxEnergy = max;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getMaxEnergy() {
        return MekanismConfig.current().general.tiers.get(baseTier).InductionCellMaxEnergy.val();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }
}