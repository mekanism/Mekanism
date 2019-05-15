package mekanism.common.tier;

import javax.annotation.Nonnull;
import mekanism.common.config.MekanismConfig;
import mekanism.api.util.EnumUtils;

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
        baseTier = BaseTier.get(ordinal());
    }

    public static TransporterTier getDefault() {
        return BASIC;
    }

    public static TransporterTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static TransporterTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
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