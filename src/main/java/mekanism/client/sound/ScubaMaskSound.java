package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class ScubaMaskSound extends PlayerSound {

    public ScubaMaskSound(@NotNull Player player) {
        super(player, MekanismSounds.SCUBA_MASK);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        return ClientTickHandler.isScubaMaskOn(player);
    }
}