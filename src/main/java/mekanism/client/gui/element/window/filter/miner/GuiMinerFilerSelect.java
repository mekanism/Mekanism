package mekanism.client.gui.element.window.filter.miner;

import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.machine.TileEntityDigitalMiner;

public class GuiMinerFilerSelect extends GuiFilterSelect<TileEntityDigitalMiner> {

    public GuiMinerFilerSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
        super(gui, tile, 4);
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getItemStackFilterCreator() {
        return GuiMinerItemStackFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getTagFilterCreator() {
        return GuiMinerTagFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getMaterialFilterCreator() {
        return GuiMinerMaterialFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityDigitalMiner> getModIDFilterCreator() {
        return GuiMinerModIDFilter::create;
    }
}