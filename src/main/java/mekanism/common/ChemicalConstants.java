package mekanism.common;

import mekanism.common.base.IChemicalConstant;

/**
 * Contains various information about different chemicals.
 *
 * @implNote This is used os that we can then have them get rounded for purposes of use in things like FluidAttributes, but so that we have accurate data if at some point
 * they start supporting more accurate data, without us having to look any of the data back up.
 */
public enum ChemicalConstants implements IChemicalConstant {
    HYDROGEN("hydrogen", 0xFFFFFFFF, 0, 20.28F, 70.85F),
    OXYGEN("oxygen", 0xFF6CE2FF, 0, 90.19F, 1141),
    CHLORINE("chlorine", 0xFFCFE800, 0, 207.15F, 1422.92F),
    SULFUR_DIOXIDE("sulfur_dioxide", 0xFFA99D90, 0, 263.05F, 1400),
    SULFUR_TRIOXIDE("sulfur_trioxide", 0xFFCE6C6C, 0, 318, 1920),
    //Note: We use 300 for the temperature given that is what water is set to by forge for "room temperature"
    // Sulfuric acid is a liquid at room temperature
    SULFURIC_ACID("sulfuric_acid", 0xFF82802B, 0, 300, 1840),
    HYDROGEN_CHLORIDE("hydrogen_chloride", 0xFFA8F1E9, 0, 188.1F, 821.43F),
    ETHENE("ethene", 0xFFEACCF9, 0, 169.45F, 577),
    //Note: it is a solid at room temperature
    SODIUM("sodium", 0xFFE9FEF4, 0, 370.944F, 927),
    //Note: it is a solid at room temperature
    LITHIUM("lithium", 0xFFEBA400, 0, 453.65F, 512);

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
    ChemicalConstants(String name, int color, int luminosity, float temperature, float density) {
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