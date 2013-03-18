package mekanism.common;

import universalelectricity.core.item.IItemElectric;
import ic2.api.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
