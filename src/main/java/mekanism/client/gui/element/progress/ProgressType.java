package mekanism.client.gui.element.progress;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public enum ProgressType {
    BAR(25, 9, false, "bar.png"),
    LARGE_RIGHT(48, 8, false, "large_right.png"),
    LARGE_LEFT(48, 8, false, "large_left.png"),
    TALL_RIGHT(20, 15, false, "tall_right.png"),
    RIGHT(32, 8, false, "right.png"),
    SMALL_RIGHT(28, 8, false, "small_right.png"),
    SMALL_LEFT(28, 8, false, "small_left.png"),
    BI(16, 6, false, "bidirectional.png"),
    FLAME(13, 13, true, false, "flame.png"),
    INSTALLING(10, 14, true, "installing.png"),
    UNINSTALLING(12, 12, true, "uninstalling.png"),
    DOWN(8, 20, true, "down.png");

    private final int width;
    private final int height;
    private final int textureWidth;
    private final int textureHeight;
    private final int overlayX;
    private final int overlayY;
    private final int warningOverlayX;
    private final int warningOverlayY;
    private final ResourceLocation texture;
    private final boolean vertical;

    ProgressType(int width, int height, boolean vertical, String texture) {
        this(width, height, vertical, true, texture);
    }

    ProgressType(int width, int height, boolean vertical, boolean hasWarning, String texture) {
        this.width = width;
        this.height = height;
        int dimensionMultiplier = hasWarning ? 3 : 2;
        if (vertical) {
            this.textureWidth = width * dimensionMultiplier;
            this.textureHeight = height;
            this.overlayX = width;
            this.warningOverlayX = hasWarning ? width * 2 : 0;
            this.warningOverlayY = this.overlayY = 0;
        } else {
            this.textureWidth = width;
            this.textureHeight = height * dimensionMultiplier;
            this.warningOverlayX = this.overlayX = 0;
            this.overlayY = height;
            this.warningOverlayY = hasWarning ? height * 2 : 0;
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

    public int getOverlayX(boolean isWarning) {
        return isWarning ? warningOverlayX : overlayX;
    }

    public int getOverlayY(boolean isWarning) {
        return isWarning ? warningOverlayY : overlayY;
    }

    public boolean isVertical() {
        return vertical;
    }

    /**
     * Keeps track of if it is going in the "opposite" direction as to normal for how it fills up
     */
    public boolean isReverse() {
        return this == SMALL_LEFT || this == LARGE_LEFT;
    }
}