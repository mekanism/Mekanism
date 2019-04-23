package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum FluidTankTier implements ITier {
    BASIC(14000, 400),
    ADVANCED(28000, 800),
    ELITE(56000, 1600),
    ULTIMATE(112000, 3200),
    CREATIVE(Integer.MAX_VALUE, Integer.MAX_VALUE / 2);

    private final int baseStorage;
    private final int baseOutput;
    private final BaseTier baseTier;

    FluidTankTier(int s, int o) {
        baseStorage = s;
        baseOutput = o;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getStorage() {
        return MekanismConfig.current().general.tiers.get(baseTier).FluidTankStorage.val();
    }

    public int getOutput() {
        return MekanismConfig.current().general.tiers.get(baseTier).FluidTankOutput.val();
    }

    public int getBaseStorage() {
        return baseStorage;
    }

    public int getBaseOutput() {
        return baseOutput;
    }
}