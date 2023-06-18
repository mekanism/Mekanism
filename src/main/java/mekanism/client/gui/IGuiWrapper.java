package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGuiWrapper {

    default void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, Component... components) {
        this.displayTooltips(guiGraphics, mouseX, mouseY, List.of(components));
    }

    default void displayTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, List<Component> components) {
        guiGraphics.renderComponentTooltip(getFont(), components, mouseX, mouseY);
    }

    @NotNull
    default ItemStack getCarriedItem() {
        if (this instanceof AbstractContainerScreen<?> screen) {
            return screen.getMenu().getCarried();
        }
        //General fallback to just get it from the player's container menu
        Player player = Minecraft.getInstance().player;
        return player == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
    }

    default int getLeft() {
        if (this instanceof AbstractContainerScreen<?> screen) {
            return screen.getGuiLeft();
        }
        return 0;
    }

    default int getTop() {
        if (this instanceof AbstractContainerScreen<?> screen) {
            return screen.getGuiTop();
        }
        return 0;
    }

    default int getWidth() {
        if (this instanceof AbstractContainerScreen<?> screen) {
            return screen.getXSize();
        }
        return 0;
    }

    default int getHeight() {
        if (this instanceof AbstractContainerScreen<?> screen) {
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

    @NotNull
    default BooleanSupplier trackWarning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        Mekanism.logger.error("Tried to call 'trackWarning' but unsupported in {}", getClass().getName());
        return warningSupplier;
    }

    Font getFont();

    default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
        renderItem(guiGraphics, stack, xAxis, yAxis, 1);
    }

    default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, getFont(), null, false);
    }

    default void renderItemTooltip(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
        //Mekanism.logger.error("Tried to call 'renderItemTooltip' but unsupported in {}", getClass().getName());
        //renderTooltip(guiGraphics, stack, xAxis, yAxis);
        //TODO - 1.20: Re-evaluate that this doesn't need any form of repositioning y value wise
        guiGraphics.renderTooltip(getFont(), stack, xAxis, yAxis);
    }

    default void renderItemTooltipWithExtra(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
        if (toAppend.isEmpty()) {
            renderItemTooltip(guiGraphics, stack, xAxis, yAxis);
        } else {
            //Mekanism.logger.error("Tried to call 'renderItemTooltipWithExtra' but unsupported in {}", getClass().getName());
            List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
            tooltip.addAll(toAppend);
            //renderTooltip(guiGraphics, tooltip, stack.getTooltipImage(), xAxis, yAxis, stack);
            //TODO - 1.20: Re-evaluate that this doesn't need any form of repositioning y value wise
            guiGraphics.renderTooltip(getFont(), tooltip, stack.getTooltipImage(), stack, xAxis, yAxis);
        }
    }

    default void renderItemWithOverlay(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, getFont(), text, true);
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