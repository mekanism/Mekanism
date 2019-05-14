package mekanism.common.tier;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.IStringSerializable;

public enum EnergyCubeTier implements ITier<EnergyCubeTier>, IStringSerializable {
    BASIC(2000000, 800),
    ADVANCED(8000000, 3200),
    ELITE(32000000, 12800),
    ULTIMATE(128000000, 51200),
    CREATIVE(Double.MAX_VALUE, Double.MAX_VALUE);

    private final double baseMaxEnergy;
    private final double baseOutput;
    private final BaseTier baseTier;

    EnergyCubeTier(double max, double out) {
        baseMaxEnergy = max;
        baseOutput = out;
        baseTier = BaseTier.get(ordinal());
    }

    public static EnergyCubeTier getDefault() {
        return BASIC;
    }

    public static EnergyCubeTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static EnergyCubeTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return ordinal() + 1 < values().length;
    }

    @Nullable
    @Override
    public EnergyCubeTier next() {
        return hasNext() ? get(ordinal() + 1) : null;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    @Override
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public double getMaxEnergy() {
        return MekanismConfig.current().general.tiers.get(baseTier).EnergyCubeMaxEnergy.val();
    }

    public double getOutput() {
        return MekanismConfig.current().general.tiers.get(baseTier).EnergyCubeOutput.val();
    }

    public double getBaseMaxEnergy() {
        return baseMaxEnergy;
    }

    public double getBaseOutput() {
        return baseOutput;
    }
}