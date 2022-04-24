package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSorterItemStackFilter extends GuiItemStackFilter<SorterItemStackFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterItemStackFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 195) / 2, 30, tile, null);
    }

    public static GuiSorterItemStackFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterItemStackFilter filter) {
        return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 195) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, @Nullable SorterItemStackFilter origFilter) {
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
        addChild(new MekanismImageButton(gui(), relativeX + 148, relativeY + 68, 11, 14, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
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
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter, tile.getSingleItem());
        drawString(matrix, OnOff.of(filter.fuzzyMode).getTextComponent(), relativeX + 161, relativeY + 71, titleTextColor());
    }
}