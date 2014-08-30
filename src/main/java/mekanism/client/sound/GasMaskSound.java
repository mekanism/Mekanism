package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemGasMask;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GasMaskSound extends PlayerSound
{
	public GasMaskSound(EntityPlayer player)
	{
		super(player, new ResourceLocation("mekanism", "item.gasMask"));
		setFadeIn(30);
		setFadeOut(10);
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
