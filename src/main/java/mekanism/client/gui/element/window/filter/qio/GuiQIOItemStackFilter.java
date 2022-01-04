package mekanism.client.gui.element.window.filter.qio;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiQIOItemStackFilter extends GuiItemStackFilter<QIOItemStackFilter, TileEntityQIOFilterHandler> implements GuiQIOFilterHelper {

    public static GuiQIOItemStackFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 185) / 2, 15, tile, null);
    }

    public static GuiQIOItemStackFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOItemStackFilter filter) {
        return new GuiQIOItemStackFilter(gui, (gui.getWidth() - 185) / 2, 15, tile, filter);
    }

    private GuiQIOItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityQIOFilterHandler tile, @Nullable QIOItemStackFilter origFilter) {
        super(gui, x, y, 185, 90, tile, origFilter);
    }

    @Override
    protected int getLeftButtonX() {
        return relativeX + 29;
    }

    @Override
    protected void init() {
        super.init();
        addChild(new MekanismImageButton(gui(), relativeX + 148, relativeY + 18, 11, 14, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
    }

    @Override
    protected QIOItemStackFilter createNewFilter() {
        return new QIOItemStackFilter();
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawString(matrix, OnOff.of(filter.fuzzyMode).getTextComponent(), relativeX + 161, relativeY + 20, titleTextColor());
    }
}