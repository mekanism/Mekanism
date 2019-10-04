package mekanism.client.gui.element.tab;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TabType<TILE extends TileEntity> {

    ResourceLocation getResource();

    void onClick(TILE tile);

    ITextComponent getDescription();

    int getYPos();
}