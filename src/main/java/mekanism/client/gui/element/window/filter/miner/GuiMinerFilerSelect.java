package mekanism.client.gui.element.window.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerFilerSelect extends GuiFilterSelect<TileEntityDigitalMiner> {

    public GuiMinerFilerSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        super(gui, tile, 3);
    }

    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getItemStackFilterCreator() {
        return GuiMinerItemStackFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getTagFilterCreator() {
        return GuiMinerTagFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getModIDFilterCreator() {
        return GuiMinerModIDFilter::create;
    }
}