package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiTagFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;

public class GuiQIOTagFilter extends GuiTagFilter<QIOTagFilter, TileEntityQIOFilterHandler> {

    public static GuiQIOTagFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOTagFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiQIOTagFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOTagFilter filter) {
        return new GuiQIOTagFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiQIOTagFilter(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, QIOTagFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected QIOTagFilter createNewFilter() {
        return new QIOTagFilter();
    }
}