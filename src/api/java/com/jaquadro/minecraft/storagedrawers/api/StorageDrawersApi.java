package com.jaquadro.minecraft.storagedrawers.api;

/**
 * Entry point for the public API.
 */
public class StorageDrawersApi
{
    private static IStorageDrawersApi instance;

    public static final String VERSION = "1.10.2-1.3.0";

    /**
     * API entry point.
     *
     * @return The {@link IStorageDrawersApi} instance or null if the API or Storage Drawers is unavailable.
     */
    public static IStorageDrawersApi instance () {
        if (instance == null) {
            try {
                Class classApi = Class.forName( "com.jaquadro.minecraft.storagedrawers.core.Api" );
                instance = (IStorageDrawersApi) classApi.getField("instance").get(null);
            }
            catch (Throwable t) {
                return null;
            }
        }

        return instance;
    }
}
