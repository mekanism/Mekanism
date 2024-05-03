package mekanism.common.integration.lookingat;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LookingAtElement implements ILookingAtElement {

    private final int borderColor;
    private final int textColor;

    protected LookingAtElement(int borderColor, int textColor) {
        this.borderColor = borderColor;
        this.textColor = textColor;
    }

    public void render(@NotNull GuiGraphics guiGraphics, int x, int y) {
        int width = getWidth();
        int height = getHeight();
        guiGraphics.fill(x, y, x + width - 1, y + 1, borderColor);
        guiGraphics.fill(x, y, x + 1, y + height - 1, borderColor);
        guiGraphics.fill(x + width - 1, y, x + width, y + height - 1, borderColor);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        TextureAtlasSprite icon = getIcon();
        if (icon != null) {
            int scale = getScaledLevel(width - 2);
            if (scale > 0) {
                boolean colored = applyRenderColor(guiGraphics);
                GuiUtils.drawTiledSprite(guiGraphics, x + 1, y + 1, height - 2, scale, height - 2, icon,
                      16, 16, 0, TilingDirection.DOWN_RIGHT);
                if (colored) {
                    MekanismRenderer.resetColor(guiGraphics);
                }
            }
        }
        renderScaledText(Minecraft.getInstance(), guiGraphics, x + 4, y + 3, textColor, width - 8, getText());
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

    protected boolean applyRenderColor(GuiGraphics guiGraphics) {
        return false;
    }

    public static void renderScaledText(Minecraft mc, @NotNull GuiGraphics guiGraphics, float x, float y, int color, float maxWidth, Component component) {
        int length = mc.font.width(component);
        if (length <= maxWidth) {
            GuiUtils.drawString(guiGraphics, mc.font, component, x, y, color, false);
        } else {
            float scale = maxWidth / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.scale(scale, scale, scale);
            guiGraphics.drawString(mc.font, component, (int) (x * reverse), (int) ((y * reverse) + yAdd), color, false);
            pose.popPose();
        }
    }
}