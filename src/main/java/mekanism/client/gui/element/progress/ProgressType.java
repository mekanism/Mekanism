package mekanism.client.gui.element.progress;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public enum ProgressType {
    BAR(25, 9, false, "bar.png"),
    LARGE_RIGHT(48, 8, false, "large_right.png"),
    LARGE_LEFT(48, 8, false, "large_left.png"),
    TALL_RIGHT(20, 15, false, "tall_right.png"),
    RIGHT(32, 8, false, "right.png"),
    SMALL_RIGHT(28, 8, false, "small_right.png"),
    SMALL_LEFT(28, 8, false, "small_left.png"),
    BI(16, 6, false, "bidirectional.png"),
    FLAME(13, 13, true, "flame.png"),
    INSTALLING(10, 14, true, "installing.png"),
    DOWN(8, 20, true, "down.png");

    private final int width;
    private final int height;
    private final int textureWidth;
    private final int textureHeight;
    private final int overlayX;
    private final int overlayY;
    private final ResourceLocation texture;
    private final boolean vertical;

    ProgressType(int width, int height, boolean vertical, String texture) {
        this.width = width;
        this.height = height;
        if (vertical) {
            this.textureWidth = width * 2;
            this.textureHeight = height;
            this.overlayX = width;
            this.overlayY = 0;
        } else {
            this.textureWidth = width;
            this.textureHeight = height * 2;
            this.overlayX = 0;
            this.overlayY = height;
        }
        this.vertical = vertical;
        this.texture = MekanismUtils.getResource(ResourceType.GUI_PROGRESS, texture);
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public int getOverlayX() {
        return overlayX;
    }

    public int getOverlayY() {
        return overlayY;
    }

    public boolean isVertical() {
        return vertical;
    }
}