package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;

public enum QIODriveTier implements ITier {
    BASE(BaseTier.BASIC, 16_000, 128),
    HYPER_DENSE(BaseTier.ADVANCED, 128_000, 256),
    TIME_DILATING(BaseTier.ELITE, 1_048_000, 1_024),
    SUPERMASSIVE(BaseTier.ULTIMATE, 16_000_000_000L, 8_192);

    private final BaseTier baseTier;
    private final long count;
    private final int types;

    QIODriveTier(BaseTier tier, long count, int types) {
        baseTier = tier;
        this.count = count;
        this.types = types;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public long getMaxCount() {
        return count;
    }

    public int getMaxTypes() {
        return types;
    }
}
