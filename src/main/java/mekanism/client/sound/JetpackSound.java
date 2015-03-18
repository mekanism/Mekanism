package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemJetpack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class JetpackSound extends PlayerSound
{
	public JetpackSound(EntityPlayer player)
	{
		super(player, new ResourceLocation("mekanism", "item.jetpack"));
		
		setFadeIn(10);
		setFadeOut(5);
	}

	@Override
	public boolean isDonePlaying()
	{
		return donePlaying;
	}

	@Override
	public boolean shouldPlaySound()
	{
		return hasJetpack(player) && ClientTickHandler.isJetpackOn(player);
	}

	private boolean hasJetpack(EntityPlayer player)
	{
		return player.inventory.armorInventory[2] != null && player.inventory.armorInventory[2].getItem() instanceof ItemJetpack;
	}
}
