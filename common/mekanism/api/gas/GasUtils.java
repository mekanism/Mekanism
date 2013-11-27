package mekanism.api.gas;

import net.minecraft.item.ItemStack;

public final class GasUtils
{
	public static int addGas(ItemStack itemStack, GasStack stack)
	{
		if(itemStack != null && itemStack.getItem() instanceof IGasItem && ((IGasItem)itemStack.getItem()).canReceiveGas(itemStack, stack.getGas()))
		{
			return ((IGasItem)itemStack.getItem()).addGas(itemStack, stack.copy());
		}
		
		return 0;
	}
	
	public static GasStack removeGas(ItemStack itemStack, Gas type, int amount)
	{
		if(itemStack != null && itemStack.getItem() instanceof IGasItem)
		{
			IGasItem item = (IGasItem)itemStack.getItem();
			
			if(type != null && item.getGas(itemStack) != null && item.getGas(itemStack).getGas() != type || !item.canProvideGas(itemStack, type))
			{
				return null;
			}
			
			return item.removeGas(itemStack, amount);
		}
		
		return null;
	}
}
