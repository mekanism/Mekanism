package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.radiation.RadiationManager.RadiationScale;
import net.minecraft.entity.player.PlayerEntity;

public class GeigerSound extends PlayerSound {

    private RadiationScale scale;

    public GeigerSound(@Nonnull PlayerEntity player, RadiationScale scale) {
        super(player, scale.getSoundEvent());
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
