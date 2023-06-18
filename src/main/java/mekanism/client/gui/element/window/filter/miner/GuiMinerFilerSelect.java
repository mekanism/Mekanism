package mekanism.client.gui.element.window.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import org.jetbrains.annotations.NotNull;

public class GuiMinerFilerSelect extends GuiFilterSelect<TileEntityDigitalMiner> {

    public GuiMinerFilerSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        super(gui, tile, 3);
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getItemStackFilterCreator() {
        return GuiMinerItemStackFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getTagFilterCreator() {
        return GuiMinerTagFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getModIDFilterCreator() {
        return GuiMinerModIDFilter::create;
    }
}