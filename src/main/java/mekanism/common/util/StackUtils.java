package mekanism.common.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public final class StackUtils
{
	public static List<ItemStack> split(ItemStack stack)
	{
		if(stack.isEmpty() || stack.getCount() == 0)
		{
			return null;
		}

		List<ItemStack> ret = new ArrayList<>();

		if(stack.getCount() == 1)
		{
			ret.add(stack);
			return ret;
		}

		int remain = stack.getCount() % 2;
		int split = (int)((float)(stack.getCount())/2F);

		ret.add(size(stack, split+remain));
		ret.add(size(stack, split));

		return ret;
	}

	public static Item getItem(ItemStack stack)
	{
		if(stack.isEmpty())
		{
			return null;
		}

		return stack.getItem();
	}

	public static boolean diffIgnoreNull(ItemStack stack1, ItemStack stack2)
	{
		if(stack1.isEmpty() || stack2.isEmpty())
		{
			return false;
		}

		return stack1.getItem() != stack2.getItem() || stack1.getItemDamage() != stack2.getItemDamage();
	}

	public static boolean equalsWildcard(ItemStack wild, ItemStack check)
	{
		if(wild.isEmpty() || check.isEmpty())
		{
			return check == wild;
		}
		
		return wild.getItem() == check.getItem() && (wild.getItemDamage() == OreDictionary.WILDCARD_VALUE || check.getItemDamage() == OreDictionary.WILDCARD_VALUE || wild.getItemDamage() == check.getItemDamage());
	}

	public static boolean equalsWildcardWithNBT(ItemStack wild, ItemStack check)
	{
		boolean wildcard = equalsWildcard(wild, check);
		
		if(wild.isEmpty() || check.isEmpty())
		{
			return wildcard;
		}
		
		return wildcard && (!wild.hasTagCompound() ? !check.hasTagCompound() : (wild.getTagCompound() == check.getTagCompound() || wild.getTagCompound().equals(check.getTagCompound())));
	}

	public static List<ItemStack> even(ItemStack stack1, ItemStack stack2)
	{
		ArrayList<ItemStack> ret = new ArrayList<>();

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
			ret.add(add(stack2, split.get(0)));
		}
		else if(getSize(stack2) > getSize(stack1))
		{
			int diff = getSize(stack2)-getSize(stack1);

			List<ItemStack> split = split(size(stack2, diff));

			ret.add(subtract(stack2, split.get(0)));
			ret.add(add(stack1, split.get(0)));
		}

		return ret;
	}

	public static ItemStack add(ItemStack stack1, ItemStack stack2)
	{
		if(stack1.isEmpty())
		{
			return stack2;
		}
		else if(stack2.isEmpty())
		{
			return stack1;
		}

		return size(stack1, getSize(stack1)+getSize(stack2));
	}

	public static ItemStack subtract(ItemStack stack1, ItemStack stack2)
	{
		if(stack1.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		else if(stack2.isEmpty())
		{
			return stack1;
		}

		return size(stack1, getSize(stack1)-getSize(stack2));
	}

	public static ItemStack size(ItemStack stack, int size)
	{
		if(size <= 0 || stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		ItemStack ret = stack.copy();
		ret.setCount(size);
		
		return ret;
	}

	public static ItemStack copy(ItemStack stack)
	{
		if(stack.isEmpty())
		{
			return ItemStack.EMPTY;
		}

		return stack.copy();
	}

	public static int getSize(ItemStack stack)
	{
		return !stack.isEmpty() ? stack.getCount() : 0;
	}
	
	public static List<ItemStack> getMergeRejects(NonNullList<ItemStack> orig, NonNullList<ItemStack> toAdd)
	{
		List<ItemStack> ret = new ArrayList<>();
		
		for(int i = 0; i < toAdd.size(); i++)
		{
			if(!toAdd.get(i).isEmpty())
			{
				ItemStack reject = getMergeReject(orig.get(i), toAdd.get(i));
				
				if(!reject.isEmpty())
				{
					ret.add(reject);
				}
			}
		}
			
		return ret;
	}
	
	public static void merge(NonNullList<ItemStack> orig, NonNullList<ItemStack> toAdd)
	{
		for(int i = 0; i < toAdd.size(); i++)
		{
			if(!toAdd.get(i).isEmpty())
			{
				orig.set(i, merge(orig.get(i), toAdd.get(i)));
			}
		}
	}
	
	public static ItemStack merge(ItemStack orig, ItemStack toAdd)
	{
		if(orig.isEmpty())
		{
			return toAdd;
		}
		
		if(toAdd.isEmpty())
		{
			return orig;
		}
		
		if(!orig.isItemEqual(toAdd) || !ItemStack.areItemStackTagsEqual(orig, toAdd))
		{
			return orig;
		}
		
		return StackUtils.size(orig, Math.min(orig.getMaxStackSize(), orig.getCount()+toAdd.getCount()));
	}
	
	public static ItemStack getMergeReject(ItemStack orig, ItemStack toAdd)
	{
		if(orig.isEmpty())
		{
			return ItemStack.EMPTY;
		}
		
		if(toAdd.isEmpty())
		{
			return orig;
		}
		
		if(!orig.isItemEqual(toAdd) || !ItemStack.areItemStackTagsEqual(orig, toAdd))
		{
			return orig;
		}
		
		int newSize = orig.getCount()+toAdd.getCount();
		
		if(newSize > orig.getMaxStackSize())
		{
			return StackUtils.size(orig, newSize-orig.getMaxStackSize());
		}
		else {
			return StackUtils.size(orig, newSize);
		}
	}

	public static int hashItemStack(ItemStack stack)
	{
		if(stack.isEmpty() || stack.getItem() == null)
		{
			return -1;
		}
		
		String name = stack.getItemDamage() == OreDictionary.WILDCARD_VALUE ? stack.getItem().getTranslationKey() : stack.getItem().getTranslationKey(stack);
		return name.hashCode() << 8 | stack.getItemDamage();
	}
}
