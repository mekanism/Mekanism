package mekanism.client.sound;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class JetpackSound extends PlayerSound {

    public JetpackSound(Player player) {
        super(player, MekanismSounds.JETPACK);
    }

    @Override
    public boolean shouldPlaySound(Player player) {
        return Mekanism.playerState.isJetpackOn(player);
    }
}