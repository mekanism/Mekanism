package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GravitationalModulationSound extends PlayerSound {

    public GravitationalModulationSound(@NotNull Player player) {
        super(player, MekanismSounds.GRAVITATIONAL_MODULATION_UNIT);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        return ClientTickHandler.isGravitationalModulationOn(player);
    }
}