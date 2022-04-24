package mekanism.api.gear;

import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Interface representing the needed information for rendering elements on the MekaSuit HUD. It is recommended to use one of the following helper methods to build this,
 * though it is possible to implement HUD Elements manually.
 * <ul>
 *     <li>{@link IModuleHelper#hudElement(ResourceLocation, Component, HUDColor)}</li>
 *     <li>{@link IModuleHelper#hudElementEnabled(ResourceLocation, boolean)}</li>
 *     <li>{@link IModuleHelper#hudElementPercent(ResourceLocation, double)}</li>
 * </ul>
 */
public interface IHUDElement {

    /**
     * Gets the path to the texture/icon to render for this {@link IHUDElement}.
     *
     * @return Icon.
     */
    @Nonnull
    ResourceLocation getIcon();

    /**
     * Gets the text to render for this {@link IHUDElement}.
     *
     * @return Text to render.
     */
    @Nonnull
    Component getText();

    /**
     * Gets the color to use for this {@link IHUDElement}.
     *
     * @return ARGB color.
     */
    int getColor();

    /**
     * Enum representing the built-in configurable HUD-Colors Mekanism uses.
     */
    enum HUDColor {
        REGULAR,
        FADED,
        WARNING,
        DANGER;
    }
}