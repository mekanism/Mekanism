package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiModIDFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiSorterModIDFilter extends GuiModIDFilter<SorterModIDFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterModIDFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterModIDFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, null);
    }

    public static GuiSorterModIDFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterModIDFilter filter) {
        return new GuiSorterModIDFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterModIDFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterModIDFilter origFilter) {
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
    }

    @Override
    protected void validateAndSave() {
        if (text.getText().isEmpty() || setText()) {
            validateAndSaveSorterFilter(this, minField, maxField);
        }
    }

    @Override
    protected SorterModIDFilter createNewFilter() {
        return new SorterModIDFilter();
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        renderSorterForeground(guiGraphics);
    }

    @Override
    public boolean isSingleItem() {
        return tile.getSingleItem();
    }
}