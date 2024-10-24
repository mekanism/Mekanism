package mekanism.common.integration.lookingat;

import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.render.IFancyFontRenderer;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LookingAtElement implements ILookingAtElement, IFancyFontRenderer {

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
        drawScrollingString(guiGraphics, getText(), x, y + 3, TextAlignment.LEFT, textColor, 4, false);
    }

    public int getWidth() {
        return 100;
    }

    @Override
    public int getXSize() {
        return getWidth();
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
}