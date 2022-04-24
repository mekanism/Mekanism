package mekanism.client.gui.element.gauge;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;

public enum GaugeOverlay {
    SMALL(16, 28, "small.png"),
    SMALL_MED(16, 46, "small_med.png"),
    STANDARD(16, 58, "standard.png"),
    MEDIUM(32, 58, "medium.png"),
    WIDE(64, 48, "wide.png");

    private final int width;
    private final int height;
    private final ResourceLocation barOverlay;

    GaugeOverlay(int width, int height, String barOverlay) {
        this.width = width;
        this.height = height;
        this.barOverlay = MekanismUtils.getResource(ResourceType.GUI_GAUGE, barOverlay);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ResourceLocation getBarOverlay() {
        return barOverlay;
    }
}