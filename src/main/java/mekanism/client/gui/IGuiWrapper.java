package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiWindow;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public interface IGuiWrapper {

    default void displayTooltip(ITextComponent component, int x, int y, int maxWidth) {
        this.displayTooltips(Collections.singletonList(component), x, y, maxWidth);
    }

    default void displayTooltip(ITextComponent component, int x, int y) {
        this.displayTooltips(Collections.singletonList(component), x, y);
    }

    default void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis) {
        displayTooltips(components, xAxis, yAxis, -1);
    }

    default void displayTooltips(List<ITextComponent> components, int xAxis, int yAxis, int maxWidth) {
        List<String> toolTips = components.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList());
        GuiUtils.drawHoveringText(toolTips, xAxis, yAxis, getWidth(), getHeight(), maxWidth, getFont());
    }

    default int getLeft() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getGuiLeft();
        }
        return 0;
    }

    default int getTop() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getGuiTop();
        }
        return 0;
    }

    default int getWidth() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getXSize();
        }
        return 0;
    }

    default int getHeight() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getYSize();
        }
        return 0;
    }

    default void addWindow(GuiWindow window) {
        Mekanism.logger.error("Tried to call 'addWindow' but unsupported in {}", getClass().getName());
    }

    default void removeWindow(GuiWindow window) {
        Mekanism.logger.error("Tried to call 'removeWindow' but unsupported in {}", getClass().getName());
    }

    @Nullable
    default GuiWindow getWindowHovering(double mouseX, double mouseY) {
        Mekanism.logger.error("Tried to call 'getWindowHovering' but unsupported in {}", getClass().getName());
        return null;
    }

    @Nullable
    FontRenderer getFont();

    default void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderItem(stack, xAxis, yAxis, 1);
    }

    void renderItem(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale);

    ItemRenderer getItemRenderer();

    default void renderItemTooltip(@Nonnull ItemStack stack, int xAxis, int yAxis) {
        Mekanism.logger.error("Tried to call 'renderItemTooltip' but unsupported in {}", getClass().getName());
    }

    default void renderItemWithOverlay(@Nonnull ItemStack stack, int xAxis, int yAxis, float scale, String text) {
        Mekanism.logger.error("Tried to call 'renderItemWithOverlay' but unsupported in {}", getClass().getName());
    }

    default void addFocusListener(GuiElement element) {
        Mekanism.logger.error("Tried to call 'addFocusListener' but unsupported in {}", getClass().getName());
    }

    default void removeFocusListener(GuiElement element) {
        Mekanism.logger.error("Tried to call 'removeFocusListener' but unsupported in {}", getClass().getName());
    }

    default void focusChange(GuiElement changed) {
        Mekanism.logger.error("Tried to call 'focusChange' but unsupported in {}", getClass().getName());
    }

    default void incrementFocus(GuiElement current) {
        Mekanism.logger.error("Tried to call 'incrementFocus' but unsupported in {}", getClass().getName());
    }
}