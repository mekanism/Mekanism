package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.lib.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.PlayerEntity;

public class GeigerSound extends PlayerSound {

    private RadiationScale scale;

    private GeigerSound(@Nonnull PlayerEntity player, RadiationScale scale) {
        super(player, scale.getSoundEvent());
        this.scale = scale;
        setFade(1, 1);
    }

    public static GeigerSound create(@Nonnull PlayerEntity player, RadiationScale scale) {
        if (scale == RadiationScale.NONE) {
            Mekanism.logger.error("Can't create a GeigerSound with a RadiationScale of NONE.");
            return null;
        }
        return new GeigerSound(player, scale);
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
