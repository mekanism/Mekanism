package mekanism.common.inventory.container;

import mekanism.common.tile.TileEntityLaserTractorBeam;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerLaserTractorBeam extends Container
{
	private TileEntityLaserTractorBeam tileEntity;

	public ContainerLaserTractorBeam(InventoryPlayer inventory, TileEntityLaserTractorBeam tentity)
	{
		tileEntity = tentity;
		int slotX;

		for(slotX = 0; slotX < 9; slotX++)
		{
			for(int slotY = 0; slotY < 3; slotY++)
			{
				addSlotToContainer(new Slot(tentity, slotX + slotY * 9, 8 + slotX * 18, 16 + slotY * 18));
			}
		}

		for(slotX = 0; slotX < 9; slotX++)
		{
			for(int slotY = 0; slotY < 3; slotY++)
			{
				addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, 84 + slotY * 18));
			}
		}

		for(slotX = 0; slotX < 9; slotX++)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
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
			
			if(slotID < 27)
			{
				if(!mergeItemStack(slotStack, 27, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(!mergeItemStack(slotStack, 0, 27, false))
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
}
