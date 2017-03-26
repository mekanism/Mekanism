package mekanism.common.inventory.container;

import invtweaks.api.container.ChestContainer;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.inventory.slot.SlotPersonalChest;
import mekanism.common.tile.TileEntityPersonalChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

@ChestContainer(isLargeChest=true)
public class ContainerPersonalChest extends Container
{
	private TileEntityPersonalChest tileEntity;
	private IInventory itemInventory;
	private boolean isBlock;

	public ContainerPersonalChest(InventoryPlayer inventory, TileEntityPersonalChest tentity, IInventory inv, boolean b)
	{
		tileEntity = tentity;
		itemInventory = inv;
		isBlock = b;

		if(isBlock)
		{
			tileEntity.open(inventory.player);
			tileEntity.openInventory(inventory.player);
		}
		else {
			itemInventory.openInventory(inventory.player);
		}

		for(int slotY = 0; slotY < 6; slotY++)
		{
			for(int slotX = 0; slotX < 9; slotX++)
			{
				addSlotToContainer(new SlotPersonalChest(getInv(), slotX + slotY * 9, 8 + slotX * 18, 26 + slotY * 18));
			}
		}

		int slotX;

		for(slotX = 0; slotX < 3; ++slotX)
		{
			for(int slotY = 0; slotY < 9; ++slotY)
			{
				addSlotToContainer(new SlotPersonalChest(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 148 + slotX * 18));
			}
		}

		for(slotX = 0; slotX < 9; ++slotX)
		{
			addSlotToContainer(new SlotPersonalChest(inventory, slotX, 8 + slotX * 18, 206));
		}
	}

	public IInventory getInv()
	{
		if(isBlock)
		{
			return tileEntity;
		}
		else {
			return itemInventory;
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);

		if(isBlock)
		{
			tileEntity.close(entityplayer);
			tileEntity.closeInventory(entityplayer);
		}
		else {
			itemInventory.closeInventory(entityplayer);
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		if(isBlock)
		{
			return tileEntity.isUsableByPlayer(entityplayer);
		}

		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = null;
		Slot currentSlot = inventorySlots.get(slotID);

		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if(slotID < 54)
			{
				if(!mergeItemStack(slotStack, 54, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(!mergeItemStack(slotStack, 0, 54, false))
			{
				return null;
			}

			if(slotStack.getCount() == 0)
			{
				currentSlot.putStack((ItemStack)null);
			}
			else {
				currentSlot.onSlotChanged();
			}

			if(slotStack.getCount() == stack.getCount())
			{
				return null;
			}

			currentSlot.onTake(player, slotStack);
		}

		return stack;
	}

	@Override
	public ItemStack slotClick(int slotNumber, int destSlot, ClickType clickType, EntityPlayer player)
	{
		if(destSlot >= 0 && destSlot < 9)
		{
			ItemStack itemStack = player.inventory.getStackInSlot(destSlot);
			
			if(itemStack != null && MachineType.get(itemStack) == MachineType.PERSONAL_CHEST)
			{
				return null;
			}
		}

		return super.slotClick(slotNumber, destSlot, clickType, player);
	}
}
