package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import mekanism.common.util.text.InputValidator;
import net.minecraft.client.gui.screen.Screen;

public interface GuiSorterFilterHelper extends GuiFilterHelper<TileEntityLogisticalSorter>, IFancyFontRenderer {

    default void addSorterDefaults(IGuiWrapper gui, SorterFilter<?> filter, int slotOffset, UnaryOperator<GuiElement> childAdder, BooleanSupplier singleItem,
          BiConsumer<GuiTextField, GuiTextField> rangeSetter) {
        int relativeX = getRelativeX();
        int relativeY = getRelativeY();
        int slotX = relativeX + 7;
        int colorSlotY = relativeY + slotOffset + 25;
        childAdder.apply(new GuiSlot(SlotType.NORMAL, gui, slotX, colorSlotY));
        childAdder.apply(new ColorButton(gui, slotX + 1, colorSlotY + 1, 16, 16, () -> filter.color,
              () -> filter.color = Screen.hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
        childAdder.apply(new MekanismImageButton(gui, relativeX + 148, relativeY + 18, 11, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "default.png"),
              () -> filter.allowDefault = !filter.allowDefault, (onHover, matrix, xAxis, yAxis) -> gui.displayTooltip(matrix, MekanismLang.FILTER_ALLOW_DEFAULT.translate(),
              xAxis, yAxis)));
        GuiTextField minField = new GuiTextField(gui, relativeX + 169, relativeY + 31, 20, 11);
        minField.setMaxStringLength(2);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.setText("" + filter.min);
        childAdder.apply(minField);
        GuiTextField maxField = new GuiTextField(gui, relativeX + 169, relativeY + 43, 20, 11);
        maxField.setMaxStringLength(2);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.setText("" + filter.max);
        childAdder.apply(maxField);
        rangeSetter.accept(minField, maxField);
        childAdder.apply(new MekanismImageButton(gui, relativeX + 148, relativeY + 56, 11, 14, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "silk_touch.png"),
              () -> filter.sizeMode = !filter.sizeMode, (onHover, matrix, xAxis, yAxis) -> {
            if (singleItem.getAsBoolean() && filter.sizeMode) {
                gui.displayTooltip(matrix, MekanismLang.SORTER_SIZE_MODE_CONFLICT.translate(), xAxis, yAxis);
            } else {
                gui.displayTooltip(matrix, MekanismLang.SORTER_SIZE_MODE.translate(), xAxis, yAxis);
            }
        }));
    }

    @Override
    default GuiSorterFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterFilerSelect(gui, tile);
    }

    default void renderSorterForeground(MatrixStack matrix, SorterFilter<?> filter, boolean singleItem) {
        int relativeX = getRelativeX();
        int relativeY = getRelativeY();
        drawString(matrix, OnOff.of(filter.allowDefault).getTextComponent(), relativeX + 161, relativeY + 20, titleTextColor());
        drawString(matrix, MekanismLang.MIN.translate(""), relativeX + 148, relativeY + 32, titleTextColor());
        drawString(matrix, MekanismLang.MAX.translate(""), relativeX + 148, relativeY + 44, titleTextColor());
        if (singleItem && filter.sizeMode) {
            drawString(matrix, MekanismLang.SORTER_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.of(true)), relativeX + 161, relativeY + 58, titleTextColor());
        } else {
            drawString(matrix, OnOff.of(filter.sizeMode).getTextComponent(), relativeX + 161, relativeY + 58, titleTextColor());
        }
    }
}