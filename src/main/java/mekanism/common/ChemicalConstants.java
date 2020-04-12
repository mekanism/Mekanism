package mekanism.common;

/**
 * Contains various information about different chemicals.
 *
 * @implNote This is used os that we can then have them get rounded for purposes of use in things like FluidAttributes, but so that we have accurate data if at some point
 * they start supporting more accurate data, without us having to look any of the data back up.
 */
public enum ChemicalConstants {
    HYDROGEN("hydrogen", 0xFFFFFFFF, 20.28F, 70.85F),
    OXYGEN("oxygen", 0xFF6CE2FF, 90.19F, 1141),
    CHLORINE("chlorine", 0xFFCFE800, 207.15F, 1422.92F),
    SULFUR_DIOXIDE("sulfur_dioxide", 0xFFA99D90, 263.05F, 1400),
    SULFUR_TRIOXIDE("sulfur_trioxide", 0xFFCE6C6C, 318, 1920),
    //Note: We use 300 for the temperature given that is what water is set to by forge for "room temperature"
    // Sulfuric acid is a liquid at room temperature
    SULFURIC_ACID("sulfuric_acid", 0xFF82802B, 300, 1840),
    HYDROGEN_CHLORIDE("hydrogen_chloride", 0xFFA8F1E9, 188.1F, 821.43F),
    ETHENE("ethene", 0xFFEACCF9, 169.45F, 577),
    //Note: it is a solid at room temperature
    SODIUM("sodium", 0xFFE9FEF4, 370.944F, 927),
    //TODO: Move this reference into MekanismGenerators at some point
    DEUTERIUM("deuterium", 0xFFFF3232, 23.7F, 162.4F),
    //Note: it is a solid at room temperature
    LITHIUM("lithium", 0xFFEBA400, 453.65F, 512),
    HYDROFLUORIC_ACID("hydrofluoric_acid", 0xFFC6C7BD, 189.6F, 1150F),
    URANIUM_OXIDE("uranium_oxide", 0xFFE1F573, 3138.15F, 10970F),
    URANIUM_HEXAFLUORIDE("uranium_hexafluoride", 0xFF809960, 337.2F, 5090F);

    private final String name;
    private final int color;
    private final float temperature;
    private final float density;

    /**
     * @param name        The name of the chemical
     * @param color       Visual color in ARGB format
     * @param temperature Temperature in Kelvin that the chemical exists as a liquid
     * @param density     Density as a liquid in kg/m^3
     */
    ChemicalConstants(String name, int color, float temperature, float density) {
        this.name = name;
        this.color = color;
        this.temperature = temperature;
        this.density = density;
    }

    /**
     * @return The name of the chemical
     */
    public String getName() {
        return name;
    }

    /**
     * @return Visual color in ARGB format
     */
    public int getColor() {
        return color;
    }

    /**
     * @return Temperature in Kelvin that the chemical exists as a liquid
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * @return Density as a liquid in kg/m^3
     */
    public float getDensity() {
        return density;
    }
}