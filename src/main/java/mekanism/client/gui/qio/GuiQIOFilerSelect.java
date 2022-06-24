package mekanism.client.gui.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.client.gui.element.window.filter.qio.GuiQIOItemStackFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOModIDFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOTagFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import org.jetbrains.annotations.NotNull;

public class GuiQIOFilerSelect extends GuiFilterSelect<TileEntityQIOFilterHandler> {

    public GuiQIOFilerSelect(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        super(gui, tile, 3);
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getItemStackFilterCreator() {
        return GuiQIOItemStackFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getTagFilterCreator() {
        return GuiQIOTagFilter::create;
    }

    @NotNull
    @Override
    protected GuiFilterCreator<TileEntityQIOFilterHandler> getModIDFilterCreator() {
        return GuiQIOModIDFilter::create;
    }
}