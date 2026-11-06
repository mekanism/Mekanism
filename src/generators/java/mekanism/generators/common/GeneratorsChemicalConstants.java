package mekanism.generators.common;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.base.IChemicalConstant;
import net.minecraft.resources.ResourceKey;

/// Contains various information about different chemicals.
///
/// @implNote This is used os that we can then have them get rounded for purposes of use in things like FluidType.Properties, but so that we have accurate data if at some
/// point they start supporting more accurate data, without us having to look any of the data back up.
public enum GeneratorsChemicalConstants implements IChemicalConstant {
    DEUTERIUM(ChemicalIds.DEUTERIUM, 0xFFFF3232, 0, 23.7F, 162.4F, 1_250);

    private final ResourceKey<Chemical> key;
    private final int color;
    private final int lightLevel;
    private final float temperature;
    private final int viscosity;
    private final float density;

    /// @param key         The name of the chemical
    /// @param color       Visual color in ARGB format
    /// @param lightLevel  Light level
    /// @param temperature Temperature in Kelvin that the chemical exists as a liquid
    /// @param density     Density as a liquid in kg/m^3
    /// @param viscosity   Viscosity as a liquid in m/s^2
    GeneratorsChemicalConstants(ResourceKey<Chemical> key, int color, int lightLevel, float temperature, float density, int viscosity) {
        this.key = key;
        this.color = color;
        this.lightLevel = lightLevel;
        this.temperature = temperature;
        this.density = density;
        this.viscosity = viscosity;
    }

    @Override
    public ResourceKey<Chemical> key() {
        return key;
    }

    @Override
    public int color() {
        return color;
    }

    @Override
    public float temperature() {
        return temperature;
    }

    @Override
    public float density() {
        return density;
    }

    @Override
    public int viscosity() {
        return viscosity;
    }

    @Override
    public int lightLevel() {
        return lightLevel;
    }
}