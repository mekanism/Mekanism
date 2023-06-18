package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.TileEntityLogisticalSorter;
import org.jetbrains.annotations.NotNull;

public class GuiSorterFilerSelect extends GuiFilterSelect<TileEntityLogisticalSorter> {

    public GuiSorterFilerSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        super(gui, tile, 3);
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getItemStackFilterCreator() {
        return GuiSorterItemStackFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getTagFilterCreator() {
        return GuiSorterTagFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getModIDFilterCreator() {
        return GuiSorterModIDFilter::create;
    }
}