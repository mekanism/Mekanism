package com.jaquadro.minecraft.storagedrawers.api.registry;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawer;

public interface IWailaTooltipHandler
{
    String transformItemName (IDrawer drawer, String defaultName);
}
