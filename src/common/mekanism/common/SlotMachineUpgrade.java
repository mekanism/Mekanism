package mekanism.common;

import mekanism.api.ItemMachineUpgrade;
import net.minecraft.src.*;

public class SlotMachineUpgrade extends Slot
{
	public SlotMachineUpgrade(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof ItemMachineUpgrade;
	}
}
