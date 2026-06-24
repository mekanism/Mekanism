package mekanism.client.gui.element.progress;

import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public enum ProgressType {
    BAR(25, 9, false, "bar"),
    LARGE_RIGHT(48, 8, false, "large_right"),
    LARGE_LEFT(48, 8, false, "large_left"),
    TALL_RIGHT(20, 15, false, "tall_right"),
    RIGHT(32, 8, false, "right"),
    SMALL_RIGHT(28, 8, false, "small_right"),
    SMALL_LEFT(28, 8, false, "small_left"),
    BI(16, 6, false, "bidirectional"),
    FLAME(13, 13, true, false, "flame"),
    INSTALLING(10, 14, true, "installing"),
    UNINSTALLING(12, 12, true, "uninstalling"),
    DOWN(8, 20, true, "down");

    private final int width;
    private final int height;
    private final Identifier emptyTexture;
    private final Identifier fullTexture;
    @Nullable
    private final Identifier warningTexture;
    private final boolean vertical;

    ProgressType(int width, int height, boolean vertical, String texture) {
        this(width, height, vertical, true, texture);
    }

    ProgressType(int width, int height, boolean vertical, boolean hasWarning, String texture) {
        this.width = width;
        this.height = height;
        this.vertical = vertical;
        Identifier baseTexture = Mekanism.rl("progress/" + texture + "/");
        this.emptyTexture = baseTexture.withSuffix("empty");
        this.fullTexture = baseTexture.withSuffix("full");
        this.warningTexture = hasWarning ? baseTexture.withSuffix("warning") : null;
    }

    public Identifier emptyTexture() {
        return emptyTexture;
    }

    public Identifier texture(boolean warning) {
        if (warning && warningTexture != null) {
            return warningTexture;
        }
        return fullTexture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVertical() {
        return vertical;
    }

    /// Keeps track of if it is going in the "opposite" direction as to normal for how it fills up
    public boolean isReverse() {
        return this == SMALL_LEFT || this == LARGE_LEFT || this == FLAME;
    }
}