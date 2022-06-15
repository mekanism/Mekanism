package mekanism.generators.common;

import mekanism.common.base.IChemicalConstant;

/**
 * Contains various information about different chemicals.
 *
 * @implNote This is used os that we can then have them get rounded for purposes of use in things like FluidType.Properties, but so that we have accurate data if at some
 * point they start supporting more accurate data, without us having to look any of the data back up.
 */
public enum GeneratorsChemicalConstants implements IChemicalConstant {
    DEUTERIUM("deuterium", 0xFFFF3232, 0, 23.7F, 162.4F);

    private final String name;
    private final int color;
    private final int lightLevel;
    private final float temperature;
    private final float density;

    /**
     * @param name        The name of the chemical
     * @param color       Visual color in ARGB format
     * @param lightLevel  Light level
     * @param temperature Temperature in Kelvin that the chemical exists as a liquid
     * @param density     Density as a liquid in kg/m^3
     */
    GeneratorsChemicalConstants(String name, int color, int lightLevel, float temperature, float density) {
        this.name = name;
        this.color = color;
        this.lightLevel = lightLevel;
        this.temperature = temperature;
        this.density = density;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public float getTemperature() {
        return temperature;
    }

    @Override
    public float getDensity() {
        return density;
    }

    @Override
    public int getLightLevel() {
        return lightLevel;
    }
}