package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum CableTier implements ITier {
    BASIC(3200),
    ADVANCED(12800),
    ELITE(64000),
    ULTIMATE(320000);

    private final int baseCapacity;
    private final BaseTier baseTier;

    CableTier(int capacity) {
        baseCapacity = capacity;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static CableTier get(BaseTier tier) {
        for (CableTier transmitter : values()) {
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

    public int getCableCapacity() {
        return MekanismConfig.current().general.tiers.get(baseTier).CableCapacity.val();
    }

    public int getBaseCapacity() {
        return baseCapacity;
    }
}