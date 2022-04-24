package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class ScubaMaskSound extends PlayerSound {

    public ScubaMaskSound(@Nonnull Player player) {
        super(player, MekanismSounds.SCUBA_MASK);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull Player player) {
        return ClientTickHandler.isScubaMaskOn(player);
    }
}