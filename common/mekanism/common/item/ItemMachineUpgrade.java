package mekanism.common.item;

import mekanism.common.Mekanism;


public class ItemMachineUpgrade extends ItemMekanism
{
	public ItemMachineUpgrade(int id, int energyBoost, int tickReduction)
	{
		super(id);
		setMaxStackSize(8);
		setCreativeTab(Mekanism.tabMekanism);
	}
}
