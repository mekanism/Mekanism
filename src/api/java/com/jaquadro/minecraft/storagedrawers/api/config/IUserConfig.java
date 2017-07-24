package com.jaquadro.minecraft.storagedrawers.api.config;

/**
 * The main hub for user-managed mod configuration.
 */
public interface IUserConfig
{
    /**
     * Configuration options related to third party addon packs for Storage Drawers.
     */
    @Deprecated
    IAddonConfig addonConfig ();

    /**
     * Configuration options related to individual blocks.
     */
    IBlockConfig blockConfig ();
}
