package mekanism.client.gui.filter;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
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
    protected boolean wasTextboxKey(char c) {
        return super.wasTextboxKey(c) || c == ':' || c == '/';
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43, () -> Arrays.asList(
            MekanismLang.STATUS.translate(status),
            MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName())
        )).clearFormat());
    }

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        } else if (name.equals(filter.getTagName())) {
            status = MekanismLang.TAG_FILTER_SAME_TAG.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        }
        filter.setTagName(name);
        updateRenderStacks();
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.TAG_FILTER), 43, 6, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}