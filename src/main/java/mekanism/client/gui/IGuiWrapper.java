package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface IGuiWrapper {

    default void displayTooltip(PoseStack matrix, Component component, int x, int y, int maxWidth) {
        this.displayTooltips(matrix, Collections.singletonList(component), x, y, maxWidth);
    }

    default void displayTooltip(PoseStack matrix, Component component, int x, int y) {
        this.displayTooltips(matrix, Collections.singletonList(component), x, y);
    }

    default void displayTooltips(PoseStack matrix, List<Component> components, int xAxis, int yAxis) {
        displayTooltips(matrix, components, xAxis, yAxis, -1);
    }

    default void displayTooltips(PoseStack matrix, List<Component> components, int xAxis, int yAxis, int maxWidth) {
        //TODO - 1.18: Re-evaluate some form of this that wraps further along for use in Gui Windows (such as viewing descriptions of supported upgrades)
        //TODO - 1.18: Fix this I think it may just be a normal vanilla tooltip call that is used now
        //net.minecraftforge.client.gui.GuiUtils.drawHoveringText(matrix, components, xAxis, yAxis, getWidth(), getHeight(), maxWidth, getFont());
        if (this instanceof Screen screen) {
            screen.renderComponentTooltip(matrix, components, xAxis, yAxis);
        }
        //TODO - 1.18: Else??
    }

    default int getLeft() {
        if (this instanceof AbstractContainerScreen screen) {
            return screen.getGuiLeft();
        }
        return 0;
    }

    default int getTop() {
        if (this instanceof AbstractContainerScreen screen) {
            return screen.getGuiTop();
        }
        return 0;
    }

    default int getWidth() {
        if (this instanceof AbstractContainerScreen screen) {
            return screen.getXSize();
        }
        return 0;
    }

    default int getHeight() {
        if (this instanceof AbstractContainerScreen screen) {
            return screen.getYSize();
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
    Font getFont();

    default void renderItem(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis) {
        renderItem(matrix, stack, xAxis, yAxis, 1);
    }

    default void renderItem(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, float scale) {
        GuiUtils.renderItem(matrix, getItemRenderer(), stack, xAxis, yAxis, scale, getFont(), null, false);
    }

    ItemRenderer getItemRenderer();

    default void renderItemTooltip(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis) {
        Mekanism.logger.error("Tried to call 'renderItemTooltip' but unsupported in {}", getClass().getName());
    }

    default void renderItemTooltipWithExtra(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
        if (toAppend.isEmpty()) {
            renderItemTooltip(matrix, stack, xAxis, yAxis);
        } else {
            Mekanism.logger.error("Tried to call 'renderItemTooltipWithExtra' but unsupported in {}", getClass().getName());
        }
    }

    default void renderItemWithOverlay(PoseStack matrix, @Nonnull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
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