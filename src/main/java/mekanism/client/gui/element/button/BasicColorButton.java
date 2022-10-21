package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicColorButton extends MekanismButton {

    public static BasicColorButton toggle(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, @Nullable Runnable onLeftClick,
          @Nullable GuiElement.IHoverable onHover) {
        return new BasicColorButton(gui, x, y, size, () -> toggled.getAsBoolean() ? color : null, onLeftClick, onLeftClick, onHover);
    }

    public static BasicColorButton renderActive(IGuiWrapper gui, int x, int y, int size, EnumColor color, @Nullable GuiElement.IHoverable onHover) {
        return new BasicColorButton(gui, x, y, size, () -> color, null, null, onHover);
    }

    private final Supplier<EnumColor> colorSupplier;

    public BasicColorButton(IGuiWrapper gui, int x, int y, int size, Supplier<EnumColor> color, @Nullable Runnable onLeftClick, @Nullable Runnable onRightClick,
          @Nullable GuiElement.IHoverable onHover) {
        super(gui, x, y, size, size, Component.empty(), onLeftClick, onRightClick, onHover);
        this.colorSupplier = color;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        EnumColor color = getColor();
        boolean doColor = color != null && color != EnumColor.GRAY;
        if (doColor) {
            Color c = Color.rgbi(color.getRgbCode()[0], color.getRgbCode()[1], color.getRgbCode()[2]);
            double[] hsv = c.hsvArray();
            hsv[1] = Math.max(0, hsv[1] - 0.25F);
            hsv[2] = Math.min(1, hsv[2] + 0.4F);
            MekanismRenderer.color(Color.hsv(hsv[0], hsv[1], hsv[2]));
        } else {
            MekanismRenderer.resetColor();
        }
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (doColor) {
            MekanismRenderer.resetColor();
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
