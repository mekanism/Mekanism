package mekanism.api;

import java.util.UUID;
import mekanism.api.security.SecurityMode;
import org.jetbrains.annotations.Nullable;

/**
 * Defines and exposes various methods relating to frequency implementations ot the API.
 *
 * @since 10.2.1
 */
public interface IFrequency {

    /**
     * Gets the security mode of this frequency. This determines who is able to see and select the frequency from the frequency selection screens.
     *
     * @return The frequency's security mode.
     */
    SecurityMode getSecurity();

    /**
     * Checks if this frequency is valid. In 99% of cases where this can be interacted with this will be the case except when this frequency was just loaded from disk.
     *
     * @return {@code true} unless the frequency is still loading.
     */
    boolean isValid();

    /**
     * Gets the name of this frequency.
     *
     * @return Frequency name.
     */
    String getName();

    /**
     * Gets the UUID of the owner of this frequency.
     *
     * @return Frequency owner, or {@code null} if the frequency was loaded with incomplete data.
     */
    @Nullable
    UUID getOwner();
}