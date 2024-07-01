package mekanism.client.render.lib;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class ScrollIncrementer {

    private final boolean discrete;
    private long lastScrollTime = -1;
    private double scrollDelta;

    public ScrollIncrementer(boolean discrete) {
        this.discrete = discrete;
    }

    public int scroll(double delta) {
        long time = Minecraft.getInstance().gui.getGuiTicks();
        if (time - lastScrollTime > SharedConstants.TICKS_PER_SECOND) {
            scrollDelta = 0;
        }
        lastScrollTime = time;
        scrollDelta += delta;
        int shift = (int) scrollDelta;
        scrollDelta %= 1;
        if (discrete) {
            shift = Mth.clamp(shift, -1, 1);
        }
        return shift;
    }
}