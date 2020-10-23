package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.entity.player.PlayerEntity;

public class JetpackSound extends PlayerSound {

    public JetpackSound(@Nonnull PlayerEntity player) {
        super(player, MekanismSounds.JETPACK);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return ClientTickHandler.isJetpackActive(player);
    }
}