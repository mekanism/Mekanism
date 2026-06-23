package mekanism.client.gui.element.gauge;

import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public enum GaugeOverlay {
    SMALL(16, 28, "small"),
    SMALL_MED(16, 46, "small_med"),
    STANDARD(16, 58, "standard"),
    MEDIUM(32, 58, "medium"),
    WIDE(64, 48, "wide");

    private final int width;
    private final int height;
    private final Identifier barOverlay;

    GaugeOverlay(int width, int height, String barOverlay) {
        this.width = width;
        this.height = height;
        this.barOverlay = Mekanism.rl("gauge/overlay/" + barOverlay);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Identifier getBarOverlay() {
        return barOverlay;
    }
}