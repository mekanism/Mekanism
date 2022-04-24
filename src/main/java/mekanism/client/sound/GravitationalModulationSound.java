package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class GravitationalModulationSound extends PlayerSound {

    public GravitationalModulationSound(@Nonnull Player player) {
        super(player, MekanismSounds.GRAVITATIONAL_MODULATION_UNIT);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull Player player) {
        return ClientTickHandler.isGravitationalModulationOn(player);
    }
}
