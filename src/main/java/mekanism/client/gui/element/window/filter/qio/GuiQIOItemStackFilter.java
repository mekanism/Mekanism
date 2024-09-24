package mekanism.client.gui.element.window.filter.qio;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiQIOItemStackFilter extends GuiItemStackFilter<QIOItemStackFilter, TileEntityQIOFilterHandler> implements GuiQIOFilterHelper {

    public static GuiQIOItemStackFilter create(IGuiWrapper gui, TileEntityQIOFilterHandler tile) {
        return new GuiQIOItemStackFilter(gui, (gui.getXSize() - 185) / 2, 15, tile, null);
    }

    public static GuiQIOItemStackFilter edit(IGuiWrapper gui, TileEntityQIOFilterHandler tile, QIOItemStackFilter filter) {
        return new GuiQIOItemStackFilter(gui, (gui.getXSize() - 185) / 2, 15, tile, filter);
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
        addChild(new MekanismImageButton(gui(), relativeX + 148, relativeY + 18, 11, 14, getButtonLocation("fuzzy"), (element, mouseX, mouseY) -> {
            filter.fuzzyMode = !filter.fuzzyMode;
            return true;
        })).setTooltip(MekanismLang.FUZZY_MODE);
    }

    @Override
    protected QIOItemStackFilter createNewFilter() {
        return new QIOItemStackFilter();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawScrollingString(guiGraphics, OnOff.of(filter.fuzzyMode).getTextComponent(), 159, 20, TextAlignment.LEFT, titleTextColor(), width - 161, 2, false);
    }
}