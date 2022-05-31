package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.world.entity.player.Player;

public class JetpackSound extends PlayerSound {

    public JetpackSound(@Nonnull Player player) {
        super(player, MekanismSounds.JETPACK);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull Player player) {
        return ClientTickHandler.isJetpackOn(player);
    }
}