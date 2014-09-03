package mekanism.common.inventory.slot;

import mekanism.common.util.ChargeUtils;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

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
			return ChargeUtils.canBeCharged(itemstack);
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
			return ChargeUtils.canBeDischarged(itemstack);
		}
	}
}
