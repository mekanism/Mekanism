package mekanism.common.transporter;

import java.util.List;

import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;

public abstract class Finder
{
	public abstract boolean modifies(ItemStack stack);
	
	public static class FirstFinder extends Finder
	{
		@Override
		public boolean modifies(ItemStack stack)
		{
			return true;
		}
	}
	
	public static class OreDictFinder extends Finder
	{
		public String oreDictName;
		
		public OreDictFinder(String name)
		{
			oreDictName = name;
		}
		
		@Override
		public boolean modifies(ItemStack stack)
		{
			List<String> oreKeys = MekanismUtils.getOreDictName(stack);
			
			if(!oreDictName.equals("*") && oreKeys.isEmpty())
			{
				return false;
			}
			
			for(String oreKey : oreKeys)
			{
				if(oreDictName.equals(oreKey) || oreDictName.equals("*"))
				{
					return true;
				}
				else if(oreDictName.endsWith("*") && !oreDictName.startsWith("*"))
				{
					if(oreKey.startsWith(oreDictName.substring(0, oreDictName.length()-1)))
					{
						return true;
					}
				}
				else if(oreDictName.startsWith("*") && !oreDictName.endsWith("*"))
				{
					if(oreKey.endsWith(oreDictName.substring(1)))
					{
						return true;
					}
				}
				else if(oreDictName.startsWith("*") && oreDictName.endsWith("*"))
				{
					if(oreKey.contains(oreDictName.substring(1, oreDictName.length()-1)))
					{
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	public static class ItemStackFinder extends Finder
	{
		public ItemStack itemType;
		
		public ItemStackFinder(ItemStack type)
		{
			itemType = type;
		}
		
		@Override
		public boolean modifies(ItemStack stack)
		{
			return StackUtils.equalsWildcard(itemType, stack);
		}
	}
}
