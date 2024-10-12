package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicColorButton extends MekanismButton {

    public static BasicColorButton toggle(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, @NotNull IClickable onLeftClick) {
        return new BasicColorButton(gui, x, y, size, () -> toggled.getAsBoolean() ? color : null, onLeftClick, onLeftClick);
    }

    private final Supplier<EnumColor> colorSupplier;

    public BasicColorButton(IGuiWrapper gui, int x, int y, int size, Supplier<EnumColor> color, @NotNull IClickable onLeftClick, @Nullable IClickable onRightClick) {
        super(gui, x, y, size, size, CommonComponents.EMPTY, onLeftClick, onRightClick);
        this.colorSupplier = color;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        EnumColor color = getColor();
        if (color != null) {
            Color c = Color.rgb(color.getRgbCode());
            double[] hsv = c.hsvArray();
            hsv[1] = Math.max(0, hsv[1] - 0.1);
            hsv[2] = Math.min(1, hsv[2] + 0.1);
            MekanismRenderer.color(guiGraphics, Color.hsv(hsv[0], hsv[1], hsv[2]));
        } else {
            MekanismRenderer.resetColor(guiGraphics);
        }
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (color != null) {
            MekanismRenderer.resetColor(guiGraphics);
        }
    }

    @Override
    protected boolean resetColorBeforeRender() {
        return false;
    }

    public EnumColor getColor() {
        return this.colorSupplier.get();
    }
}
