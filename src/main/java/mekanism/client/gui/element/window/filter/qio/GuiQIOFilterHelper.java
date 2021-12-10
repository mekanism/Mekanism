package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.gui.qio.GuiQIOFilerSelect;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;

public interface GuiQIOFilterHelper extends GuiFilterHelper<TileEntityQIOFilterHandler> {

    @Override
    default GuiQIOFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOFilerSelect(gui, tile);
    }
}