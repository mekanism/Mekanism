package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class ScubaMaskSound extends PlayerSound {

    public ScubaMaskSound(Player player) {
        super(player, MekanismSounds.SCUBA_MASK);
    }

    @Override
    public boolean shouldPlaySound(Player player) {
        return ClientTickHandler.isScubaMaskOn(player);
    }
}