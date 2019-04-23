package mekanism.common.tier;

import mekanism.common.config.MekanismConfig;

public enum BinTier implements ITier {
    BASIC(4096),
    ADVANCED(8192),
    ELITE(32768),
    ULTIMATE(262144),
    CREATIVE(Integer.MAX_VALUE);

    private final int baseStorage;
    private final BaseTier baseTier;

    BinTier(int s) {
        baseStorage = s;
        baseTier = BaseTier.values()[ordinal()];
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public int getStorage() {
        return MekanismConfig.current().general.tiers.get(baseTier).BinStorage.val();
    }

    public int getBaseStorage() {
        return baseStorage;
    }
}