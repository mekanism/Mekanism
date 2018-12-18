package mekanism.common.inventory.container;

import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntitySecurityDesk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSecurityDesk extends Container
{
	private TileEntitySecurityDesk tileEntity;

	public ContainerSecurityDesk(InventoryPlayer inventory, TileEntitySecurityDesk tentity)
	{
		tileEntity = tentity;
		
		addSlotToContainer(new Slot(tentity, 0, 146, 18));
		addSlotToContainer(new Slot(tentity, 1, 146, 97));

		int slotY;

		for(slotY = 0; slotY < 3; slotY++)
		{
			for(int slotX = 0; slotX < 9; slotX++)
			{
				addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, 148 + slotY * 18));
			}
		}

		for(slotY = 0; slotY < 9; slotY++)
		{
			addSlotToContainer(new Slot(inventory, slotY, 8 + slotY * 18, 206));
		}

		tileEntity.open(inventory.player);
		tileEntity.openInventory(inventory.player);
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		tileEntity.close(entityplayer);
		tileEntity.closeInventory(entityplayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return tileEntity.isUsableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot currentSlot = inventorySlots.get(slotID);

		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if(slotStack.getItem() instanceof ISecurityItem && ((ISecurityItem)slotStack.getItem()).hasSecurity(slotStack))
			{
				if(slotID != 0 && slotID != 1)
				{
					if(!mergeItemStack(slotStack, 1, 2, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 29, inventorySlots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else {
				if(slotID >= 2 && slotID <= 28)
				{
					if(!mergeItemStack(slotStack, 29, inventorySlots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if(slotID > 28)
				{
					if(!mergeItemStack(slotStack, 2, 28, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}

			if(slotStack.getCount() == 0)
			{
				currentSlot.putStack(ItemStack.EMPTY);
			}
			else {
				currentSlot.onSlotChanged();
			}

			if(slotStack.getCount() == stack.getCount())
			{
				return ItemStack.EMPTY;
			}

			currentSlot.onTake(player, slotStack);
		}

		return stack;
	}
}
