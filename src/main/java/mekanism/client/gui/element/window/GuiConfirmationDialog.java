package mekanism.client.gui.element.window;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class GuiConfirmationDialog extends GuiWindow {

    private static final int PADDING = 5;

    private final WrappedTextRenderer wrappedTextRenderer;

    private GuiConfirmationDialog(IGuiWrapper gui, int x, int y, int width, int height, ReplaceableWrappedTextRenderer renderer, Runnable onConfirm, DialogType type) {
        super(gui, x, y, width, height, WindowType.CONFIRMATION);
        this.wrappedTextRenderer = renderer.replaceFont(this);
        active = true;

        addChild(new TranslationButton(gui, relativeX + width / 2 - 51, relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CANCEL, this::close));
        addChild(new TranslationButton(gui, relativeX + width / 2 + 1, relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CONFIRM, (element, mouseX, mouseY) -> {
            onConfirm.run();
            return close(element, mouseX, mouseY);
        }, type.getColorSupplier()));
    }

    public static void show(IGuiWrapper gui, Component title, Runnable onConfirm, DialogType type) {
        int width = 140;
        ReplaceableWrappedTextRenderer renderer = new ReplaceableWrappedTextRenderer(gui, width, title);
        int height = 33 + renderer.getRequiredHeight(width - 2 * PADDING);
        gui.addWindow(new GuiConfirmationDialog(gui, (gui.getXSize() - width) / 2, (gui.getYSize() - height) / 2, width, height, renderer, onConfirm, type));
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        wrappedTextRenderer.render(guiGraphics, relativeX + PADDING, relativeY + 6, width - 2 * PADDING, TextAlignment.CENTER, titleTextColor());
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // only allow clicks here
        return true;
    }

    @Override
    protected boolean isFocusOverlay() {
        return true;
    }

    public enum DialogType {
        NORMAL(() -> null),
        DANGER(() -> EnumColor.RED);

        private final Supplier<EnumColor> colorSupplier;

        DialogType(Supplier<EnumColor> colorSupplier) {
            this.colorSupplier = colorSupplier;
        }

        public Supplier<EnumColor> getColorSupplier() {
            return colorSupplier;
        }
    }
}
