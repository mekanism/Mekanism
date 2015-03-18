package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemGasMask;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GasMaskSound extends PlayerSound
{
	public GasMaskSound(EntityPlayer player)
	{
		super(player, new ResourceLocation("mekanism", "item.gasMask"));
		
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
		return hasGasMask(player) && ClientTickHandler.isGasMaskOn(player);
	}

	private boolean hasGasMask(EntityPlayer player)
	{
		return player.inventory.armorInventory[3] != null && player.inventory.armorInventory[3].getItem() instanceof ItemGasMask;
	}
}
