package mekanism.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mekanism.api.energy.IEnergizedItem;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.api.util.StackUtils;
import mekanism.common.Upgrade;
import mekanism.common.base.IEnergyCube;
import mekanism.common.base.IFactory;
import mekanism.common.base.ITierItem;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.recipe.ShapelessMekanismRecipe;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeUtils 
{
	public static boolean areItemsEqualForCrafting(ItemStack target, ItemStack input)
	{
		if(target == null && input != null || target != null && input == null)
		{
			return false;
		}
		else if(target == null && input == null)
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
			else if(target.getItem() instanceof ITierItem && input.getItem() instanceof ITierItem)
			{
				if(((ITierItem)target.getItem()).getBaseTier(target) != ((ITierItem)input.getItem()).getBaseTier(input))
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
	
	public static ItemStack getCraftingResult(InventoryCrafting inv, ItemStack toReturn)
	{
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
	
	public static ItemStack loadRecipeItemStack(NBTTagCompound nbtTags)
	{
		int meta = 0;
		int amount = 1;
		
		if(nbtTags.hasKey("meta"))
		{
			meta = nbtTags.getInteger("meta");
		}
		
		if(nbtTags.hasKey("amount"))
		{
			amount = nbtTags.getInteger("amount");
		}
		
		if(nbtTags.hasKey("itemstack"))
		{
			return ItemStack.loadItemStackFromNBT(nbtTags.getCompoundTag("itemstack"));
		}
		else if(nbtTags.hasKey("itemname"))
		{
			Object obj = Item.itemRegistry.getObject(nbtTags.getString("itemname"));
			
			if(obj instanceof Item)
			{
				return new ItemStack((Item)obj, amount, meta);
			}
		}
		else if(nbtTags.hasKey("blockname"))
		{
			Object obj = Block.blockRegistry.getObject(nbtTags.getString("blockname"));
			
			if(obj instanceof Block)
			{
				return new ItemStack((Block)obj, amount, meta);
			}
		}
		
		return null;
	}
	
	public static boolean removeRecipes(ItemStack stack)
	{
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		
		for(Iterator<IRecipe> iter = recipes.iterator(); iter.hasNext();)
		{
			IRecipe iterRecipe = iter.next();
			
			if(iterRecipe instanceof ShapedMekanismRecipe || iterRecipe instanceof ShapelessMekanismRecipe)
			{
				if(StackUtils.equalsWildcard(stack, iterRecipe.getRecipeOutput()))
				{
					iter.remove();
				}
			}
		}
		
		return false;
	}
}
