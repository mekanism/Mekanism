package mekanism.common;

import universalelectricity.implement.IItemElectric;
import ic2.api.IElectricItem;
import mekanism.api.IEnergizedItem;
import mekanism.api.IStorageTank;
import mekanism.api.IStorageTank.EnumGas;
import net.minecraft.src.*;

public class SlotStorageTank extends Slot
{
	public EnumGas type;
	
	public SlotStorageTank(IInventory inventory, EnumGas gas, int index, int x, int y)
	{
		super(inventory, index, x, y);
		type = gas;
	}
	
	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		if(itemstack.getItem() instanceof IStorageTank)
		{
			return ((IStorageTank)itemstack.getItem()).gasType() == type;
		}
		return false;
	}
}
