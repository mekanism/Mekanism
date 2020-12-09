package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterFilerSelect extends GuiFilterSelect {

    private final TileEntityLogisticalSorter tile;

    public GuiSorterFilerSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        super(gui);
        this.tile = tile;
    }

    @Override
    protected GuiSorterItemStackFilter createNewItemStackFilter() {
        return GuiSorterItemStackFilter.create(getGuiObj(), tile);
    }

    @Override
    protected GuiSorterTagFilter createNewTagFilter() {
        return GuiSorterTagFilter.create(getGuiObj(), tile);
    }

    @Override
    protected GuiSorterMaterialFilter createNewMaterialFilter() {
        return GuiSorterMaterialFilter.create(getGuiObj(), tile);
    }

    @Override
    protected GuiSorterModIDFilter createNewModIDFilter() {
        return GuiSorterModIDFilter.create(getGuiObj(), tile);
    }
}