package mekanism.client.sound;

import javax.annotation.Nonnull;
import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FlamethrowerSound extends PlayerSound {

    private static final ResourceLocation IDLE_SOUND = new ResourceLocation(Mekanism.MODID, "item.flamethrower.idle");
    private static final ResourceLocation ON_SOUND = new ResourceLocation(Mekanism.MODID, "item.flamethrower.active");

    private boolean active;

    private FlamethrowerSound(@Nonnull EntityPlayer player, boolean active) {
        super(player, active ? ON_SOUND : IDLE_SOUND);
        this.active = active;
    }

    @Override
    public boolean shouldPlaySound(@Nonnull EntityPlayer player) {
        if (!ClientTickHandler.hasFlamethrower(player)) {
            return false;
        }
        return ClientTickHandler.isFlamethrowerOn(player) == active;
    }

    public static class Active extends FlamethrowerSound {

        public Active(@Nonnull EntityPlayer player) {
            super(player, true);
        }
    }

    public static class Idle extends FlamethrowerSound {

        public Idle(@Nonnull EntityPlayer player) {
            super(player, false);
        }
    }
}