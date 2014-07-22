package mekanism.common.item;

import mekanism.common.IUpgradeItem;
import mekanism.common.Upgrade;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends ItemMekanism implements IUpgradeItem
{
	private Upgrade upgrade;
	
	public ItemUpgrade(Upgrade type)
	{
		upgrade = type;
		
		setMaxStackSize(type.getMax());
	}
	
	@Override
	public Upgrade getUpgradeType(ItemStack stack) 
	{
		return upgrade;
	}
}
