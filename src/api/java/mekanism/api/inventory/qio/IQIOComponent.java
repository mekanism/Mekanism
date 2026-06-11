package mekanism.api.inventory.qio;

import org.jspecify.annotations.Nullable;

/// Implemented by Mekanism's QIO Component block entities.
///
/// @since 10.2.1
public interface IQIOComponent {

    /// Gets the current [`QIO Frequency`][IQIOFrequency] that this component is set to.
    ///
    /// @return Current frequency or `null` if this component doesn't have a frequency selected.
    @Nullable
    IQIOFrequency getQIOFrequency();
}