package mekanism.common;

import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.implement.IItemElectric;

public class SlotEnergy extends Slot
{
	public SlotEnergy(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof IElectricItem || itemstack.getItem() instanceof IItemElectric || itemstack.itemID == Item.redstone.itemID;
	}
}
