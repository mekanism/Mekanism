package mekanism.client.gui.element.window.filter.transporter;

import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TooltipToggleButton;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;

public interface GuiSorterFilterHelper extends GuiFilterHelper<TileEntityLogisticalSorter>, IFancyFontRenderer, ContainerEventHandler {

    int SORTER_FILTER_WIDTH = 200;

    @Override
    SorterFilter<?> getFilter();

    boolean isSingleItem();

    default void addSorterDefaults(IGuiWrapper gui, int slotOffset, UnaryOperator<GuiElement> childAdder, BiConsumer<GuiTextField, GuiTextField> rangeSetter) {
        int relativeX = getRelativeX();
        int relativeY = getRelativeY();
        int slotX = relativeX + 7;
        int colorSlotY = relativeY + slotOffset + 25;
        childAdder.apply(new GuiSlot(SlotType.NORMAL, gui, slotX, colorSlotY));
        childAdder.apply(new ColorButton(gui, slotX + 1, colorSlotY + 1, 16, 16, () -> getFilter().color, (element, mouseX, mouseY) -> {
            SorterFilter<?> filter = getFilter();
            filter.color = Screen.hasShiftDown() ? null : TransporterUtils.increment(filter.color);
            return true;
        }, (element, mouseX, mouseY) -> {
            SorterFilter<?> filter = getFilter();
            filter.color = TransporterUtils.decrement(filter.color);
            return true;
        }));
        childAdder.apply(new MekanismImageButton(gui, relativeX + 148, relativeY + 18, 11, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "default.png"), (element, mouseX, mouseY) -> {
            SorterFilter<?> filter = getFilter();
            filter.allowDefault = !filter.allowDefault;
            return true;
        })).setTooltip(MekanismLang.FILTER_ALLOW_DEFAULT);
        int maxStackSizeDigits = Integer.toString(Item.ABSOLUTE_MAX_STACK_SIZE).length();
        GuiTextField minField = new GuiTextField(gui, this, relativeX + 174, relativeY + 31, 20, 11);
        minField.setMaxLength(maxStackSizeDigits);
        minField.setInputValidator(InputValidator.DIGIT);
        minField.setText(Integer.toString(getFilter().min));
        childAdder.apply(minField);
        GuiTextField maxField = new GuiTextField(gui, this, relativeX + 174, relativeY + 43, 20, 11);
        maxField.setMaxLength(maxStackSizeDigits);
        maxField.setInputValidator(InputValidator.DIGIT);
        maxField.setText(Integer.toString(getFilter().max));
        childAdder.apply(maxField);
        rangeSetter.accept(minField, maxField);
        childAdder.apply(new TooltipToggleButton(gui, relativeX + 148, relativeY + 56, 11, 14, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "silk_touch.png"),
              () -> isSingleItem() && getFilter().isEnabled(), (element, mouseX, mouseY) -> {
                  SorterFilter<?> filter = getFilter();
                  filter.sizeMode = !filter.sizeMode;
                  return true;
              }, MekanismLang.SORTER_SIZE_MODE_CONFLICT.translate(), MekanismLang.SORTER_SIZE_MODE.translate()));
    }

    @Override
    default GuiSorterFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterFilerSelect(gui, tile);
    }

    default void renderSorterForeground(GuiGraphics guiGraphics) {
        SorterFilter<?> filter = getFilter();
        int screenEnd = 30 + getScreenWidth();
        drawScrollingString(guiGraphics, OnOff.of(filter.allowDefault).getTextComponent(), 159, 20, TextAlignment.LEFT, titleTextColor(), getXSize() - 161, 2, false);
        //Note: We add two to the length, as min and max have two spaces at the end given we aren't actually providing a parameter
        //TODO: Do we want to adjust this, and just have separate lang keys for them?
        drawScrollingString(guiGraphics, MekanismLang.MIN.translate(""), screenEnd, 32, TextAlignment.LEFT, titleTextColor(), 173 - screenEnd, 2, false);
        drawScrollingString(guiGraphics, MekanismLang.MAX.translate(""), screenEnd, 44, TextAlignment.LEFT, titleTextColor(), 173 - screenEnd, 2, false);
        if (isSingleItem() && filter.sizeMode) {
            drawScrollingString(guiGraphics, MekanismLang.SORTER_FILTER_SIZE_MODE.translateColored(EnumColor.RED, OnOff.ON), 159, 58, TextAlignment.LEFT, titleTextColor(), getXSize() - 159, 2, false);
        } else {
            drawScrollingString(guiGraphics, OnOff.of(filter.sizeMode).getTextComponent(), 159, 58, TextAlignment.LEFT, titleTextColor(), getXSize() - 161, 2, false);
        }
    }
}