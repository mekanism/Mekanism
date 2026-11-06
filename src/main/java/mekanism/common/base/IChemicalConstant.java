package mekanism.common.base;

import mekanism.api.chemical.Chemical;
import net.minecraft.resources.ResourceKey;

public interface IChemicalConstant {

    /// @return The name of the chemical
    ResourceKey<Chemical> key();

    /// @return Visual color in ARGB format
    int color();

    /// @return Temperature in Kelvin that the chemical exists as a liquid
    float temperature();

    /// @return Density as a liquid in kg/m^3
    float density();

    /// @return Viscosity as a liquid in m/s^2
    int viscosity();

    /// @return Brightness
    int lightLevel();
}