package mekanism.common;

import universalelectricity.implement.IItemElectric;
import ic2.api.IElectricItem;
import mekanism.api.IEnergizedItem;
import net.minecraft.src.*;

public class SlotEnergy extends Slot
{
	public SlotEnergy(IInventory inventory, int x, int y, int z)
	{
		super(inventory, x, y, z);
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return itemstack.getItem() instanceof IEnergizedItem || itemstack.getItem() instanceof IElectricItem || itemstack.getItem() instanceof IItemElectric || itemstack.itemID == Item.redstone.shiftedIndex;
	}
}
