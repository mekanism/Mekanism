package mekanism.common.tier;

import mekanism.common.ColourRGBA;
import mekanism.common.config.MekanismConfig;

public enum ConductorTier implements ITier {
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
        baseTier = BaseTier.values()[ordinal()];
    }

    public static ConductorTier get(BaseTier tier) {
        for (ConductorTier transmitter : values()) {
            if (transmitter.getBaseTier() == tier) {
                return transmitter;
            }
        }
        return BASIC;
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