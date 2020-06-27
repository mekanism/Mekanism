package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.common.MekanismLang;
import net.minecraft.util.text.ITextComponent;

public class GuiConfirmationDialog extends GuiWindow {

    private final ITextComponent title;

    public GuiConfirmationDialog(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent title, Runnable onConfirm, DialogType type) {
        super(gui, x, y, width, height);
        this.title = title;
        field_230693_o_ = true;

        addChild(new TranslationButton(gui, this.field_230690_l_ + width / 2 - 102 / 2, this.field_230691_m_ + height - 24, 50, 18, MekanismLang.BUTTON_CANCEL, this::close));
        addChild(new TranslationButton(gui, this.field_230690_l_ + width / 2 + 1, this.field_230691_m_ + height - 24, 50, 18, MekanismLang.BUTTON_CONFIRM, () -> {
            onConfirm.run();
            close();
        }, null, type.getColorSupplier()));
    }

    public static void show(IGuiWrapper gui, ITextComponent title, Runnable onConfirm, DialogType type) {
        gui.addWindow(new GuiConfirmationDialog(gui, gui.getWidth() / 2 - 140 / 2, gui.getHeight() / 2 - 64 / 2, 140, 64, title, onConfirm, type));
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawWrappedCenteredText(matrix, title.getString(), relativeX + (field_230688_j_ / 2), relativeY + 10, titleTextColor(), field_230688_j_ - 10);
    }

    @Override
    public boolean func_231047_b_(double mouseX, double mouseY) {
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
