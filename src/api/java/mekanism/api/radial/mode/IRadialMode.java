package mekanism.api.radial.mode;

import mekanism.api.text.EnumColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Interface representing a mode for usage in a radial menu.
///
/// @since 10.3.2
public interface IRadialMode {

    /// @return Name to display in this mode's slice of the radial menu.
    Component sliceName();

    /// @return Asset location of the icon to draw in this mode's slice of the radial menu.
    Identifier icon();

    /// Gets the color to that is used for rendering this mode's slice of the radial menu when this mode is currently selected.
    ///
    /// @return Selection color of this mode's slice.
    @Nullable
    default EnumColor color() {
        return null;
    }
}