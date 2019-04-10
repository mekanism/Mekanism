package mekanism.common.tier;

import java.util.Locale;
import mekanism.common.config.MekanismConfig;
import net.minecraft.util.IStringSerializable;

public enum EnergyCubeTier implements ITier, IStringSerializable {
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
        baseTier = BaseTier.values()[ordinal()];
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