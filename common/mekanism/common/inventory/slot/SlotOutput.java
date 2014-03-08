package mekanism.common.inventory.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotOutput extends Slot
{
	public SlotOutput(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return false;
	}
}
