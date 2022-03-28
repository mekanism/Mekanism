package mekanism.client.gui.element.button;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.Color;
import mekanism.common.tile.component.config.DataType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.Nullable;

public class ColorToggleButton extends MekanismButton {

    private final BooleanSupplier toggled;
    private final EnumColor color;

    public ColorToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, Runnable onLeftClick, @Nullable GuiElement.IHoverable onHover) {
        this(gui, x, y, size, EnumColor.DARK_BLUE, toggled, onLeftClick, onHover);
    }

    public ColorToggleButton(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, Runnable onLeftClick, @Nullable GuiElement.IHoverable onHover) {
        super(gui, x, y, size, size, TextComponent.EMPTY, onLeftClick, onHover);
        this.toggled = toggled;
        this.color = color;
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (toggled.getAsBoolean()) {
            Color c = Color.rgbi(color.getRgbCode()[0], color.getRgbCode()[1], color.getRgbCode()[2]);
            double[] hsv = c.hsvArray();
            hsv[1] = Math.max(0, hsv[1] - 0.25F);
            hsv[2] = Math.min(1, hsv[2] + 0.4F);
            MekanismRenderer.color(Color.hsv(hsv[0], hsv[1], hsv[2]));
        } else {
            MekanismRenderer.resetColor();
        }
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (toggled.getAsBoolean()) {
            MekanismRenderer.resetColor();
        }
    }

    @Override
    protected boolean resetColorBeforeRender() {
        return false;
    }
}
