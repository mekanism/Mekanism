package mekanism.client.gui.element.window.filter;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.base.TileEntityMekanism;

public interface GuiFilterHelper<TILE extends TileEntityMekanism> {

    @Nullable
    default GuiFilterSelect getFilterSelect(IGuiWrapper gui, TILE tile) {
        return null;
    }

    int getRelativeX();

    int getRelativeY();
}