package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslationButton extends MekanismButton {

    @Nullable
    private final Supplier<EnumColor> colorSupplier;

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, @NotNull IClickable onPress) {
        this(gui, x, y, width, height, translationHelper, onPress, null);
    }

    public TranslationButton(IGuiWrapper gui, int x, int y, int width, int height, ILangEntry translationHelper, @NotNull IClickable onPress,
          @Nullable Supplier<EnumColor> colorSupplier) {
        super(gui, x, y, width, height, translationHelper.translate(), onPress);
        this.colorSupplier = colorSupplier;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (colorSupplier == null) {
            MekanismRenderer.resetColor(guiGraphics);
            super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
            return;
        }
        MekanismRenderer.color(guiGraphics, colorSupplier.get());
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        MekanismRenderer.resetColor(guiGraphics);
    }

    @Override
    protected boolean resetColorBeforeRender() {
        return false;
    }
}