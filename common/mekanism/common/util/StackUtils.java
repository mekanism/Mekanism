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
	
	public static List<ItemStack> even(ItemStack stack1, ItemStack stack2)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		
		if(getSize(stack1) == getSize(stack2) || Math.abs(getSize(stack1)-getSize(stack2)) == 1)
		{
			ret.add(stack1);
			ret.add(stack2);
			
			return ret;
		}
		
		if(getSize(stack1) > getSize(stack2))
		{
			int diff = getSize(stack1)-getSize(stack2);
			
			List<ItemStack> split = split(size(stack1, diff));
			
			ret.add(subtract(stack1, split.get(0)));
			ret.add(add(stack2, split.get(1)));
		}
		else if(getSize(stack2) > getSize(stack1))
		{
			int diff = getSize(stack2)-getSize(stack1);
			
			List<ItemStack> split = split(size(stack2, diff));
			
			ret.add(subtract(stack2, split.get(0)));
			ret.add(add(stack1, split.get(1)));
		}
		
		return ret;
	}
	
	public static ItemStack add(ItemStack stack1, ItemStack stack2)
	{
		if(stack1 == null)
		{
			return stack2;
		}
		else if(stack2 == null)
		{
			return stack1;
		}
		
		return size(stack1, getSize(stack1)+getSize(stack2));
	}
	
	public static ItemStack subtract(ItemStack stack1, ItemStack stack2)
	{
		if(stack1 == null)
		{
			return null;
		}
		else if(stack2 == null)
		{
			return stack1;
		}
		
		return size(stack1, getSize(stack1)-getSize(stack2));
	}
	
	public static ItemStack size(ItemStack stack, int size)
	{
		if(size <= 0 || stack == null)
		{
			return null;
		}
		
		ItemStack ret = stack.copy();
		ret.stackSize = size;
		return ret;
	}
	
	public static int getSize(ItemStack stack)
	{
		return stack != null ? stack.stackSize : 0;
	}
}
