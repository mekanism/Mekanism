package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JetpackSound extends PlayerSound {

    private static final ResourceLocation SOUND = new ResourceLocation(Mekanism.MODID, "item.jetpack");

    public JetpackSound(EntityPlayer player) {
        super(player, SOUND);
    }

    @Override
    public boolean shouldPlaySound() {
        return ClientTickHandler.isJetpackActive(player);
    }
}