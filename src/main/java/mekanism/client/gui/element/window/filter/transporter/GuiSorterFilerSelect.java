package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterFilerSelect extends GuiFilterSelect<TileEntityLogisticalSorter> {

    public GuiSorterFilerSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        super(gui, tile, 3);
    }

    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getItemStackFilterCreator() {
        return GuiSorterItemStackFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getTagFilterCreator() {
        return GuiSorterTagFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getModIDFilterCreator() {
        return GuiSorterModIDFilter::create;
    }
}