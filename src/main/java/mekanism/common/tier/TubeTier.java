package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum TubeTier implements ITier {
    BASIC(256, 64),
    ADVANCED(1024, 256),
    ELITE(4096, 1024),
    ULTIMATE(16384, 4096);

    private final int baseCapacity;
    private final int basePull;
    private final BaseTier baseTier;

    TubeTier(int capacity, int pullAmount) {
        baseCapacity = capacity;
        basePull = pullAmount;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static TubeTier get(BaseTier tier) {
        for (TubeTier transmitter : values()) {
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

    public int getTubeCapacity() {
        return MekanismConfig.current().general.tiers.get(baseTier).TubeCapacity.val();
    }

    public int getTubePullAmount() {
        return MekanismConfig.current().general.tiers.get(baseTier).TubePullAmount.val();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }

    public int getBasePull() {
        return basePull;
    }
}