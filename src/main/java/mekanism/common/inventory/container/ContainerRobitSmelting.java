package mekanism.common.inventory.container;

import mekanism.common.entity.EntityRobit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class ContainerRobitSmelting extends Container
{
	public EntityRobit robit;

	private int lastCookTime = 0;
	private int lastBurnTime = 0;
	private int lastItemBurnTime = 0;

	public ContainerRobitSmelting(InventoryPlayer inventory, EntityRobit entity)
	{
		robit = entity;
		robit.openInventory();

		addSlotToContainer(new Slot(entity, 28, 56, 17));
		addSlotToContainer(new Slot(entity, 29, 56, 53));
		addSlotToContainer(new SlotFurnace(inventory.player, entity, 30, 116, 35));

		int slotY;

		for(slotY = 0; slotY < 3; slotY++)
		{
			for(int slotX = 0; slotX < 9; slotX++)
			{
				addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, 84 + slotY * 18));
			}
		}

		for(slotY = 0; slotY < 9; slotY++)
		{
			addSlotToContainer(new Slot(inventory, slotY, 8 + slotY * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void addCraftingToCrafters(ICrafting icrafting)
	{
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, robit.furnaceCookTime);
		icrafting.sendProgressBarUpdate(this, 1, robit.furnaceBurnTime);
		icrafting.sendProgressBarUpdate(this, 2, robit.currentItemBurnTime);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		for(int i = 0; i < crafters.size(); ++i)
		{
			ICrafting icrafting = (ICrafting)crafters.get(i);

			if(lastCookTime != robit.furnaceCookTime)
			{
				icrafting.sendProgressBarUpdate(this, 0, robit.furnaceCookTime);
			}

			if(lastBurnTime != robit.furnaceBurnTime)
			{
				icrafting.sendProgressBarUpdate(this, 1, robit.furnaceBurnTime);
			}

			if(lastItemBurnTime != robit.currentItemBurnTime)
			{
				icrafting.sendProgressBarUpdate(this, 2, robit.currentItemBurnTime);
			}
		}

		lastCookTime = robit.furnaceCookTime;
		lastBurnTime = robit.furnaceBurnTime;
		lastItemBurnTime = robit.currentItemBurnTime;
	}

	@Override
	public void updateProgressBar(int i, int j)
	{
		if(i == 0)
		{
			robit.furnaceCookTime = j;
		}

		if(i == 1)
		{
			robit.furnaceBurnTime = j;
		}

		if(i == 2)
		{
			robit.currentItemBurnTime = j;
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = null;
		Slot currentSlot = (Slot)inventorySlots.get(slotID);

		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if(slotID == 2)
			{
				if(!mergeItemStack(slotStack, 3, 39, true))
				{
					return null;
				}
			}
			else if(slotID != 1 && slotID != 0)
			{
				if(FurnaceRecipes.smelting().getSmeltingResult(slotStack) != null)
				{
					if(!mergeItemStack(slotStack, 0, 1, false))
					{
						return null;
					}
				}
				else if(TileEntityFurnace.isItemFuel(slotStack))
				{
					if(!mergeItemStack(slotStack, 1, 2, false))
					{
						return null;
					}
				}
				else if(slotID >= 3 && slotID < 30)
				{
					if(!mergeItemStack(slotStack, 30, 39, false))
					{
						return null;
					}
				}
				else if(slotID >= 30 && slotID < 39 && !mergeItemStack(slotStack, 3, 30, false))
				{
					return null;
				}
			}
			else if(!mergeItemStack(slotStack, 3, 39, false))
			{
				return null;
			}

			if(slotStack.stackSize == 0)
			{
				currentSlot.putStack((ItemStack)null);
			}
			else {
				currentSlot.onSlotChanged();
			}

			if(slotStack.stackSize == stack.stackSize)
			{
				return null;
			}

			currentSlot.onPickupFromSlot(player, slotStack);
		}

		return stack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
		robit.closeInventory();
	}
}
