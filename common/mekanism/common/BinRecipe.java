package mekanism.common;

import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import cpw.mods.fml.common.ICraftingHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class BinRecipe implements IRecipe, ICraftingHandler
{
	public BinRecipe()
	{
		GameRegistry.registerCraftingHandler(this);
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World world) 
	{
		return getCraftingResult(inv) != null;
	}
	
	private boolean isBin(ItemStack itemStack)
	{
		if(itemStack == null)
		{
			return false;
		}
		
		return itemStack.getItem() instanceof ItemBlockBasic && itemStack.getItemDamage() == 6;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		return getResult(inv);
	}
	
	public ItemStack getResult(IInventory inv)
	{
		ItemStack bin = null;
		
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			
			if(isBin(stack))
			{
				if(bin != null)
				{
					return null;
				}
				
				bin = stack.copy();
			}
		}
		
		if(bin == null)
		{
			return null;
		}
		
		ItemStack addStack = null;
		
		for(int i = 0; i < 9; i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			
			if(stack != null && !isBin(stack))
			{
				if(addStack != null)
				{
					return null;
				}
				
				addStack = stack.copy();
			}
		}
		
		InventoryBin binInv = new InventoryBin(bin);
		
		if(addStack != null)
		{
			if(binInv.getItemType() != null && !binInv.getItemType().isItemEqual(addStack))
			{
				return null;
			}
			
			binInv.add(addStack);
			
			return bin;
		}
		else {
			return binInv.removeStack();
		}
	}

	@Override
	public int getRecipeSize() 
	{
		return 0;
	}

	@Override
	public ItemStack getRecipeOutput() 
	{
		return null;
	}

	@Override
	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix) 
	{
		if(getResult(craftMatrix) != null)
		{
			if(!isBin(item))
			{
				for(int i = 0; i < craftMatrix.getSizeInventory(); i++)
				{
					if(isBin(craftMatrix.getStackInSlot(i)))
					{
						ItemStack bin = craftMatrix.getStackInSlot(i);
						InventoryBin inv = new InventoryBin(bin.copy());
						
						int size = inv.getItemCount();
						
						ItemStack testRemove = inv.removeStack();
						
						bin.stackTagCompound.setInteger("newCount", size-(testRemove != null ? testRemove.stackSize : 0));
					}
				}
			}
			else {
				int bin = -1;
				int other = -1;
				
				for(int i = 0; i < craftMatrix.getSizeInventory(); i++)
				{
					if(isBin(craftMatrix.getStackInSlot(i)))
					{
						bin = i;
					}
					else if(!isBin(craftMatrix.getStackInSlot(i)) && craftMatrix.getStackInSlot(i) != null)
					{
						other = i;
					}
				}
				
				ItemStack binStack = craftMatrix.getStackInSlot(bin);
				ItemStack otherStack = craftMatrix.getStackInSlot(other);
				
				ItemStack testRemain = new InventoryBin(binStack.copy()).add(otherStack.copy());
				
				if(testRemain != null && testRemain.stackSize > 0)
				{
					ItemStack proxy = new ItemStack(Mekanism.ItemProxy);
					((ItemProxy)proxy.getItem()).setSavedItem(proxy, testRemain.copy());
					craftMatrix.setInventorySlotContents(other, proxy);
				}
				else {
					craftMatrix.setInventorySlotContents(other, null);
				}
				
				craftMatrix.setInventorySlotContents(bin, null);
			}
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item) {}
}
