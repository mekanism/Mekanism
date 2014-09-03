package mekanism.common.content.transporter;

import java.util.List;

import mekanism.api.util.StackUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
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

			if(oreKeys.isEmpty())
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
	
	public static class MaterialFinder extends Finder
	{
		public Material materialType;
		
		public MaterialFinder(Material type)
		{
			materialType = type;
		}
		
		@Override
		public boolean modifies(ItemStack stack)
		{
			if(stack == null || !(stack.getItem() instanceof ItemBlock))
			{
				return false;
			}
			
			return Block.getBlockFromItem(stack.getItem()).getMaterial() == materialType;
		}
	}
	
	public static class ModIDFinder extends Finder
	{
		public String modID;
		
		public ModIDFinder(String mod)
		{
			modID = mod;
		}
		
		@Override
		public boolean modifies(ItemStack stack)
		{
			if(stack == null || !(stack.getItem() instanceof ItemBlock))
			{
				return false;
			}
			
			String id = MekanismUtils.getMod(stack);
			
			if(modID.equals(id) || modID.equals("*"))
			{
				return true;
			}
			else if(modID.endsWith("*") && !modID.startsWith("*"))
			{
				if(id.startsWith(modID.substring(0, modID.length()-1)))
				{
					return true;
				}
			}
			else if(modID.startsWith("*") && !modID.endsWith("*"))
			{
				if(id.endsWith(modID.substring(1)))
				{
					return true;
				}
			}
			else if(modID.startsWith("*") && modID.endsWith("*"))
			{
				if(id.contains(modID.substring(1, modID.length()-1)))
				{
					return true;
				}
			}
			
			return false;
		}
	}
}
