package net.uberkat.obsidian.common;

import net.minecraft.src.*;
import obsidian.api.ItemMachineUpgrade;

public class SlotMachineUpgrade extends Slot
{
	public SlotMachineUpgrade(IInventory inventory, int x, int y, int z)
	{
		super(inventory, x, y, z);
	}
	
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof ItemMachineUpgrade;
	}
}
