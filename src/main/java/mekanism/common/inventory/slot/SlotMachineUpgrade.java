package mekanism.common.inventory.slot;

import mekanism.common.base.IUpgradeItem;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotMachineUpgrade extends Slot
{
	public SlotMachineUpgrade(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof IUpgradeItem;
	}
}
