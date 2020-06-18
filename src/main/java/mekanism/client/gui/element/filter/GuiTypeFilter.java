package mekanism.client.gui.element.filter;

import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

@Deprecated
public abstract class GuiTypeFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiFilterBase<FILTER, TILE, CONTAINER> {

    protected GuiTypeFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected boolean overTypeInput(double xAxis, double yAxis) {
        return xAxis >= 12 && xAxis <= 28 && yAxis >= 19 && yAxis <= 35;
    }

    protected abstract void drawForegroundLayer(int mouseX, int mouseY);
}