package mekanism.common.tier;

import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;

public enum FactoryTier implements ITier {
    BASIC(BaseTier.BASIC, 3),
    ADVANCED(BaseTier.ADVANCED, 5),
    ELITE(BaseTier.ELITE, 7),
    ULTIMATE(BaseTier.ULTIMATE, 9);

    public final int processes;
    private final BaseTier baseTier;

    FactoryTier(BaseTier tier, int process) {
        processes = process;
        baseTier = tier;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }
}