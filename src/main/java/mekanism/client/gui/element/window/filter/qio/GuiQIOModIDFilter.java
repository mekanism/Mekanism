package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiModIDFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import org.jetbrains.annotations.Nullable;

public class GuiQIOModIDFilter extends GuiModIDFilter<QIOModIDFilter, TileEntityQIOFilterHandler> implements GuiQIOFilterHelper {

    public static GuiQIOModIDFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOModIDFilter(gui, (gui.getXSize() - 152) / 2, 15, tile, null);
    }

    public static GuiQIOModIDFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOModIDFilter filter) {
        return new GuiQIOModIDFilter(gui, (gui.getXSize() - 152) / 2, 15, tile, filter);
    }

    private GuiQIOModIDFilter(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, @Nullable QIOModIDFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected QIOModIDFilter createNewFilter() {
        return new QIOModIDFilter();
    }
}