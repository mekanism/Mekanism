package com.jaquadro.minecraft.storagedrawers.api.registry;

import com.jaquadro.minecraft.storagedrawers.api.render.IRenderLabel;

public interface IRenderRegistry
{
    void registerPreLabelRenderHandler (IRenderLabel renderHandler);
}
