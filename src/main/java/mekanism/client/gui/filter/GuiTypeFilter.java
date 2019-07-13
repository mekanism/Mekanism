package mekanism.client.gui.filter;

import mekanism.common.content.filter.IFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiTypeFilter<FILTER extends IFilter, TILE extends TileEntityContainerBlock> extends GuiFilterBase<FILTER, TILE> {

    protected GuiTypeFilter(EntityPlayer player, TILE tile) {
        super(player, tile);
    }

    protected void drawItemStackBackground(int xAxis, int yAxis) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        drawTexturedModalRect(guiLeft + 5, guiTop + 5, 176, 0, xAxis >= 5 && xAxis <= 16 && yAxis >= 5 && yAxis <= 16, 11);
        if (tileEntity instanceof TileEntityDigitalMiner) {
            drawTexturedModalRect(guiLeft + 148, guiTop + 45, 199, 0, xAxis >= 148 && xAxis <= 162 && yAxis >= 45 && yAxis <= 59, 14);
            drawItemStackBackground(xAxis, yAxis);
            drawPositionedRect(xAxis, yAxis, 149, 165, 19, 35);
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawTexturedModalRect(guiLeft + 11, guiTop + 64, 198, 0, xAxis >= 11 && xAxis <= 22 && yAxis >= 64 && yAxis <= 75, 11);
            drawItemStackBackground(xAxis, yAxis);
        }
        drawPositionedRect(xAxis, yAxis, 12, 28, 19, 35);
    }

    protected abstract void drawForegroundLayer(int mouseX, int mouseY);
}