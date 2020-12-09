package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
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

    private GuiSorterItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterItemStackFilter origFilter) {
        super(gui, x, y, 195, 90, tile, origFilter);
    }

    @Override
    protected int getLeftButtonX() {
        return x + 24;
    }

    @Override
    protected void init() {
        super.init();
        addSorterDefaults(guiObj, filter, getSlotOffset(), this::addChild);
        addChild(minField = new GuiTextField(guiObj, relativeX + 169, relativeY + 32, 20, 11));
        minField.setMaxStringLength(2);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.setText("" + filter.min);
        addChild(maxField = new GuiTextField(guiObj, relativeX + 169, relativeY + 44, 20, 11));
        maxField.setMaxStringLength(2);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.setText("" + filter.max);
        addChild(new MekanismImageButton(guiObj, x + 148, y + 57, 11, 14, getButtonLocation("silk_touch"),
              () -> filter.sizeMode = !filter.sizeMode, (onHover, matrix, xAxis, yAxis) -> {
            if (tile.singleItem && filter.sizeMode) {
                displayTooltip(matrix, MekanismLang.SIZE_MODE_CONFLICT.translate(), xAxis, yAxis);
            } else {
                displayTooltip(matrix, MekanismLang.SIZE_MODE.translate(), xAxis, yAxis);
            }
        }));
        addChild(new MekanismImageButton(guiObj, x + 148, y + 70, 11, 14, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
    }

    @Override
    protected void validateAndSave() {
        if (filter.hasFilter()) {
            if (minField.getText().isEmpty() || maxField.getText().isEmpty()) {
                filterSaveFailed(MekanismLang.ITEM_FILTER_SIZE_MISSING);
            } else {
                int min = Integer.parseInt(minField.getText());
                int max = Integer.parseInt(maxField.getText());
                if (max >= min && max <= 64) {
                    filter.min = min;
                    filter.max = max;
                    saveFilter();
                } else if (min > max) {
                    filterSaveFailed(MekanismLang.ITEM_FILTER_MAX_LESS_THAN_MIN);
                } else { //if(max > 64 || min > 64)
                    filterSaveFailed(MekanismLang.ITEM_FILTER_OVER_SIZED);
                }
            }
        } else {
            filterSaveFailed(getNoFilterSaveError());
        }
    }

    @Override
    protected SorterItemStackFilter createNewFilter() {
        return new SorterItemStackFilter();
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        renderSorterForeground(matrix, filter);
        drawString(matrix, MekanismLang.MIN.translate(""), relativeX + 148, relativeY + 33, titleTextColor());
        drawString(matrix, MekanismLang.MAX.translate(""), relativeX + 148, relativeY + 45, titleTextColor());
        if (tile.singleItem && filter.sizeMode) {
            drawString(matrix, MekanismLang.ITEM_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.of(true)), relativeX + 161, relativeY + 59, titleTextColor());
        } else {
            drawString(matrix, OnOff.of(filter.sizeMode).getTextComponent(), relativeX + 161, relativeY + 59, titleTextColor());
        }
        drawString(matrix, OnOff.of(filter.fuzzyMode).getTextComponent(), relativeX + 161, relativeY + 72, titleTextColor());
    }
}