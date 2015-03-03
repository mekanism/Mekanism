package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityElectricPump;
import mekanism.common.util.ChargeUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class ContainerElectricPump extends Container
{
	private TileEntityElectricPump tileEntity;

	public ContainerElectricPump(InventoryPlayer inventory, TileEntityElectricPump tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new Slot(tentity, 0, 28, 20));
		addSlotToContainer(new SlotOutput(tentity, 1, 28, 51));
		addSlotToContainer(new SlotDischarge(tentity, 2, 143, 35));
		
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

		tileEntity.open(inventory.player);
		tileEntity.openInventory();
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		tileEntity.close(entityplayer);
		tileEntity.closeInventory();
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUseableByPlayer(entityplayer);
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

			if(ChargeUtils.canBeDischarged(slotStack))
			{
				if(slotID != 2)
				{
					if(!mergeItemStack(slotStack, 2, 3, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else if(FluidContainerRegistry.isEmptyContainer(slotStack))
			{
				if(slotID != 0)
				{
					if(!mergeItemStack(slotStack, 0, 1, false))
					{
						return null;
					}
				}
				else if(slotID == 0)
				{
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else if(slotID == 1)
			{
				if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else {
				if(slotID >= 3 && slotID <= 29)
				{
					if(!mergeItemStack(slotStack, 30, inventorySlots.size(), false))
					{
						return null;
					}
				}
				else if(slotID > 29)
				{
					if(!mergeItemStack(slotStack, 3, 29, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
					{
						return null;
					}
				}
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
}
