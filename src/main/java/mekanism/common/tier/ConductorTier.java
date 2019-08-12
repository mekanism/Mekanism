package mekanism.common.tier;

import mekanism.common.ColourRGBA;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public enum ConductorTier implements ITier {
    BASIC(5, 1, 10, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ADVANCED(5, 1, 400, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ELITE(5, 1, 8000, new ColourRGBA(0.2, 0.2, 0.2, 1)),
    ULTIMATE(5, 1, 100_000, new ColourRGBA(0.2, 0.2, 0.2, 1));

    private final ColourRGBA baseColour;
    private final double baseConduction;
    private final double baseHeatCapacity;
    private final double baseConductionInsulation;
    private final BaseTier baseTier;
    private DoubleValue conductionReference;
    private DoubleValue capacityReference;
    private DoubleValue insulationReference;

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
        return conductionReference == null ? getBaseConduction() : conductionReference.get();
    }

    public double getInverseConductionInsulation() {
        return insulationReference == null ? getBaseConductionInsulation() : insulationReference.get();
    }

    public double getInverseHeatCapacity() {
        return capacityReference == null ? getBaseHeatCapacity() : capacityReference.get();
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

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
     */
    public void setConfigReference(DoubleValue conductionReference, DoubleValue capacityReference, DoubleValue insulationReference) {
        this.conductionReference = conductionReference;
        this.capacityReference = capacityReference;
        this.insulationReference = insulationReference;
    }
}