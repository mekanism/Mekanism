package mekanism.client.gui.element.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.filter.GuiItemStackFilter;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerItemStackFilter extends GuiItemStackFilter<MinerItemStackFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {

    public static GuiMinerItemStackFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiMinerItemStackFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerItemStackFilter filter) {
        return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiMinerItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, MinerItemStackFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addMinerDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected MinerItemStackFilter createNewFilter() {
        return new MinerItemStackFilter();
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        renderMinerForeground(guiObj, filter);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return tryClickReplaceStack(guiObj, mouseX, mouseY, button, filter) || super.mouseClicked(mouseX, mouseY, button);
    }
}