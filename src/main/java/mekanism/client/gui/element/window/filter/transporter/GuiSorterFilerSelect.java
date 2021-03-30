package mekanism.client.gui.element.window.filter.transporter;

import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterFilerSelect extends GuiFilterSelect<TileEntityLogisticalSorter> {

    public GuiSorterFilerSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        super(gui, tile, 4);
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getItemStackFilterCreator() {
        return GuiSorterItemStackFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getTagFilterCreator() {
        return GuiSorterTagFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getMaterialFilterCreator() {
        return GuiSorterMaterialFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityLogisticalSorter> getModIDFilterCreator() {
        return GuiSorterModIDFilter::create;
    }
}