package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
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

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterMaterialFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterMaterialFilter origFilter) {
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
        validateAndSaveSorterFilter(this, minField, maxField);
    }

    @Override
    protected SorterMaterialFilter createNewFilter() {
        return new SorterMaterialFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter, tile.getSingleItem());
    }
}