package mekanism.client.gui.filter;

import java.util.Arrays;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiInnerScreen;
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

    @Override
    public void init() {
        super.init();

        if (filter.getModID() != null && !filter.getModID().isEmpty()) {
            updateRenderStacks();
        }
    }

    @Override
    protected void addButtons() {
        addButton(new GuiInnerScreen(this, 33, 18, 111, 43, () -> Arrays.asList(
            MekanismLang.STATUS.translate(status),
            MekanismLang.MODID_FILTER_ID.translate(filter.getModID())
        )).clearFormat());
    }

    @Override
    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.MODID_FILTER_NO_ID.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        } else if (name.equals(filter.getModID())) {
            status = MekanismLang.MODID_FILTER_SAME_ID.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        }
        filter.setModID(name);
        updateRenderStacks();
        text.setText("");
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString((isNew ? MekanismLang.FILTER_NEW : MekanismLang.FILTER_EDIT).translate(MekanismLang.MODID_FILTER), 43, 6, titleTextColor());
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}