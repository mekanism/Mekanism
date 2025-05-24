package mekanism.api.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.NotNull;

/**
 * Interface used for defining radiation sources.
 */
public interface IRadiationSource {

    /**
     * Gets the location of this {@link IRadiationSource}.
     *
     * @since 10.7.15
     */
    @NotNull
    BlockPos getPosition();

    /**
     * Gets the location of this {@link IRadiationSource}.
     * <p>
     * Only available when retrieved via {@link IRadiationManager#getRadiationSources()}
     *
     * @throws UnsupportedOperationException when retrieved via non-deprecated methods
     * @deprecated Replace with {@link #getPosition()}
     */
    @Deprecated(forRemoval = true, since = "10.7.15")
    GlobalPos getPos();

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