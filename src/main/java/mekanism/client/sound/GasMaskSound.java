package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class GasMaskSound extends PlayerSound {

    private static final ResourceLocation SOUND = Mekanism.rl("item.gasMask");

    public GasMaskSound(@Nonnull PlayerEntity player) {
        super(player, SOUND);
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        return ClientTickHandler.isGasMaskOn(player);
    }
}