package mekanism.client.gui.element.window.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiTagFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class GuiSorterTagFilter extends GuiTagFilter<SorterTagFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterTagFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterTagFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, null);
    }

    public static GuiSorterTagFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterTagFilter filter) {
        return new GuiSorterTagFilter(gui, (gui.getXSize() - SORTER_FILTER_WIDTH) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterTagFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterTagFilter origFilter) {
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
    protected SorterTagFilter createNewFilter() {
        return new SorterTagFilter();
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