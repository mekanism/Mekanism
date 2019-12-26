package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTypeFilter<FILTER, TILE, CONTAINER> {

    protected GuiItemStackFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.ITEM_FILTER), 43, 6, 0x404040);
        drawString(MekanismLang.STATUS.translate(status), 35, 20, 0x00CD00);
        drawString(MekanismLang.ITEM_FILTER_DETAILS.translate(), 35, 32, 0x00CD00);
        drawForegroundLayer(mouseX, mouseY);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}