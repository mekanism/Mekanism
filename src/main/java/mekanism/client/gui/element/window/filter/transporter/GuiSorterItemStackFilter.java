package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiSorterItemStackFilter extends GuiItemStackFilter<SorterItemStackFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterItemStackFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterItemStackFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, null);
    }

    public static GuiSorterItemStackFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterItemStackFilter filter) {
        return new GuiSorterItemStackFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterItemStackFilter origFilter) {
        super(gui, x, y, SORTER_FILTER_WIDTH, 90, tile, origFilter);
    }

    @Override
    protected int getLeftButtonX() {
        return relativeX + 24;
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(gui(), getSlotOffset(), this::addChild, (min, max) -> {
            minField = min;
            maxField = max;
        });
        addChild(new MekanismImageButton(gui(), relativeX + 148, relativeY + 68, 11, 14, getButtonLocation("fuzzy"), (element, mouseX, mouseY) -> {
            filter.fuzzyMode = !filter.fuzzyMode;
            return true;
        })).setTooltip(MekanismLang.FUZZY_MODE);
    }

    @Override
    protected void validateAndSave() {
        validateAndSaveSorterFilter(this, minField, maxField);
    }

    @Override
    protected SorterItemStackFilter createNewFilter() {
        return new SorterItemStackFilter();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        renderSorterForeground(guiGraphics);
        drawScrollingString(guiGraphics, OnOff.of(filter.fuzzyMode).getTextComponent(), 159, 71, TextAlignment.LEFT, titleTextColor(), width - 159, 2, false);
    }

    @Override
    public boolean isSingleItem() {
        return tile.getSingleItem();
    }
}