package mekanism.common.tier;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;

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
        baseTier = BaseTier.get(ordinal());
    }

    public static BinTier getDefault() {
        return BASIC;
    }

    public static BinTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static BinTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
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