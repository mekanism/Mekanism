package mekanism.client.gui.element;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public interface TabType {

    ResourceLocation getResource();

    void openGui(TileEntity tile);

    String getDesc();
}