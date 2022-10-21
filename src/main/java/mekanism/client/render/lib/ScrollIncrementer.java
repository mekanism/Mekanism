package mekanism.client.render.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public class ScrollIncrementer {

    private final boolean discrete;
    private long lastScrollTime = -1;
    private double scrollDelta;

    public ScrollIncrementer(boolean discrete) {
        this.discrete = discrete;
    }

    private long getTime() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? - 1 : level.getGameTime();
    }

    public int scroll(double delta) {
        long time = getTime();
        if (time - lastScrollTime > 20) {
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