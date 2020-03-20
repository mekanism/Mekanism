package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTagFilter<FILTER extends ITagFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTextFilter<FILTER, TILE, CONTAINER> {

    protected GuiTagFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();

        if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
            updateStackList(filter.getTagName());
        }
    }

    protected abstract void updateStackList(String oreName);

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return super.wasTextboxKey(c, i) || c == ':' || c == '/';
    }

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
            return;
        } else if (name.equals(filter.getTagName())) {
            status = MekanismLang.TAG_FILTER_SAME_TAG.translateColored(EnumColor.DARK_RED);
            return;
        }
        updateStackList(name);
        filter.setTagName(name);
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.TAG_FILTER), 43, 6, 0x404040);
        drawString(MekanismLang.STATUS.translate(status), 35, 20, 0x00CD00);
        renderScaledText(MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName()), 35, 32, 0x00CD00, 107);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}