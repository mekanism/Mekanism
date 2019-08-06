package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlamethrowerSound extends PlayerSound {

    private static final ResourceLocation IDLE_SOUND = new ResourceLocation(Mekanism.MODID, "item.flamethrower.idle");
    private static final ResourceLocation ON_SOUND = new ResourceLocation(Mekanism.MODID, "item.flamethrower.active");

    private boolean active;

    private FlamethrowerSound(@Nonnull PlayerEntity player, boolean active) {
        super(player, active ? ON_SOUND : IDLE_SOUND);
        this.active = active;
    }

    @Override
    public boolean shouldPlaySound(@Nonnull PlayerEntity player) {
        if (!ClientTickHandler.hasFlamethrower(player)) {
            return false;
        }
        return ClientTickHandler.isFlamethrowerOn(player) == active;
    }

    public static class Active extends FlamethrowerSound {

        public Active(@Nonnull PlayerEntity player) {
            super(player, true);
        }
    }

    public static class Idle extends FlamethrowerSound {

        public Idle(@Nonnull PlayerEntity player) {
            super(player, false);
        }
    }
}