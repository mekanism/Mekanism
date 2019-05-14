package mekanism.common.tier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;

public enum CableTier implements ITier<CableTier> {
    BASIC(3200),
    ADVANCED(12800),
    ELITE(64000),
    ULTIMATE(320000);

    private final int baseCapacity;
    private final BaseTier baseTier;

    CableTier(int capacity) {
        baseCapacity = capacity;
        baseTier = BaseTier.get(ordinal());
    }

    public static CableTier getDefault() {
        return BASIC;
    }

    public static CableTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static CableTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return EnumUtils.hasNext(values(), ordinal());
    }

    @Nullable
    @Override
    public CableTier next() {
        return EnumUtils.nextValue(values(), ordinal());
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