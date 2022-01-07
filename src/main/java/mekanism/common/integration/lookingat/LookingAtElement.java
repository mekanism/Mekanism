package mekanism.common.integration.lookingat;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

public abstract class LookingAtElement {

    private final int borderColor;
    private final int textColor;

    protected LookingAtElement(int borderColor, int textColor) {
        this.borderColor = borderColor;
        this.textColor = textColor;
    }

    public void render(@Nonnull PoseStack matrix, int x, int y) {
        int width = getWidth();
        int height = getHeight();
        GuiComponent.fill(matrix, x, y, x + width - 1, y + 1, borderColor);
        GuiComponent.fill(matrix, x, y, x + 1, y + height - 1, borderColor);
        GuiComponent.fill(matrix, x + width - 1, y, x + width, y + height - 1, borderColor);
        GuiComponent.fill(matrix, x, y + height - 1, x + width, y + height, borderColor);
        TextureAtlasSprite icon = getIcon();
        if (icon != null) {
            int scale = getScaledLevel(width - 2);
            if (scale > 0) {
                boolean colored = applyRenderColor();
                GuiUtils.drawTiledSprite(matrix, x + 1, y + 1, height - 2, scale, height - 2, icon,
                      16, 16, 0, TilingDirection.DOWN_RIGHT);
                if (colored) {
                    MekanismRenderer.resetColor();
                }
            }
        }
        renderScaledText(Minecraft.getInstance(), matrix, x + 4, y + 3, textColor, width - 8, getText());
    }

    public int getWidth() {
        return 100;
    }

    public int getHeight() {
        return 13;
    }

    public abstract int getScaledLevel(int level);

    @Nullable
    public abstract TextureAtlasSprite getIcon();

    public abstract Component getText();

    protected boolean applyRenderColor() {
        return false;
    }

    public static void renderScaledText(Minecraft mc, @Nonnull PoseStack matrix, float x, float y, int color, float maxWidth, Component component) {
        int length = mc.font.width(component);
        if (length <= maxWidth) {
            mc.font.draw(matrix, component, x, y, color);
        } else {
            float scale = maxWidth / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            matrix.pushPose();
            matrix.scale(scale, scale, scale);
            mc.font.draw(matrix, component, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            matrix.popPose();
        }
        //Make sure the color does not leak from having drawn the string
        MekanismRenderer.resetColor();
    }
}