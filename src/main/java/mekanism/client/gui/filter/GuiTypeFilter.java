package mekanism.client.gui.filter;

import mekanism.client.render.MekanismRenderer;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTypeFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiFilterBase<FILTER, TILE, CONTAINER> {

    protected GuiTypeFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected boolean overTypeInput(double xAxis, double yAxis) {
        return xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        if (tile instanceof TileEntityDigitalMiner && overReplaceOutput(xAxis, yAxis)) {
            fill(getGuiLeft() + 149, getGuiTop() + 19, getGuiLeft() + 165, getGuiTop() + 35, 0x80FFFFFF);
        }
        if (overTypeInput(xAxis, yAxis)) {
            fill(getGuiLeft() + 12, getGuiTop() + 19, getGuiLeft() + 28, getGuiTop() + 35, 0x80FFFFFF);
        }
        MekanismRenderer.resetColor();
    }

    protected abstract void drawForegroundLayer(int mouseX, int mouseY);
}