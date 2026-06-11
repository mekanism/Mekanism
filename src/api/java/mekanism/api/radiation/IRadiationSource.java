package mekanism.api.radiation;

import net.minecraft.core.BlockPos;

/// Interface used for defining radiation sources.
public interface IRadiationSource {

    /// Gets the location of this [IRadiationSource].
    ///
    /// @since 10.7.15
    BlockPos getPosition();

    /// Get the radiation level (in Sv/h) of this [IRadiationSource].
    ///
    /// @return radiation dosage
    double getMagnitude();

    /// Applies a radiation source (Sv) of the given magnitude to this [IRadiationSource].
    ///
    /// @param magnitude Amount of radiation to apply (Sv).
    void radiate(double magnitude);

    /// Decays the source's radiation level.
    boolean decay();
}