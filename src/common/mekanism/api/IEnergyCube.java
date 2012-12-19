package mekanism.api;

import mekanism.api.Tier.EnergyCubeTier;
import net.minecraft.src.*;

public interface IEnergyCube 
{
	public EnergyCubeTier getTier(ItemStack itemstack);
	
	public void setTier(ItemStack itemstack, EnergyCubeTier tier);
}
