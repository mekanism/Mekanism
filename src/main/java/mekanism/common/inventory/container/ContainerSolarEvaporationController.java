package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntitySolarEvaporationController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

public class ContainerSolarEvaporationController extends Container
{
	private TileEntitySolarEvaporationController tileEntity;

	public ContainerSolarEvaporationController(InventoryPlayer inventory, TileEntitySolarEvaporationController tentity)
	{
		tileEntity = tentity;

		addSlotToContainer(new Slot(tentity, 0, 28, 20));
		addSlotToContainer(new SlotOutput(tentity, 1, 28, 51));
		addSlotToContainer(new Slot(tentity, 2, 132, 20));
		addSlotToContainer(new SlotOutput(tentity, 3, 132, 51));

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

			if(slotID == 1 || slotID == 3)
			{
				if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(FluidContainerRegistry.isEmptyContainer(slotStack))
			{
				if(slotID != 2)
				{
					if(!mergeItemStack(slotStack, 2, 3, false))
					{
						return null;
					}
				}
				else if(slotID == 2)
				{
					if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(slotStack) && FluidContainerRegistry.getFluidForFilledItem(slotStack).getFluid() == FluidRegistry.WATER)
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
					if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else {
				if(slotID >= 4 && slotID <= 30)
				{
					if(!mergeItemStack(slotStack, 31, inventorySlots.size(), false))
					{
						return null;
					}
				}
				else if(slotID > 30)
				{
					if(!mergeItemStack(slotStack, 4, 30, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
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
