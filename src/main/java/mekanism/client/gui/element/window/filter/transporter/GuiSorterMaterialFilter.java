package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiMaterialFilter;
import mekanism.common.content.transporter.SorterMaterialFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;

public class GuiSorterMaterialFilter extends GuiMaterialFilter<SorterMaterialFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterMaterialFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterMaterialFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, null);
    }

    public static GuiSorterMaterialFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterMaterialFilter filter) {
        return new GuiSorterMaterialFilter(gui, (gui.getWidth() - 182) / 2, 30, tile, filter);
    }

    private GuiSorterMaterialFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterMaterialFilter origFilter) {
        super(gui, x, y, 182, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(gui(), filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected SorterMaterialFilter createNewFilter() {
        return new SorterMaterialFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter);
    }
}