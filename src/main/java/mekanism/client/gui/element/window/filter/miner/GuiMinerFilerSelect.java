package mekanism.client.gui.element.window.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerFilerSelect extends GuiFilterSelect {

    private final TileEntityDigitalMiner tile;

    public GuiMinerFilerSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        super(gui);
        this.tile = tile;
    }

    @Override
    protected GuiMinerItemStackFilter createNewItemStackFilter() {
        return GuiMinerItemStackFilter.create(gui(), tile);
    }

    @Override
    protected GuiMinerTagFilter createNewTagFilter() {
        return GuiMinerTagFilter.create(gui(), tile);
    }

    @Override
    protected GuiMinerMaterialFilter createNewMaterialFilter() {
        return GuiMinerMaterialFilter.create(gui(), tile);
    }

    @Override
    protected GuiMinerModIDFilter createNewModIDFilter() {
        return GuiMinerModIDFilter.create(gui(), tile);
    }
}