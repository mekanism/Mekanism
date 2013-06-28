package mekanism.common;


public class ItemMachineUpgrade extends ItemMekanism
{
	public ItemMachineUpgrade(int id, int energyBoost, int tickReduction)
	{
		super(id);
		setMaxStackSize(8);
		setCreativeTab(Mekanism.tabMekanism);
	}
}
