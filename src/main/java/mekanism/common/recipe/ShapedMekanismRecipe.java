package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import mekanism.common.Mekanism;
import mekanism.common.util.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Code originally from Eloraam and her work on the Ore Dictionary.  Cleaned up and modified to work well with energized items.
 * @author Eloraam, aidancbrady
 *
 */
public class ShapedMekanismRecipe implements IRecipe
{
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;

	private ItemStack output = null;
	private Object[] input = null;

	public int width = 0;
	public int height = 0;

	private boolean mirrored = true;
	
	public ShapedMekanismRecipe(ItemStack result, Object... recipe)
	{
		output = result.copy();

		String shape = "";
		int idx = 0;

		if(recipe[idx] instanceof Boolean)
		{
			mirrored = (Boolean)recipe[idx];

			if(recipe[idx+1] instanceof Object[])
			{
				recipe = (Object[])recipe[idx+1];
			}
			else {
				idx = 1;
			}
		}

		if(recipe[idx] instanceof String[])
		{
			String[] parts = ((String[])recipe[idx++]);

			for(String s : parts)
			{
				width = s.length();
				shape += s;
			}

			height = parts.length;
		}
		else {
			while(recipe[idx] instanceof String)
			{
				String s = (String)recipe[idx++];
				shape += s;
				width = s.length();
				height++;
			}
		}

		if(width * height != shape.length())
		{
			String ret = "Invalid shaped Mekanism recipe: ";

			for(Object tmp :  recipe)
			{
				ret += tmp + ", ";
			}

			ret += output;

			throw new RuntimeException(ret);
		}

		HashMap<Character, Object> itemMap = new HashMap<Character, Object>();

		for(; idx < recipe.length; idx += 2)
		{
			Character chr = (Character)recipe[idx];
			Object in = recipe[idx + 1];

			if(in instanceof ItemStack)
			{
				itemMap.put(chr, ((ItemStack)in).copy());
			}
			else if(in instanceof Item)
			{
				itemMap.put(chr, new ItemStack((Item)in));
			}
			else if(in instanceof Block)
			{
				itemMap.put(chr, new ItemStack((Block)in, 1, OreDictionary.WILDCARD_VALUE));
			}
			else if(in instanceof String)
			{
				itemMap.put(chr, OreDictionary.getOres((String)in));
			}
			else {
				String ret = "Invalid shaped Mekanism recipe: ";

				for(Object tmp :  recipe)
				{
					ret += tmp + ", ";
				}

				ret += output;
				throw new RuntimeException(ret);
			}
		}

		input = new Object[width * height];
		int x = 0;

		for(char chr : shape.toCharArray())
		{
			input[x++] = itemMap.get(chr);
		}
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return RecipeUtils.getCraftingResult(inv, output.copy());
	}

	@Override
	public int getRecipeSize()
	{
		return input.length;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return output;
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		for(int x = 0; x <= MAX_CRAFT_GRID_WIDTH - width; x++)
		{
			for(int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - height; ++y)
			{
				if(checkMatch(inv, x, y, true))
				{
					return true;
				}

				if(mirrored && checkMatch(inv, x, y, false))
				{
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkMatch(InventoryCrafting inv, int startX, int startY, boolean mirror)
	{
		for(int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++)
		{
			for(int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++)
			{
				int subX = x - startX;
				int subY = y - startY;
				Object target = null;

				if(subX >= 0 && subY >= 0 && subX < width && subY < height)
				{
					if(mirror)
					{
						target = input[width - subX - 1 + subY * width];
					}
					else {
						target = input[subX + subY * width];
					}
				}

				ItemStack slot = inv.getStackInRowAndColumn(x, y);

				if(target instanceof ItemStack)
				{
					if(!RecipeUtils.areItemsEqualForCrafting((ItemStack)target, slot))
					{
						return false;
					}
				}
				else if(target instanceof Iterable)
				{
					boolean matched = false;

					for(ItemStack item : (Iterable<ItemStack>)target)
					{
						matched = matched || RecipeUtils.areItemsEqualForCrafting(item, slot);
					}

					if(!matched)
					{
						return false;
					}
				}
				else if(target == null && slot != null)
				{
					return false;
				}
			}
		}

		return true;
	}

	public ShapedMekanismRecipe setMirrored(boolean mirror)
	{
		mirrored = mirror;
		return this;
	}

	public Object[] getInput()
	{
		return input;
	}
	
	public static ShapedMekanismRecipe create(NBTTagCompound nbtTags)
	{
		if(!nbtTags.hasKey("result") || !nbtTags.hasKey("input"))
    	{
			Mekanism.logger.error("[Mekanism] Shaped recipe parse error: missing input or result compound tag.");
    		return null;
    	}
    	
    	ItemStack result = ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("result"));
    	NBTTagList list = nbtTags.getTagList("input", NBT.TAG_COMPOUND);
    	
    	if(result == null || list.tagCount() == 0)
    	{
    		Mekanism.logger.error("[Mekanism] Shaped recipe parse error: invalid result stack or input data list.");
    		return null;
    	}
    	
    	Object[] ret = new Object[list.tagCount()];
    	
    	for(int i = 0; i < list.tagCount(); i++)
    	{
    		NBTTagCompound compound = list.getCompoundTagAt(i);
    		
    		if(compound.hasKey("oredict"))
    		{
    			ret[i] = compound.getString("oredict");
    		}
    		else if(compound.hasKey("pattern"))
    		{
    			ret[i] = compound.getString("pattern");
    		}
    		else if(compound.hasKey("character"))
    		{
    			String s = compound.getString("character");
    			
    			if(s.length() > 1)
    			{
    				Mekanism.logger.error("[Mekanism] Shaped recipe parse error: invalid pattern character data.");
    				return null;
    			}
    			
    			ret[i] = compound.getString("character").toCharArray()[0];
    		}
    		else if(compound.hasKey("itemstack"))
    		{
    			ret[i] = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("itemstack"));
    		}
    		else {
    			Mekanism.logger.error("[Mekanism] Shaped recipe parse error: invalid input tag data key.");
    			return null;
    		}
    	}
    	
    	return new ShapedMekanismRecipe(result, ret);
	}
}
