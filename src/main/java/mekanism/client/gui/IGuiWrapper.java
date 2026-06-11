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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface IGuiWrapper extends ContainerEventHandler, IFancyFontRenderer {

    default ItemStack getCarriedItem() {
        return ItemStack.EMPTY;
    }

    default Level getLevel() {
        return Minecraft.getInstance().level;
    }

    default RegistryAccess registryAccess() {
        return getLevel().registryAccess();
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

    default BooleanSupplier trackWarning(WarningType type, BooleanSupplier warningSupplier) {
        Mekanism.logger.error("Tried to call 'trackWarning' but unsupported in {}", getClass().getName());
        return warningSupplier;
    }

    default void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int xAxis, int yAxis) {
        renderItem(guiGraphics, stack, xAxis, yAxis, 1);
    }

    default void renderItem(GuiGraphicsExtractor guiGraphics, ItemStack stack, int xAxis, int yAxis, float scale) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), null, false);
    }

    default void renderItemTooltipWithExtra(GuiGraphicsExtractor guiGraphics, ItemStack stack, int xAxis, int yAxis, List<Component> toAppend) {
        if (toAppend.isEmpty()) {
            guiGraphics.setTooltipForNextFrame(font(), stack, xAxis, yAxis);
        } else {
            List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
            tooltip.addAll(toAppend);
            guiGraphics.setTooltipForNextFrame(font(), tooltip, stack.getTooltipImage(), stack, xAxis, yAxis);
        }
    }

    default void renderItemWithOverlay(GuiGraphicsExtractor guiGraphics, ItemStack stack, int xAxis, int yAxis, float scale, @Nullable String text) {
        GuiUtils.renderItem(guiGraphics, stack, xAxis, yAxis, scale, font(), text, true);
    }

    default void setSelectedWindow(SelectedWindowData selectedWindow) {
        Mekanism.logger.error("Tried to call 'setSelectedWindow' but unsupported in {}", getClass().getName());
    }
}