package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.gui.warning.WarningTracker.WarningType;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.SelectedWindowData;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IGuiWrapper {

    default void displayTooltip(MatrixStack matrix, ITextComponent component, int x, int y, int maxWidth) {
        this.displayTooltips(matrix, Collections.singletonList(component), x, y, maxWidth);
    }

    default void displayTooltip(MatrixStack matrix, ITextComponent component, int x, int y) {
        this.displayTooltips(matrix, Collections.singletonList(component), x, y);
    }

    default void displayTooltips(MatrixStack matrix, List<ITextComponent> components, int xAxis, int yAxis) {
        displayTooltips(matrix, components, xAxis, yAxis, -1);
    }

    default void displayTooltips(MatrixStack matrix, List<ITextComponent> components, int xAxis, int yAxis, int maxWidth) {
        //TODO - 1.18: Re-evaluate some form of this that wraps further along for use in Gui Windows (such as viewing descriptions of supported upgrades)
        net.minecraftforge.fml.client.gui.GuiUtils.drawHoveringText(matrix, components, xAxis, yAxis, getWidth(), getHeight(), maxWidth, getFont());
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

    default boolean currentlyQuickCrafting() {
        return false;
    }

    @Nullable
    default GuiWindow getWindowHovering(double mouseX, double mouseY) {
        Mekanism.logger.error("Tried to call 'getWindowHovering' but unsupported in {}", getClass().getName());
        return null;
    }

    @Nonnull
    default BooleanSupplier trackWarning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        Mekanism.logger.error("Tried to call 'trackWarning' but unsupported in {}", getClass().getName());
        return warningSupplier;
    }

    @Nullable
    FontRenderer getFont();

    default void renderItem(MatrixStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderItem(matrix, stack, xAxis, yAxis, 1);
    }

    default void renderItem(MatrixStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        GuiUtils.renderItem(matrix, getItemRenderer(), stack, xAxis, yAxis, scale, getFont(), null, false);
    }

    ItemRenderer getItemRenderer();

    default void renderItemTooltip(MatrixStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis) {
        Mekanism.logger.error("Tried to call 'renderItemTooltip' but unsupported in {}", getClass().getName());
    }

    default void renderItemTooltipWithExtra(MatrixStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, List<ITextComponent> toAppend) {
        if (toAppend.isEmpty()) {
            renderItemTooltip(matrix, stack, xAxis, yAxis);
        } else {
            Mekanism.logger.error("Tried to call 'renderItemTooltipWithExtra' but unsupported in {}", getClass().getName());
        }
    }

    default void renderItemWithOverlay(MatrixStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
        GuiUtils.renderItem(matrix, getItemRenderer(), stack, xAxis, yAxis, scale, getFont(), text, true);
    }

    default void setSelectedWindow(SelectedWindowData selectedWindow) {
        Mekanism.logger.error("Tried to call 'setSelectedWindow' but unsupported in {}", getClass().getName());
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