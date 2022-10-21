package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiTagFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import org.jetbrains.annotations.Nullable;

public class GuiSorterTagFilter extends GuiTagFilter<SorterTagFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterTagFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, null);
    }

    public static GuiSorterTagFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterTagFilter filter) {
        return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterTagFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterTagFilter origFilter) {
        super(gui, x, y, 195, 90, tile, origFilter);
    }

    @Override
    protected int getLeftButtonX() {
        return relativeX + 24;
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(gui(), filter, getSlotOffset(), this::addChild, tile::getSingleItem, (min, max) -> {
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
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter, tile.getSingleItem());
    }
}