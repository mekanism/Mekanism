package mekanism.client.gui.element.filter.transporter;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.filter.GuiMaterialFilter;
import mekanism.common.content.transporter.SorterMaterialFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSorterMaterialFilter extends GuiMaterialFilter<SorterMaterialFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterMaterialFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterMaterialFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
    }

    public static GuiSorterMaterialFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterMaterialFilter filter) {
        return new GuiSorterMaterialFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
    }

    private GuiSorterMaterialFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterMaterialFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(guiObj, filter, getSlotOffset(), this::addChild);
    }

    @Override
    protected SorterMaterialFilter createNewFilter() {
        return new SorterMaterialFilter();
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawString(OnOff.of(filter.allowDefault).getTextComponent(), relativeX + 24, relativeY + 66, titleTextColor());
    }
}