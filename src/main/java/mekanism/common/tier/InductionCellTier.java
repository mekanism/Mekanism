package mekanism.common.tier;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.api.util.EnumUtils;

public enum InductionCellTier implements ITier {
    BASIC(1E9D),
    ADVANCED(8E9D),
    ELITE(64E9D),
    ULTIMATE(512E9D);

    private final double baseMaxEnergy;
    private final BaseTier baseTier;

    InductionCellTier(double max) {
        baseMaxEnergy = max;
        baseTier = BaseTier.get(ordinal());
    }

    public static InductionCellTier getDefault() {
        return BASIC;
    }

    public static InductionCellTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static InductionCellTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getMaxEnergy() {
        return MekanismConfig.current().general.tiers.get(baseTier).InductionCellMaxEnergy.val();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }
}