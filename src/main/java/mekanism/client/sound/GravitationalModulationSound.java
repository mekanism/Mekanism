package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.entity.player.PlayerEntity;

public class GravitationalModulationSound extends PlayerSound {

    public GravitationalModulationSound(@Nonnull PlayerEntity player) {
        super(player, MekanismSounds.GRAVITATIONAL_MODULATION_UNIT);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return ClientTickHandler.isGravitationalModulationOn(player);
    }
}
