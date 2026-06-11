package mekanism.api.radial.mode;

import mekanism.api.radial.RadialData;
import org.jspecify.annotations.Nullable;

/// Interface representing a mode for usage in a radial menu. This mode may or may not represent a group of modes nested inside this mode.
///
/// @since 10.3.2
public interface INestedRadialMode extends IRadialMode {

    /// @return Nested Radial Data or `null` if this radial mode doesn't actually support nested data.
    ///
    /// @apiNote [#hasNestedData()] can be used to determine whether there is nested data present or not.
    @Nullable
    RadialData<?> nestedData();

    /// Used to check whether this radial mode has any nested data. If it does then [#nestedData()] will not be null.
    ///
    /// @return `true` if this radial mode has nested data.
    default boolean hasNestedData() {
        return nestedData() != null;
    }
}