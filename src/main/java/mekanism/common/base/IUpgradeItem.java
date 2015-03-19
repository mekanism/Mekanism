package mekanism.common.base;

import mekanism.common.Upgrade;

import net.minecraft.item.ItemStack;

public interface IUpgradeItem 
{
	public Upgrade getUpgradeType(ItemStack stack);
}
