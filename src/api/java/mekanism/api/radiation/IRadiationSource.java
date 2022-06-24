package mekanism.api.radiation;

import mekanism.api.Coord4D;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used for defining radiation sources.
 */
public interface IRadiationSource {

    /**
     * Gets the location of this {@link IRadiationSource}.
     */
    @NotNull
    Coord4D getPos();

    /**
     * Get the radiation level (in Sv/h) of this {@link IRadiationSource}.
     *
     * @return radiation dosage
     */
    double getMagnitude();

    /**
     * Applies a radiation source (Sv) of the given magnitude to this {@link IRadiationSource}.
     *
     * @param magnitude Amount of radiation to apply (Sv).
     */
    void radiate(double magnitude);

    /**
     * Decays the source's radiation level.
     */
    boolean decay();
}