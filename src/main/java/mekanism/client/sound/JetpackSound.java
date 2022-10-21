package mekanism.client.sound;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class JetpackSound extends PlayerSound {

    public JetpackSound(@NotNull Player player) {
        super(player, MekanismSounds.JETPACK);
    }

    @Override
    public boolean shouldPlaySound(@NotNull Player player) {
        return Mekanism.playerState.isJetpackOn(player);
    }
}