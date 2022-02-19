package mekanism.client.gui.element.window;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.SelectedWindowData.WindowType;
import net.minecraft.util.text.ITextComponent;

public class GuiConfirmationDialog extends GuiWindow {

    private final WrappedTextRenderer wrappedTextRenderer;

    private GuiConfirmationDialog(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent title, Runnable onConfirm, DialogType type) {
        super(gui, x, y, width, height, WindowType.CONFIRMATION);
        this.wrappedTextRenderer = new WrappedTextRenderer(this, title);
        active = true;

        addChild(new TranslationButton(gui, relativeX + width / 2 - 51, relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CANCEL, this::close));
        addChild(new TranslationButton(gui, relativeX + width / 2 + 1, relativeY + height - 24, 50, 18, MekanismLang.BUTTON_CONFIRM, () -> {
            onConfirm.run();
            close();
        }, null, type.getColorSupplier()));
    }

    public static void show(IGuiWrapper gui, ITextComponent title, Runnable onConfirm, DialogType type) {
        int width = 140;
        int height = 33 + WrappedTextRenderer.calculateHeightRequired(gui.getFont(), title, width, width - 10);
        gui.addWindow(new GuiConfirmationDialog(gui, (gui.getWidth() - width) / 2, (gui.getHeight() - height) / 2, width, height, title, onConfirm, type));
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        wrappedTextRenderer.renderCentered(matrix, relativeX + (width / 2F), relativeY + 6, titleTextColor(), width - 10);
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
