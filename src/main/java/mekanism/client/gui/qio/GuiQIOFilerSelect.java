package mekanism.client.gui.qio;

import javax.annotation.Nonnull;
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

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getItemStackFilterCreator() {
        return GuiQIOItemStackFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getTagFilterCreator() {
        return GuiQIOTagFilter::create;
    }

    @Nonnull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getModIDFilterCreator() {
        return GuiQIOModIDFilter::create;
    }
}