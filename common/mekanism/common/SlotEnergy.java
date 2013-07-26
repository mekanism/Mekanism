package mekanism.common;

import ic2.api.item.IElectricItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;

public class SlotEnergy
{
	public static class SlotCharge extends Slot
	{
		public SlotCharge(IInventory inventory, int index, int x, int y)
		{
			super(inventory, index, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack itemstack)
		{
			return MekanismUtils.canBeCharged(itemstack);
		}
	}
	
	public static class SlotDischarge extends Slot
	{
		public SlotDischarge(IInventory inventory, int index, int x, int y)
		{
			super(inventory, index, x, y);
		}
		
		@Override
		public boolean isItemValid(ItemStack itemstack)
		{
			return MekanismUtils.canBeDischarged(itemstack);
		}
	}
}
