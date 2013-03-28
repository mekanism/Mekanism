package mekanism.common;

import universalelectricity.prefab.modifier.IModifier;

import net.minecraft.item.ItemStack;

public class ItemMachineUpgrade extends ItemMekanism implements IModifier
{
	public ItemMachineUpgrade(int id, int energyBoost, int tickReduction)
	{
		super(id);
		setMaxStackSize(8);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public String getName(ItemStack itemstack)
	{
		return itemID == Mekanism.SpeedUpgrade.itemID ? "Speed" : "Capacity";
	}

	@Override
	public int getEffectiveness(ItemStack itemstack)
	{
		return itemID == Mekanism.SpeedUpgrade.itemID ? 150 : 1000;
	}
}
