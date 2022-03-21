package mekanism.chemistry.common;

import mekanism.common.base.IChemicalConstant;

public enum ChemistryChemicalConstants implements IChemicalConstant {
    ;

    private final String name;
    private final int color;
    private final int luminosity;
    private final float temperature;
    private final float density;

    /**
     * @param name        The name of the chemical
     * @param color       Visual color in ARGB format
     * @param luminosity  Luminosity
     * @param temperature Temperature in Kelvin that the chemical exists as a liquid
     * @param density     Density as a liquid in kg/m^3
     */
    ChemistryChemicalConstants(String name, int color, int luminosity, float temperature, float density) {
        this.name = name;
        this.color = color;
        this.luminosity = luminosity;
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
    public int getLuminosity() {
        return luminosity;
    }
}
