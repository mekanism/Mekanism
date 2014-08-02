package mekanism.common;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem 
{
	public Upgrade getUpgradeType(ItemStack stack);
}
