package mekanism.api;

import mekanism.api.Tier.EnergyCubeTier;
import net.minecraft.item.ItemStack;

public interface IEnergyCube 
{
	public EnergyCubeTier getTier(ItemStack itemstack);
	
	public void setTier(ItemStack itemstack, EnergyCubeTier tier);
}
