package mekanism.client.gui.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.client.gui.element.window.filter.qio.GuiQIOItemStackFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOModIDFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOTagFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;

public class GuiQIOFilerSelect extends GuiFilterSelect<TileEntityQIOFilterHandler> {

    public GuiQIOFilerSelect(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        super(gui, tile, 3);
    }

    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getItemStackFilterCreator() {
        return GuiQIOItemStackFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getTagFilterCreator() {
        return GuiQIOTagFilter::create;
    }

    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getModIDFilterCreator() {
        return GuiQIOModIDFilter::create;
    }
}