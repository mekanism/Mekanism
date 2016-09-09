package mekanism.common.recipe;

import mekanism.common.MekanismItems;
import mekanism.common.Tier.BinTier;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ItemProxy;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class BinRecipe implements IRecipe
{
	private static boolean registered;
	
	public BinRecipe()
	{
		if(!registered)
		{
			MinecraftForge.EVENT_BUS.register(this);
			registered = true;
		}
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

		return BasicBlockType.get(itemStack) == BasicBlockType.BIN && itemStack.stackSize <= 1;
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

		if(bin == null || bin.stackSize > 1)
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
			if(!(addStack.getItem() instanceof ItemProxy))
			{
				if(binInv.getItemType() != null && !binInv.getItemType().isItemEqual(addStack))
				{
					return null;
				}
	
				binInv.add(addStack);
			}
			
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
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
	}

	@SubscribeEvent
	public void onCrafting(ItemCraftedEvent event)
	{
		if(getResult(event.craftMatrix) != null)
		{
			if(!isBin(event.crafting))
			{
				for(int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
				{
					if(isBin(event.craftMatrix.getStackInSlot(i)))
					{
						ItemStack bin = event.craftMatrix.getStackInSlot(i);
						InventoryBin inv = new InventoryBin(bin.copy());

						int size = inv.getItemCount();

						ItemStack testRemove = inv.removeStack();
						int newCount = size-(testRemove != null ? testRemove.stackSize : 0);
						
						if(inv.getTier() == BinTier.CREATIVE)
						{
							newCount = size;
						}

						ItemDataUtils.setInt(bin, "newCount", newCount);
					}
				}
			}
			else {
				int bin = -1;
				int other = -1;

				for(int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
				{
					if(isBin(event.craftMatrix.getStackInSlot(i)))
					{
						bin = i;
					}
					else if(!isBin(event.craftMatrix.getStackInSlot(i)) && event.craftMatrix.getStackInSlot(i) != null)
					{
						other = i;
					}
				}

				ItemStack binStack = event.craftMatrix.getStackInSlot(bin);
				ItemStack otherStack = event.craftMatrix.getStackInSlot(other);

				ItemStack testRemain = new InventoryBin(binStack.copy()).add(otherStack.copy());

				if(testRemain != null && testRemain.stackSize > 0)
				{
					ItemStack proxy = new ItemStack(MekanismItems.ItemProxy);
					((ItemProxy)proxy.getItem()).setSavedItem(proxy, testRemain.copy());
					event.craftMatrix.setInventorySlotContents(other, proxy);
				}
				else {
					event.craftMatrix.setInventorySlotContents(other, null);
				}
			}
		}
	}
}
