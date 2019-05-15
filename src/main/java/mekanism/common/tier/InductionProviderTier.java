package mekanism.common.tier;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.api.util.EnumUtils;

public enum InductionProviderTier implements ITier {
    BASIC(64000),
    ADVANCED(512000),
    ELITE(4096000),
    ULTIMATE(32768000);

    private final double baseOutput;
    private final BaseTier baseTier;

    InductionProviderTier(double out) {
        baseOutput = out;
        baseTier = BaseTier.get(ordinal());
    }

    public static InductionProviderTier getDefault() {
        return BASIC;
    }

    public static InductionProviderTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static InductionProviderTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getOutput() {
        return MekanismConfig.current().general.tiers.get(baseTier).InductionProviderOutput.val();
    }

    public double getBaseOutput() {
        return baseOutput;
    }
}