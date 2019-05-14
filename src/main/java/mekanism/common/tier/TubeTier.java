package mekanism.common.tier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;

public enum TubeTier implements ITier<TubeTier> {
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
        baseTier = BaseTier.get(ordinal());
    }

    public static TubeTier getDefault() {
        return BASIC;
    }

    public static TubeTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static TubeTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return ordinal() + 1 < values().length;
    }

    @Nullable
    @Override
    public TubeTier next() {
        return hasNext() ? get(ordinal() + 1) : null;
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