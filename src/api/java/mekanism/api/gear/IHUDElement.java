package mekanism.api.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/// Interface representing the needed information for rendering elements on the MekaSuit HUD. It is recommended to use one of the following helper methods to build this,
/// though it is possible to implement HUD Elements manually.
/// - [IModuleHelper#hudElement(Identifier, Component, HUDColor)]
/// - [IModuleHelper#hudElementEnabled(Identifier, boolean)]
/// - [IModuleHelper#hudElementPercent(Identifier, double)]
public interface IHUDElement {

    /// Gets the path to the texture/icon to render for this [IHUDElement].
    ///
    /// @return Icon.
    Identifier getIcon();

    /// Gets the text to render for this [IHUDElement].
    ///
    /// @return Text to render.
    Component getText();

    /// Gets the color to use for this [IHUDElement].
    ///
    /// @return ARGB color.
    int getColor();

    /// Enum representing the built-in configurable HUD-Colors Mekanism uses.
    enum HUDColor {
        REGULAR,
        FADED,
        WARNING,
        DANGER;
    }
}