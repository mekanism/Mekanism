package mekanism.common;

import mekanism.api.ItemMachineUpgrade;
import net.minecraft.src.*;

public class SlotMachineUpgrade extends Slot
{
	public SlotMachineUpgrade(IInventory inventory, int x, int y, int z)
	{
		super(inventory, x, y, z);
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof ItemMachineUpgrade;
	}
}
