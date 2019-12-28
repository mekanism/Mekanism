package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.entity.player.PlayerEntity;

public class GasMaskSound extends PlayerSound {

    public GasMaskSound(@Nonnull PlayerEntity player) {
        super(player, MekanismSounds.GAS_MASK.getSoundEvent());
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return ClientTickHandler.isGasMaskOn(player);
    }
}