package mekanism.common.base;

public interface IChemicalConstant {

    /**
     * @return The name of the chemical
     */
    String getName();

    /**
     * @return Visual color in ARGB format
     */
    int getColor();

    /**
     * @return Temperature in Kelvin that the chemical exists as a liquid
     */
    float getTemperature();

    /**
     * @return Density as a liquid in kg/m^3
     */
    float getDensity();

    /**
     * @return Brightness
     */
    int getLightLevel();
}