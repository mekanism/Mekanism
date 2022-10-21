package mekanism.api.radial.mode;

import java.util.Objects;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Record providing a basic implementation for simple radial modes.
 *
 * @since 10.3.2
 */
public record BasicRadialMode(@NotNull Component sliceName, @NotNull ResourceLocation icon, @Nullable EnumColor color) implements IRadialMode {

    /**
     * @param sliceName Name to display in this mode's slice of the radial menu.
     * @param icon      Asset location of the icon to draw in this mode's slice of the radial menu.
     */
    public BasicRadialMode(@NotNull Component sliceName, @NotNull ResourceLocation icon) {
        this(sliceName, icon, null);
    }

    /**
     * @param sliceName Lang entry for the name to display in this mode's slice of the radial menu.
     * @param icon      Asset location of the icon to draw in this mode's slice of the radial menu.
     * @param color     Selection color of this mode's slice.
     *
     * @implNote {@code sliceName} is colored using the given color.
     */
    public BasicRadialMode(@NotNull ILangEntry sliceName, @NotNull ResourceLocation icon, @NotNull EnumColor color) {
        this(sliceName.translateColored(color), icon, color);
    }

    public BasicRadialMode {
        Objects.requireNonNull(sliceName, "Radial modes must have a slice name.");
        Objects.requireNonNull(icon, "Radial modes must have an icon to display.");
    }
}