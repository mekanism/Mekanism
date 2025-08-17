package mekanism.client.sound;

import java.util.Objects;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.RadiationScale;
import mekanism.common.util.MekanismUtils;
import net.minecraft.SharedConstants;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GeigerSound extends PlayerSound {

    public static GeigerSound create(@NotNull Player player, RadiationScale scale) {
        int subtitleFrequency = switch (scale) {
            case LOW -> 3 * SharedConstants.TICKS_PER_SECOND;
            case MEDIUM -> 2 * SharedConstants.TICKS_PER_SECOND + MekanismUtils.TICKS_PER_HALF_SECOND;
            case ELEVATED -> 2 * SharedConstants.TICKS_PER_SECOND;
            case HIGH -> SharedConstants.TICKS_PER_SECOND + MekanismUtils.TICKS_PER_HALF_SECOND;
            case EXTREME -> SharedConstants.TICKS_PER_SECOND;
            case NONE -> throw new IllegalArgumentException("Can't create a GeigerSound with a RadiationScale of NONE.");
        };
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
        return scale == ClientRadiation.getClientScale();
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.05F;
    }
}