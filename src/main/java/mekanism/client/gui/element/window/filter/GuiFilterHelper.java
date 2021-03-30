package mekanism.client.gui.element.window.filter;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;

public interface GuiFilterHelper<TILE extends TileEntityMekanism & ITileFilterHolder<?>> {

    @Nullable
    default GuiFilterSelect<TILE> getFilterSelect(IGuiWrapper gui, TILE tile) {
        return null;
    }

    int getRelativeX();

    int getRelativeY();
}