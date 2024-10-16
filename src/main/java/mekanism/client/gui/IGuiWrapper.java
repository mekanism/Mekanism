package mekanism.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IGuiWrapper extends ContainerEventHandler, IFancyFontRenderer {

    @NotNull
    default ItemStack getCarriedItem() {
        return ItemStack.EMPTY;
    }

    int getGuiLeft();

    int getGuiTop();

    @Override
    int getXSize();

    int getYSize();

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
        return null;
    }

    @NotNull
    default BooleanSupplier trackWarning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        Mekanism.logger.error("Tried to call 'trackWarning' but unsupported in {}", getClass().getName());
        return warningSupplier;
    }

    default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis) {
        renderItem(guiGraphics, stack, xAxis, yAxis, 1);
    }

    default void renderItem(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), null, false);
    }

    default void renderItemTooltipWithExtra(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
        if (toAppend.isEmpty()) {
            guiGraphics.renderTooltip(font(), stack, xAxis, yAxis);
        } else {
            List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
            tooltip.addAll(toAppend);
            guiGraphics.renderTooltip(font(), tooltip, stack.getTooltipImage(), stack, xAxis, yAxis);
        }
    }

    default void renderItemWithOverlay(GuiGraphics guiGraphics, @NotNull ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), text, true);
    }

    default void setSelectedWindow(SelectedWindowData selectedWindow) {
        Mekanism.logger.error("Tried to call 'setSelectedWindow' but unsupported in {}", getClass().getName());
    }
}