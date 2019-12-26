package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiModIDFilter<FILTER extends IModIDFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTextFilter<FILTER, TILE, CONTAINER> {

    protected GuiModIDFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract void updateStackList(String modName);

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.MODID_FILTER_NO_ID.translateColored(EnumColor.DARK_RED);
            return;
        } else if (name.equals(filter.getModID())) {
            status = MekanismLang.MODID_FILTER_SAME_ID.translateColored(EnumColor.DARK_RED);
            return;
        }
        updateStackList(name);
        filter.setModID(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.MODID_FILTER), 43, 6, 0x404040);
        drawString(MekanismLang.STATUS.translate(status), 35, 20, 0x00CD00);
        renderScaledText(MekanismLang.MODID_FILTER_ID.translate(filter.getModID()), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}