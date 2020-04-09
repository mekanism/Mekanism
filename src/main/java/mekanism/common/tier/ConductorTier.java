package mekanism.common.tier;

import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.ColorRGBA;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.util.EnumUtils;

public enum ConductorTier implements ITier {
    BASIC(BaseTier.BASIC, FloatingLong.createConst(5), HeatAPI.DEFAULT_HEAT_CAPACITY, FloatingLong.createConst(10), new ColorRGBA(0.2, 0.2, 0.2, 1)),
    ADVANCED(BaseTier.ADVANCED, FloatingLong.createConst(5), HeatAPI.DEFAULT_HEAT_CAPACITY, FloatingLong.createConst(400), new ColorRGBA(0.2, 0.2, 0.2, 1)),
    ELITE(BaseTier.ELITE, FloatingLong.createConst(5), HeatAPI.DEFAULT_HEAT_CAPACITY, FloatingLong.createConst(8_000), new ColorRGBA(0.2, 0.2, 0.2, 1)),
    ULTIMATE(BaseTier.ULTIMATE, FloatingLong.createConst(5), HeatAPI.DEFAULT_HEAT_CAPACITY, FloatingLong.createConst(100_000), new ColorRGBA(0.2, 0.2, 0.2, 1));

    private final ColorRGBA baseColor;
    private final FloatingLong baseConduction;
    private final FloatingLong baseHeatCapacity;
    private final FloatingLong baseConductionInsulation;
    private final BaseTier baseTier;
    private CachedFloatingLongValue conductionReference;
    private CachedFloatingLongValue capacityReference;
    private CachedFloatingLongValue insulationReference;

    ConductorTier(BaseTier tier, FloatingLong conduction, FloatingLong heatCapacity, FloatingLong conductionInsulation, ColorRGBA color) {
        baseConduction = conduction;
        baseHeatCapacity = heatCapacity;
        baseConductionInsulation = conductionInsulation;

        baseColor = color;
        baseTier = tier;
    }

    public static ConductorTier get(BaseTier tier) {
        for (ConductorTier transmitter : EnumUtils.CONDUCTOR_TIERS) {
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

    public FloatingLong getInverseConduction() {
        return conductionReference == null ? getBaseConduction() : conductionReference.get();
    }

    public FloatingLong getInverseConductionInsulation() {
        return insulationReference == null ? getBaseConductionInsulation() : insulationReference.get();
    }

    public FloatingLong getHeatCapacity() {
        return capacityReference == null ? getBaseHeatCapacity() : capacityReference.get();
    }

    public ColorRGBA getBaseColor() {
        return baseColor;
    }

    public FloatingLong getBaseConduction() {
        return baseConduction;
    }

    public FloatingLong getBaseHeatCapacity() {
        return baseHeatCapacity;
    }

    public FloatingLong getBaseConductionInsulation() {
        return baseConductionInsulation;
    }

    /**
     * ONLY CALL THIS FROM TierConfig. It is used to give the BinTier a reference to the actual config value object
     */
    public void setConfigReference(CachedFloatingLongValue conductionReference, CachedFloatingLongValue capacityReference, CachedFloatingLongValue insulationReference) {
        this.conductionReference = conductionReference;
        this.capacityReference = capacityReference;
        this.insulationReference = insulationReference;
    }
}