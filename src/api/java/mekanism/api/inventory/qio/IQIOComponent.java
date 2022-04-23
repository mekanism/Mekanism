package mekanism.api.inventory.qio;

import javax.annotation.Nullable;

/**
 * Implemented by Mekanism's QIO Component block entities.
 *
 * @since 10.2.1
 */
public interface IQIOComponent {

    /**
     * Gets the current {@link IQIOFrequency QIO Frequency} that this component is set to.
     *
     * @return Current frequency or {@code null} if this component doesn't have a frequency selected.
     */
    @Nullable
    IQIOFrequency getQIOFrequency();
}