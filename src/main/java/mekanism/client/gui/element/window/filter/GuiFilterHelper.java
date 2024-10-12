package mekanism.client.gui.element.window.filter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.content.filter.IFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import org.jetbrains.annotations.Nullable;

public interface GuiFilterHelper<TILE extends TileEntityMekanism & ITileFilterHolder<?>> {

    @Nullable
    GuiFilterSelect<TILE> getFilterSelect(IGuiWrapper gui, TILE tile);

    default boolean hasFilterSelect() {
        return true;
    }

    int getRelativeX();

    int getRelativeY();

    IFilter<?> getFilter();

    int getScreenWidth();
}