package mekanism.client.gui.element.window.filter.transporter;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Consumer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.gui.element.window.filter.GuiFilterSelect;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.screen.Screen;

public interface GuiSorterFilterHelper extends GuiFilterHelper<TileEntityLogisticalSorter>, IFancyFontRenderer {

    default void addSorterDefaults(IGuiWrapper gui, SorterFilter<?> filter, int slotOffset, Consumer<GuiElement> childAdder) {
        int slotX = getRelativeX() + 7;
        int colorSlotY = getRelativeY() + slotOffset + 25;
        childAdder.accept(new GuiSlot(SlotType.NORMAL, gui, slotX, colorSlotY));
        childAdder.accept(new ColorButton(gui, gui.getLeft() + slotX + 1, gui.getTop() + colorSlotY + 1, 16, 16, () -> filter.color,
              () -> filter.color = Screen.hasShiftDown() ? null : TransporterUtils.increment(filter.color), () -> filter.color = TransporterUtils.decrement(filter.color)));
        childAdder.accept(new MekanismImageButton(gui, gui.getLeft() + getRelativeX() + 148, gui.getTop() + getRelativeY() + 19, 11,
              MekanismUtils.getResource(ResourceType.GUI_BUTTON, "default.png"), () -> filter.allowDefault = !filter.allowDefault,
              (onHover, matrix, xAxis, yAxis) -> gui.displayTooltip(matrix, MekanismLang.FILTER_ALLOW_DEFAULT.translate(), xAxis, yAxis)));
    }

    @Override
    default GuiFilterSelect getFilterSelect(IGuiWrapper gui, TileEntityLogisticalSorter tile) {
        return new GuiSorterFilerSelect(gui, tile);
    }

    default void renderSorterForeground(MatrixStack matrix, SorterFilter<?> filter) {
        drawString(matrix, OnOff.of(filter.allowDefault).getTextComponent(), getRelativeX() + 161, getRelativeY() + 21, titleTextColor());
    }
}