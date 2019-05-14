package mekanism.common.tier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;

public enum PipeTier implements ITier<PipeTier> {
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
        baseTier = BaseTier.get(ordinal());
    }

    public static PipeTier getDefault() {
        return BASIC;
    }

    public static PipeTier get(int index) {
        if (index < 0 || index >= values().length) {
            return getDefault();
        }
        return values()[index];
    }

    public static PipeTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return ordinal() + 1 < values().length;
    }

    @Nullable
    @Override
    public PipeTier next() {
        return hasNext() ? get(ordinal() + 1) : null;
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