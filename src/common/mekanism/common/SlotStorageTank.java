package mekanism.common;

import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import net.minecraft.src.*;

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
			return ((IStorageTank)itemstack.getItem()).gasType() == type;
		}
		return false;
	}
}
