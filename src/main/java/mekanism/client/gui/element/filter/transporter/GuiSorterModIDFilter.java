package mekanism.client.gui.element.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.filter.GuiModIDFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterModIDFilter extends GuiModIDFilter<SorterModIDFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterModIDFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterModIDFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiSorterModIDFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterModIDFilter filter) {
        return new GuiSorterModIDFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiSorterModIDFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterModIDFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected SorterModIDFilter createNewFilter() {
        return new SorterModIDFilter();
    }
}