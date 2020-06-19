package mekanism.client.gui.element.filter.transporter;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.filter.GuiItemStackFilter;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;

public class GuiSorterItemStackFilter extends GuiItemStackFilter<SorterItemStackFilter, TileEntityLogisticalSorter> implements GuiSorterFilterHelper {

    public static GuiSorterItemStackFilter create(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 152) / 2, 30, tile, null);
    }

    public static GuiSorterItemStackFilter edit(IGuiWrapper gui, TileEntityLogisticalSorter tile, SorterItemStackFilter filter) {
        return new GuiSorterItemStackFilter(gui, (gui.getWidth() - 152) / 2, 30, tile, filter);
    }

    private GuiTextField minField;
    private GuiTextField maxField;

    private GuiSorterItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityLogisticalSorter tile, SorterItemStackFilter origFilter) {
        super(gui, x, y, 152, 90, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        addChild(minField = new GuiTextField(guiObj, 149, 19, 20, 11));
        minField.setMaxStringLength(2);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.setText("" + filter.min);
        addChild(maxField = new GuiTextField(guiObj, 149, 31, 20, 11));
        maxField.setMaxStringLength(2);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.setText("" + filter.max);

        addSorterDefaults(guiObj, filter, getSlotOffset(), this::addChild);
        addChild(new MekanismImageButton(guiObj, guiObj.getLeft() + 11, guiObj.getTop() + 72, 10, getButtonLocation("fuzzy"),
              () -> filter.fuzzyMode = !filter.fuzzyMode, getOnHover(MekanismLang.FUZZY_MODE)));
        addChild(new MekanismImageButton(guiObj, guiObj.getLeft() + 128, guiObj.getTop() + 44, 11, 14, getButtonLocation("silk_touch"),
              () -> filter.sizeMode = !filter.sizeMode, (onHover, xAxis, yAxis) -> {
            if (tile.singleItem && filter.sizeMode) {
                displayTooltip(MekanismLang.SIZE_MODE_CONFLICT.translate(), xAxis, yAxis);
            } else {
                displayTooltip(MekanismLang.SIZE_MODE.translate(), xAxis, yAxis);
            }
        }));
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
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawString(MekanismLang.MIN.translate(""), relativeX + 128, relativeY + 20, titleTextColor());
        drawString(MekanismLang.MAX.translate(""), relativeX + 128, relativeY + 32, titleTextColor());
        if (tile.singleItem && filter.sizeMode) {
            drawString(MekanismLang.ITEM_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.of(filter.sizeMode)), relativeX + 141, relativeY + 46, titleTextColor());
        } else {
            drawString(OnOff.of(filter.sizeMode).getTextComponent(), relativeX + 141, relativeY + 46, titleTextColor());
        }
        drawString(OnOff.of(filter.fuzzyMode).getTextComponent(), relativeX + 24, relativeY + 74, titleTextColor());
        drawString(OnOff.of(filter.allowDefault).getTextComponent(), relativeX + 24, relativeY + 64, titleTextColor());
    }
}