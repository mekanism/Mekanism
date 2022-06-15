package mekanism.common;

import mekanism.common.base.IChemicalConstant;

/**
 * Contains various information about different chemicals.
 *
 * @implNote This is used os that we can then have them get rounded for purposes of use in things like FluidType.Properties, but so that we have accurate data if at some
 * point they start supporting more accurate data, without us having to look any of the data back up.
 */
public enum ChemicalConstants implements IChemicalConstant {
    HYDROGEN("hydrogen", 0xFFFFFFFF, 0, 20.28F, 70.85F),
    OXYGEN("oxygen", 0xFF6CE2FF, 0, 90.19F, 1_141),
    CHLORINE("chlorine", 0xFFCFE800, 0, 207.15F, 1_422.92F),
    SULFUR_DIOXIDE("sulfur_dioxide", 0xFFA99D90, 0, 263.05F, 1_400),
    SULFUR_TRIOXIDE("sulfur_trioxide", 0xFFCE6C6C, 0, 318, 1_920),
    //Note: We use 300 for the temperature given that is what water is set to by forge for "room temperature"
    // Sulfuric acid is a liquid at room temperature
    SULFURIC_ACID("sulfuric_acid", 0xFF82802B, 0, 300, 1_840),
    HYDROGEN_CHLORIDE("hydrogen_chloride", 0xFFA8F1E9, 0, 188.1F, 821.43F),
    ETHENE("ethene", 0xFFEACCF9, 0, 169.45F, 577),
    //Note: it is a solid at room temperature
    SODIUM("sodium", 0xFFE9FEF4, 0, 370.944F, 927),
    SUPERHEATED_SODIUM("superheated_sodium", 0xFFD19469, 0, 2_000.0F, 927),
    //Note: it is a solid at room temperature
    LITHIUM("lithium", 0xFFEBA400, 0, 453.65F, 512),
    HYDROFLUORIC_ACID("hydrofluoric_acid", 0xFFC6C7BD, 0, 189.6F, 1_150),
    URANIUM_OXIDE("uranium_oxide", 0xFFE1F573, 0, 3138.15F, 10_970),
    URANIUM_HEXAFLUORIDE("uranium_hexafluoride", 0xFF809960, 0, 337.2F, 5_090);

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
    ChemicalConstants(String name, int color, int lightLevel, float temperature, float density) {
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