package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum PipeTier implements ITier {
    BASIC(1000, 100),
    ADVANCED(4000, 400),
    ELITE(16000, 1600),
    ULTIMATE(64000, 6400);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;

    PipeTier(int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static PipeTier get(BaseTier tier) {
        for (PipeTier transmitter : values()) {
            if (transmitter.getBaseTier() == tier) {
                return transmitter;
            }
        }
        return BASIC;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getPipeCapacity() {
        return MekanismConfig.current().general.tiers.get(baseTier).PipeCapacity.val();
    }

    public int getPipePullAmount() {
        return MekanismConfig.current().general.tiers.get(baseTier).PipePullAmount.val();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }
}