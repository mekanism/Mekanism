package mekanism.api;

import mekanism.common.Mekanism;
import net.minecraft.src.*;

public class ItemMachineUpgrade extends Item
{
	public ItemMachineUpgrade(int id)
	{
		super(id);
		setMaxStackSize(1);
		setCreativeTab(Mekanism.tabMekanism);
	}
}
