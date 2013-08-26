package mekanism.common.inventory.slot;

import mekanism.api.IStorageTank;
import mekanism.api.gas.EnumGas;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotStorageTank extends Slot
{
	public EnumGas type;
	public boolean acceptsAllGasses;
	
	public SlotStorageTank(IInventory inventory, EnumGas gas, boolean all, int index, int x, int y)
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
			return itemstack.getItem() instanceof IStorageTank;
		}
		
		if(itemstack.getItem() instanceof IStorageTank)
		{
			return ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == type || ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.NONE;
		}
		return false;
	}
}
