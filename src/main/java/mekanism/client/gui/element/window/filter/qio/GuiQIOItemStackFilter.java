package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;

public class GuiQIOItemStackFilter extends GuiItemStackFilter<QIOItemStackFilter, TileEntityQIOFilterHandler> {

    public static GuiQIOItemStackFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiQIOItemStackFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOItemStackFilter filter) {
        return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiQIOItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, QIOItemStackFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected QIOItemStackFilter createNewFilter() {
        return new QIOItemStackFilter();
    }
}