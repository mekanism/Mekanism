package mekanism.common.base;

import mekanism.common.Tier.BaseTier;
import net.minecraft.item.ItemStack;

public interface ITierItem 
{
	public BaseTier getBaseTier(ItemStack stack);
	
	public void setBaseTier(ItemStack stack, BaseTier tier);
}
