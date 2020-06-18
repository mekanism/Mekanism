package mekanism.client.gui.element.filter.transporter;

import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.gui.screen.Screen;

public interface GuiSorterFilterHelper {

    default void addSorterDefaults(IGuiWrapper gui, SorterFilter<?> filter, int slotOffset, Consumer<GuiElement> childAdder) {
        int slotX = getRelativeX() + 7;
        int colorSlotY = getRelativeY() + slotOffset + 25;
        childAdder.accept(new GuiSlot(SlotType.NORMAL, gui, slotX, colorSlotY));
        childAdder.accept(new ColorButton(gui, gui.getLeft() + slotX + 1, gui.getTop() + colorSlotY + 1, 16, 16, () -> filter.color,
              () -> filter.color = Screen.hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
        childAdder.accept(new MekanismImageButton(gui, gui.getLeft() + 11, gui.getTop() + 64, 11, MekanismUtils.getResource(ResourceType.GUI_BUTTON, "default.png"),
              () -> filter.allowDefault = !filter.allowDefault, (onHover, xAxis, yAxis) -> gui.displayTooltip(MekanismLang.FILTER_ALLOW_DEFAULT.translate(), xAxis, yAxis)));
    }

    int getRelativeX();

    int getRelativeY();
}