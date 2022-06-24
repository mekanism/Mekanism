package mekanism.client.sound;

import java.util.Objects;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GeigerSound extends PlayerSound {

    public static GeigerSound create(@NotNull Player player, RadiationScale scale) {
        if (scale == RadiationScale.NONE) {
            throw new IllegalArgumentException("Can't create a GeigerSound with a RadiationScale of NONE.");
        }
        int subtitleFrequency;
        if (scale == RadiationScale.MEDIUM) {
            subtitleFrequency = 50;
        } else if (scale == RadiationScale.ELEVATED) {
            subtitleFrequency = 40;
        } else if (scale == RadiationScale.HIGH) {
            subtitleFrequency = 30;
        } else if (scale == RadiationScale.EXTREME) {
            subtitleFrequency = 20;//Every second
        } else {//LOW
            subtitleFrequency = 60;
        }
        return new GeigerSound(player, scale, subtitleFrequency);
    }

    private final RadiationScale scale;

    private GeigerSound(@NotNull Player player, RadiationScale scale, int subtitleFrequency) {
        super(player, Objects.requireNonNull(scale.getSoundEvent()), subtitleFrequency);
        this.scale = scale;
        setFade(1, 1);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        return scale == RadiationManager.INSTANCE.getClientScale();
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.05F;
    }
}