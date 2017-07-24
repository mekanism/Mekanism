package com.jaquadro.minecraft.storagedrawers.api.render;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerGroup;
import net.minecraft.tileentity.TileEntity;

public interface IRenderLabel
{
    void render (TileEntity tileEntity, IDrawerGroup drawerGroup, int slot, float brightness, float partialTickTime);
}
