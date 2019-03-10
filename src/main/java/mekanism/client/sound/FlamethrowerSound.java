package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FlamethrowerSound extends PlayerSound {

    private static ResourceLocation IDLE_SOUND = new ResourceLocation("mekanism", "item.flamethrower.idle");
    private static ResourceLocation ON_SOUND = new ResourceLocation("mekanism", "item.flamethrower.active");
    private static ResourceLocation OFF_SOUND = new ResourceLocation("mekanism", "item.flamethrower.active");

    private boolean inUse;

    public FlamethrowerSound(EntityPlayer player) {
        super(player, IDLE_SOUND);

        inUse = ClientTickHandler.isFlamethrowerOn(player);

        this.positionedSoundLocation = inUse ? ON_SOUND : OFF_SOUND;
    }

    @Override
    public boolean shouldPlaySound() {
        boolean hasFlamethrower = ClientTickHandler.hasFlamethrower(player);
        boolean isFlamethrowerOn = ClientTickHandler.isFlamethrowerOn(player);

        if (!hasFlamethrower) {
            return false;
        }

        if (inUse != isFlamethrowerOn) {
            inUse = isFlamethrowerOn;
            this.positionedSoundLocation = inUse ? ON_SOUND : OFF_SOUND;
        }
        return true;
    }

//	@Override
//	public float getVolume()
//	{
//		return super.getVolume() * (inUse ? 2 : 1);
//	}

}
