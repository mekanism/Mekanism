package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class JetpackSound extends PlayerSound {

    private static final ResourceLocation SOUND = Mekanism.rl("item.jetpack");

    public JetpackSound(@Nonnull PlayerEntity player) {
        super(player, SOUND);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return ClientTickHandler.isJetpackActive(player);
    }
}