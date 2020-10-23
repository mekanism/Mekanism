package mekanism.client.sound;

import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.PlayerEntity;

public class GeigerSound extends PlayerSound {

    public static GeigerSound create(@Nonnull PlayerEntity player, RadiationScale scale) {
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

    private GeigerSound(@Nonnull PlayerEntity player, RadiationScale scale, int subtitleFrequency) {
        super(player, Objects.requireNonNull(scale.getSoundEvent()), subtitleFrequency);
        this.scale = scale;
        setFade(1, 1);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return scale == Mekanism.radiationManager.getClientScale();
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.05F;
    }
}