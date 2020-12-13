package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiTagFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterTagFilter extends GuiTagFilter<SorterTagFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterTagFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, null);
    }

    public static GuiSorterTagFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterTagFilter filter) {
        return new GuiSorterTagFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, filter);
    }

    private GuiSorterTagFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterTagFilter origFilter) {
        super(gui, x, y, 182, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(gui(), filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected SorterTagFilter createNewFilter() {
        return new SorterTagFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter);
    }
}