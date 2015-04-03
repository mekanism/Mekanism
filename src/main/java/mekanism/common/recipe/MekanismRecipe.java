package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.Upgrade;
import mekanism.common.base.IEnergyCube;
import mekanism.common.base.IFactory;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.item.ItemBlockBasic;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Code originally from Eloraam and her work on the Ore Dictionary.  Cleaned up and modified to work well with energized items.
 * @author Eloraam, aidancbrady
 *
 */
public class MekanismRecipe implements IRecipe
{
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;

	private ItemStack output = null;
	private Object[] input = null;

	public int width = 0;
	public int height = 0;

	private boolean mirrored = true;

	public MekanismRecipe(ItemStack result, Object... recipe)
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
			String ret = "Invalid shaped ore recipe: ";

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
				String ret = "Invalid shaped ore recipe: ";

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
		ItemStack toReturn = output.copy();

		if(toReturn.getItem() instanceof IEnergizedItem)
		{
			double energyFound = 0;

			for(int i = 0; i < 9; i++)
			{
				ItemStack itemstack = inv.getStackInSlot(i);

				if(itemstack != null && itemstack.getItem() instanceof IEnergizedItem)
				{
					energyFound += ((IEnergizedItem)itemstack.getItem()).getEnergy(itemstack);
				}
			}

			((IEnergizedItem)toReturn.getItem()).setEnergy(toReturn, Math.min(((IEnergizedItem)toReturn.getItem()).getMaxEnergy(toReturn), energyFound));
		}
		
		if(toReturn.getItem() instanceof IGasItem)
		{
			GasStack gasFound = null;
			
			for(int i = 0; i < 9; i++)
			{
				ItemStack itemstack = inv.getStackInSlot(i);

				if(itemstack != null && itemstack.getItem() instanceof IGasItem)
				{
					GasStack stored = ((IGasItem)itemstack.getItem()).getGas(itemstack);
					
					if(stored != null)
					{
						if(!((IGasItem)toReturn.getItem()).canReceiveGas(toReturn, stored.getGas()))
						{
							return null;
						}
						
						if(gasFound == null)
						{
							gasFound = stored;
						}
						else {
							if(gasFound.getGas() != stored.getGas())
							{
								return null;
							}
							
							gasFound.amount += stored.amount;
						}
					}
				}
			}
			
			if(gasFound != null)
			{
				gasFound.amount = Math.min(((IGasItem)toReturn.getItem()).getMaxGas(toReturn), gasFound.amount);
				((IGasItem)toReturn.getItem()).setGas(toReturn, gasFound);
			}
		}

		if(MachineType.get(toReturn) != null && MachineType.get(toReturn).supportsUpgrades)
		{
			Map<Upgrade, Integer> upgrades = new HashMap<Upgrade, Integer>();

			for(int i = 0; i < 9; i++)
			{
				ItemStack itemstack = inv.getStackInSlot(i);

				if(itemstack != null && MachineType.get(itemstack) != null && MachineType.get(itemstack).supportsUpgrades)
				{
					Map<Upgrade, Integer> stackMap = Upgrade.buildMap(itemstack.stackTagCompound);
					
					for(Map.Entry<Upgrade, Integer> entry : stackMap.entrySet())
					{
						if(entry != null && entry.getKey() != null && entry.getValue() != null)
						{
							Integer val = upgrades.get(entry.getKey());
							
							upgrades.put(entry.getKey(), Math.min(entry.getKey().getMax(), (val != null ? val : 0) + entry.getValue()));
						}
					}
				}
			}
			
			if(toReturn.stackTagCompound == null)
			{
				toReturn.setTagCompound(new NBTTagCompound());
			}
			
			Upgrade.saveMap(upgrades, toReturn.stackTagCompound);
		}

		return toReturn;
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
					if(!checkItemEquals((ItemStack)target, slot))
					{
						return false;
					}
				}
				else if(target instanceof ArrayList)
				{
					boolean matched = false;

					for(ItemStack item : (ArrayList<ItemStack>)target)
					{
						matched = matched || checkItemEquals(item, slot);
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

	private boolean checkItemEquals(ItemStack target, ItemStack input)
	{
		if(input == null && target != null || input != null && target == null)
		{
			return false;
		}
		else if(input == null && target == null)
		{
			return true;
		}

		if(target.getItem() != input.getItem())
		{
			return false;
		}

		if(!(target.getItem() instanceof IEnergizedItem) && !(input.getItem() instanceof IEnergizedItem) && !(target.getItem() instanceof IGasItem) && !(input.getItem() instanceof IGasItem))
		{
			if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
			{
				return false;
			}
		}
		else {
			if(target.getItem() instanceof IEnergizedItem && input.getItem() instanceof IEnergizedItem)
			{
				if(((IEnergizedItem)target.getItem()).isMetadataSpecific(target) && ((IEnergizedItem)input.getItem()).isMetadataSpecific(input))
				{
					if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
					{
						return false;
					}
				}
			}
			
			if(target.getItem() instanceof IGasItem && input.getItem() instanceof IGasItem)
			{
				if(((IGasItem)target.getItem()).isMetadataSpecific(target) && ((IGasItem)input.getItem()).isMetadataSpecific(input))
				{
					if(target.getItemDamage() != input.getItemDamage() && target.getItemDamage() != OreDictionary.WILDCARD_VALUE)
					{
						return false;
					}
				}
			}

			if(target.getItem() instanceof IEnergyCube && input.getItem() instanceof IEnergyCube)
			{
				if(((IEnergyCube)target.getItem()).getEnergyCubeTier(target) != ((IEnergyCube)input.getItem()).getEnergyCubeTier(input))
				{
					return false;
				}
			}
			else if(target.getItem() instanceof ItemBlockBasic && input.getItem() instanceof ItemBlockBasic)
			{
				if(((ItemBlockBasic)target.getItem()).getTier(target) != ((ItemBlockBasic)input.getItem()).getTier(input))
				{
					return false;
				}
			}
			else if(target.getItem() instanceof IFactory && input.getItem() instanceof IFactory)
			{
				if(isFactory(target) && isFactory(input))
				{
					if(((IFactory)target.getItem()).getRecipeType(target) != ((IFactory)input.getItem()).getRecipeType(input))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	private static boolean isFactory(ItemStack stack)
	{
		return MachineType.get(stack) == MachineType.BASIC_FACTORY || MachineType.get(stack) == MachineType.ADVANCED_FACTORY || MachineType.get(stack) == MachineType.ELITE_FACTORY;
	}

	public MekanismRecipe setMirrored(boolean mirror)
	{
		mirrored = mirror;
		return this;
	}

	public Object[] getInput()
	{
		return input;
	}
}
