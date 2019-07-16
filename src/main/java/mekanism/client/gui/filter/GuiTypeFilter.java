package mekanism.client.gui.filter;

import mekanism.client.render.MekanismRenderer;
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

    protected boolean overTypeInput(int xAxis, int yAxis) {
        return xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tileEntity instanceof TileEntityDigitalMiner && overReplaceOutput(xAxis, yAxis)) {
            drawRect(guiLeft + 149, guiTop + 19, guiLeft + 165, guiTop + 35, 0x80FFFFFF);
        } else if (tileEntity instanceof TileEntityLogisticalSorter) {
            drawItemStackBackground(xAxis, yAxis);
        }
        if (overTypeInput(xAxis, yAxis)) {
            drawRect(guiLeft + 12, guiTop + 19, guiLeft + 28, guiTop + 35, 0x80FFFFFF);
        }
        MekanismRenderer.resetColor();
    }

    protected abstract void drawForegroundLayer(int mouseX, int mouseY);
}