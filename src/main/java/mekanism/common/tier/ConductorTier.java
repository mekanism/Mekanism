package mekanism.common.tier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.EnumUtils;

public enum ConductorTier implements ITier<ConductorTier> {
    BASIC(5, 1, 10, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ADVANCED(5, 1, 400, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ELITE(5, 1, 8000, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ULTIMATE(5, 1, 100000, new ColourRGBA(0.2, 0.2, 0.2, 1));

    private final ColourRGBA baseColour;
    private final double baseConduction;
    private final double baseHeatCapacity;
    private final double baseConductionInsulation;
    private final BaseTier baseTier;

    ConductorTier(double inversek, double inverseC, double insulationInversek, ColourRGBA colour) {
        baseConduction = inversek;
        baseHeatCapacity = inverseC;
        baseConductionInsulation = insulationInversek;

        baseColour = colour;
        baseTier = BaseTier.get(ordinal());
    }

    public static ConductorTier getDefault() {
        return BASIC;
    }

    public static ConductorTier get(int ordinal) {
        return EnumUtils.getEnumSafe(values(), ordinal, getDefault());
    }

    public static ConductorTier get(@Nonnull BaseTier tier) {
        return get(tier.ordinal());
    }

    @Override
    public boolean hasNext() {
        return ordinal() + 1 < values().length;
    }

    @Nullable
    @Override
    public ConductorTier next() {
        return hasNext() ? get(ordinal() + 1) : null;
    }

    @Override
    public BaseTier getBaseTier() {
        return baseTier;
    }

    public double getInverseConduction() {
        return MekanismConfig.current().general.tiers.get(baseTier).ConductorInverseConduction.val();
    }

    public double getInverseConductionInsulation() {
        return MekanismConfig.current().general.tiers.get(baseTier).ConductorConductionInsulation.val();
    }

    public double getInverseHeatCapacity() {
        return MekanismConfig.current().general.tiers.get(baseTier).ConductorHeatCapacity.val();
    }

    public ColourRGBA getBaseColour() {
        return baseColour;
    }

    public double getBaseConduction() {
        return baseConduction;
    }

    public double getBaseHeatCapacity() {
        return baseHeatCapacity;
    }

    public double getBaseConductionInsulation() {
        return baseConductionInsulation;
    }
}