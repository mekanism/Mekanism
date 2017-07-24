package com.jaquadro.minecraft.storagedrawers.api;

import com.jaquadro.minecraft.storagedrawers.api.config.IUserConfig;
import com.jaquadro.minecraft.storagedrawers.api.registry.IRecipeHandlerRegistry;
import com.jaquadro.minecraft.storagedrawers.api.registry.IRenderRegistry;
import com.jaquadro.minecraft.storagedrawers.api.registry.IWailaRegistry;

public interface IStorageDrawersApi
{
    /**
     * Recipe handlers are used to make custom recipes compatible with compacting drawers.
     */
    IRecipeHandlerRegistry recipeHandlerRegistry ();

    IRenderRegistry renderRegistry ();

    IWailaRegistry wailaRegistry ();

    /**
     * User-managed configuration for the Storage Drawers mod.
     */
    IUserConfig userConfig ();
}
