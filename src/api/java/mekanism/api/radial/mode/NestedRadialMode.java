package mekanism.api.radial.mode;

import java.util.Objects;
import mekanism.api.radial.RadialData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// Record providing a basic implementation for nested radial types.
///
/// @since 10.3.2
public record NestedRadialMode(RadialData<?> nestedData, Component sliceName, Identifier icon, @Nullable EnumColor color)
      implements INestedRadialMode {

    /// @param nestedData Nested Radial Data.
    /// @param sliceName  Name to display in this mode's slice of the radial menu.
    /// @param icon       Asset location of the icon to draw in this mode's slice of the radial menu.
    public NestedRadialMode(RadialData<?> nestedData, Component sliceName, Identifier icon) {
        this(nestedData, sliceName, icon, null);
    }

    /// @param nestedData Nested Radial Data.
    /// @param sliceName  Lang entry for the name to display in this mode's slice of the radial menu.
    /// @param icon       Asset location of the icon to draw in this mode's slice of the radial menu.
    /// @param color      Selection color of this mode's slice.
    ///
    /// @implNote `sliceName` is colored using the given color.
    public NestedRadialMode(RadialData<?> nestedData, ILangEntry sliceName, Identifier icon, EnumColor color) {
        this(nestedData, sliceName.translateColored(color), icon, color);
    }

    public NestedRadialMode {
        Objects.requireNonNull(nestedData, "Nested data is required and cannot be null.");
        Objects.requireNonNull(sliceName, "Radial modes must have a slice name.");
        Objects.requireNonNull(icon, "Radial modes must have an icon to display.");
    }

    @Override
    public boolean hasNestedData() {
        return true;
    }
}