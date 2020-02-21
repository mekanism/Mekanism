package mekanism.client.gui.element.gauge;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public enum GaugeOverlay {
    STANDARD(16, 58, "standard.png"),
    WIDE(64, 48, "wide.png"),
    SMALL(16, 28, "small.png");

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

    //TODO: Test/figure out proper numbers for JEI
    public ResourceLocation getBarOverlay() {
        return barOverlay;
    }
}