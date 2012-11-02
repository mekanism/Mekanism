package net.uberkat.obsidian.hawk.common;

import hawk.api.ProcessingRecipes;
import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;
import net.minecraft.src.SlotFurnace;
import net.minecraft.src.TileEntity;
import universalelectricity.prefab.SlotElectricItem;
import universalelectricity.implement.IItemElectric;

public class ContainerWasher extends Container
{
	private TileEntityWasher tileEntity;
	
	public ContainerWasher(InventoryPlayer playerInventory, TileEntityWasher tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new SlotElectricItem(tileEntity, 0, 36, 47));//Electric item
		addSlotToContainer(new Slot(tileEntity, 1, 58, 52));//Water input
		addSlotToContainer(new Slot(tileEntity, 2, 36, 15));//Actual input
		addSlotToContainer(new SlotFurnace(playerInventory.player, tileEntity, 3, 107, 32));
		addSlotToContainer(new SlotFurnace(playerInventory.player, tileEntity, 4, 125, 32));
		addSlotToContainer(new SlotFurnace(playerInventory.player, tileEntity, 5, 143, 32));
		
		for (int counter = 0; counter < 3; ++counter)
		{
			for (int var4 = 0; var4 < 9; ++var4)
			{
				addSlotToContainer(new Slot(playerInventory, var4 + counter * 9 + 9, 8 + var4 * 18, 84 + counter * 18));
			}
		}
		
		for (int counter = 0; counter < 9; ++counter)
		{
			addSlotToContainer(new Slot(playerInventory, counter, 8 + counter * 18, 142));
		}
		
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer var1)
	{
		return true;
	}
	
	@Override
	public ItemStack func_82846_b(EntityPlayer player, int par1)
	{
		ItemStack var2 = null;
		Slot var3 = (Slot)inventorySlots.get(par1);
		
		if (var3 != null && var3.getHasStack())
		{
			ItemStack var4 = var3.getStack();
			var2 = var4.copy();
			
			if (par1 == 2)
			{
				if (!mergeItemStack(var4, 3, 39, true))
				{
					return null;
				}
				
				var3.onSlotChange(var4, var2);
			}
			else if (par1 != 1 && par1 != 0)
			{
				if (var4.getItem() instanceof IItemElectric)
				{
					if (!mergeItemStack(var4, 0, 1, false))
					{
						return null;
					}
				}
				else if (ProcessingRecipes.getResult(var4, ProcessingRecipes.EnumProcessing.WASHING) != null)
				{
					if (!mergeItemStack(var4, 1, 2, false))
					{
						return null;
					}
				}
				else if (par1 >= 3 && par1 < 30)
				{
					if (!mergeItemStack(var4, 30, 39, false))
					{
						return null;
					}
				}
				else if (par1 >= 30 && par1 < 39 && !mergeItemStack(var4, 3, 30, false))
				{
					return null;
				}
			}
			else if (!mergeItemStack(var4, 3, 39, false))
			{
				return null;
			}
			
			if (var4.stackSize == 0)
			{
				var3.putStack((ItemStack)null);
			}
			else
			{
				var3.onSlotChanged();
			}
			
			if (var4.stackSize == var2.stackSize)
			{
				return null;
			}
			
			var3.func_82870_a(player, var4);
		}
		
		return var2;
	}
	
}
