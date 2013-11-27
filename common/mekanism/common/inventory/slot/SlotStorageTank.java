package mekanism.common.inventory.slot;

import mekanism.api.gas.Gas;
import mekanism.api.gas.IGasItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotStorageTank extends Slot
{
	public Gas type;
	public boolean acceptsAllGasses;
	
	public SlotStorageTank(IInventory inventory, Gas gas, boolean all, int index, int x, int y)
	{
		super(inventory, index, x, y);
		type = gas;
		acceptsAllGasses = all;
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		if(acceptsAllGasses) 
		{
			return itemstack.getItem() instanceof IGasItem;
		}
		
		if(itemstack.getItem() instanceof IGasItem)
		{
			return ((IGasItem)itemstack.getItem()).getGas(itemstack) == null || ((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == type;
		}
		
		return false;
	}
}
