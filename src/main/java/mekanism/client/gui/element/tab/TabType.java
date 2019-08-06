package mekanism.client.gui.element.tab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TabType {

    ResourceLocation getResource();

    void openGui(TileEntity tile);

    String getDesc();

    int getYPos();
}