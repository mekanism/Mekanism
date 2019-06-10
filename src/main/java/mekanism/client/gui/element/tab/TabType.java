package mekanism.client.gui.element.tab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface TabType {

    ResourceLocation getResource();

    void openGui(TileEntity tile);

    String getDesc();
}