package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiModIDFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterModIDFilter extends GuiModIDFilter<SorterModIDFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterModIDFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterModIDFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, null);
    }

    public static GuiSorterModIDFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterModIDFilter filter) {
        return new GuiSorterModIDFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, filter);
    }

    private GuiSorterModIDFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterModIDFilter origFilter) {
        super(gui, x, y, 182, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected SorterModIDFilter createNewFilter() {
        return new SorterModIDFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter);
    }
}