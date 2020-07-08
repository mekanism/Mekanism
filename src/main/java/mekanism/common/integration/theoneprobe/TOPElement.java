package mekanism.common.integration.theoneprobe;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcjty.theoneprobe.api.IElement;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public abstract class TOPElement implements IElement {

    private final int borderColor;
    private final int textColor;

    protected TOPElement(int borderColor, int textColor) {
        this.borderColor = borderColor;
        this.textColor = textColor;
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, int x, int y) {
        int width = getWidth();
        int height = getHeight();
        AbstractGui.func_238467_a_(matrix, x, y, x + width - 1, y + 1, borderColor);
        AbstractGui.func_238467_a_(matrix, x, y, x + 1, y + height - 1, borderColor);
        AbstractGui.func_238467_a_(matrix, x + width - 1, y, x + width, y + height - 1, borderColor);
        AbstractGui.func_238467_a_(matrix, x, y + height - 1, x + width, y + height, borderColor);
        TextureAtlasSprite icon = getIcon();
        if (icon != null) {
            int scale = getScaledLevel(width - 2);
            if (scale > 0) {
                boolean colored = applyRenderColor();
                GuiUtils.drawTiledSprite(matrix, x + 1, y + 1, height - 2, scale, height - 2, icon, 16, 16, 0);
                if (colored) {
                    MekanismRenderer.resetColor();
                }
            }
        }
        renderScaledText(Minecraft.getInstance(), matrix, x + 4, y + 3, textColor, getWidth() - 8, getText());
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 13;
    }

    public abstract int getScaledLevel(int level);

    @Nullable
    public abstract TextureAtlasSprite getIcon();

    public abstract ITextComponent getText();

    protected boolean applyRenderColor() {
        return false;
    }

    protected static void renderScaledText(Minecraft mc, @Nonnull MatrixStack matrix, int x, int y, int color, int maxWidth, ITextComponent component) {
        int length = mc.fontRenderer.func_238414_a_(component);
        if (length <= maxWidth) {
            mc.fontRenderer.func_238422_b_(matrix, component, x, y, color);
        } else {
            float scale = (float) maxWidth / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            matrix.push();
            matrix.scale(scale, scale, scale);
            mc.fontRenderer.func_238422_b_(matrix, component, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            matrix.pop();
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }
}