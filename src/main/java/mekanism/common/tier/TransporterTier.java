package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum TransporterTier implements ITier {
    BASIC(1, 5),
    ADVANCED(16, 10),
    ELITE(32, 20),
    ULTIMATE(64, 50);

    private final int basePull;
    private final int baseSpeed;
    private final BaseTier baseTier;

    TransporterTier(int pull, int s) {
        basePull = pull;
        baseSpeed = s;
        baseTier = BaseTier.values()[ordinal()];
    }

    public static TransporterTier get(BaseTier tier) {
        for (TransporterTier transmitter : values()) {
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

    public int getPullAmount() {
        return MekanismConfig.current().general.tiers.get(baseTier).TransporterPullAmount.val();
    }

    public int getSpeed() {
        return MekanismConfig.current().general.tiers.get(baseTier).TransporterSpeed.val();
    }

    public int getBasePull() {
        return basePull;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }
}