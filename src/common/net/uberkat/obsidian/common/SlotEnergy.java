package net.uberkat.obsidian.common;

import universalelectricity.implement.IItemElectric;
import ic2.api.IElectricItem;
import net.minecraft.src.*;
import net.uberkat.obsidian.api.IEnergizedItem;

public class SlotEnergy extends Slot
{
	public SlotEnergy(IInventory inventory, int x, int y, int z)
	{
		super(inventory, x, y, z);
	}
	
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof IEnergizedItem || itemstack.getItem() instanceof IElectricItem || itemstack.getItem() instanceof IItemElectric || itemstack.itemID == Item.redstone.shiftedIndex;
	}
}
