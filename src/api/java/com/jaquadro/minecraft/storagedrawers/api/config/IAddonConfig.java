package com.jaquadro.minecraft.storagedrawers.api.config;

public interface IAddonConfig
{
    /**
     * Gets whether the user has configured a preference for addon packs to hide their blocks and items from Vanilla
     * creative tabs.
     */
    boolean showAddonItemsNEI ();

    /**
     * Gets whether the user has configured a preference for addon packs to hide their blocks and items from NEI.
     */
    boolean showAddonItemsVanilla ();

    /**
     * Gets whether the user has configured a preference for addon packs to provide their blocks and items through
     * their own vanilla tab.
     */
    boolean addonItemsUseSeparateTab ();
}
