package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public class GuiConfirmationDialog extends GuiOverlayDialog {

    private ITextComponent title;

    public GuiConfirmationDialog(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent title, Runnable onConfirm, DialogType type) {
       super(gui, x, y, width, height);
       this.title = title;
       active = true;

       addChild(new TranslationButton(gui, gui.getLeft() + x + width / 2 - 102 / 2, gui.getTop() + y + height - 24, 50, 18, MekanismLang.BUTTON_CANCEL, () -> {
           gui.removeElement(this);
       }));
       addChild(new TranslationButton(gui, gui.getLeft() + x + width / 2 + 1, gui.getTop() + y + height - 24, 50, 18, MekanismLang.BUTTON_CONFIRM, () -> {
           onConfirm.run();
           gui.removeElement(this);
       }, null, type.getColorSupplier()));
    }

    public static void show(IGuiWrapper gui, ITextComponent title, Runnable onConfirm, DialogType type) {
        gui.addElement(new GuiConfirmationDialog(gui, gui.getWidth() / 2 - 140 / 2, gui.getHeight() / 2 - 64 / 2, 140, 64, title, onConfirm, type));
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);
        drawWrappedCenteredText(title.getString(), relativeX + (width / 2), relativeY + 10, titleTextColor(), width - 10);
    }

    public enum DialogType {
        NORMAL(null),
        DANGER(() -> EnumColor.RED);

        private Supplier<EnumColor> colorSupplier;

        private DialogType(Supplier<EnumColor> colorSupplier) {
            this.colorSupplier = colorSupplier;
        }

        public Supplier<EnumColor> getColorSupplier() {
            return colorSupplier;
        }
    }
}
