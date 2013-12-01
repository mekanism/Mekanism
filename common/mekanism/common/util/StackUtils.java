package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public final class StackUtils
{
	public static List<ItemStack> split(ItemStack stack)
	{
		if(stack == null || stack.stackSize == 0)
		{
			return null;
		}
		
		List<ItemStack> ret = new ArrayList<ItemStack>();
		
		if(stack.stackSize == 1)
		{
			ret.add(stack);
			return ret;
		}
		
		int remain = stack.stackSize % 2;
		int split = (int)((float)stack.stackSize/2F);
		
		ret.add(MekanismUtils.size(stack, split+remain));
		ret.add(MekanismUtils.size(stack, split));
		
		return ret;
	}
}
