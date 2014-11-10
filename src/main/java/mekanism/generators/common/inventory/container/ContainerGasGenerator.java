package mekanism.generators.common.inventory.container;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.IGasItem;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.util.ChargeUtils;
import mekanism.generators.common.tile.TileEntityGasGenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGasGenerator extends Container
{
	private TileEntityGasGenerator tileEntity;

	public ContainerGasGenerator(InventoryPlayer inventory, TileEntityGasGenerator tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new Slot(tentity, 0, 17, 35));
		addSlotToContainer(new SlotCharge(tentity, 1, 143, 35));
		int slotX;

		for(slotX = 0; slotX < 3; ++slotX)
		{
			for(int slotY = 0; slotY < 9; ++slotY)
			{
				addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
			}
		}

		for(slotX = 0; slotX < 9; ++slotX)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
		}

		tileEntity.openInventory();
		tileEntity.open(inventory.player);
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
		tileEntity.closeInventory();
		tileEntity.close(entityplayer);
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

			if(ChargeUtils.canBeCharged(slotStack))
			{
				if(slotID != 1)
				{
					if(!mergeItemStack(slotStack, 1, 2, false))
					{
						return null;
					}
				}
				else if(slotID == 1)
				{
					if(!mergeItemStack(slotStack, 2, inventorySlots.size(), false))
					{
						return null;
					}
				}
			}
			else if(slotStack.getItem() instanceof IGasItem)
			{
				if(slotID != 0 && slotID != 1)
				{
					if(((IGasItem)slotStack.getItem()).getGas(slotStack) != null && ((IGasItem)slotStack.getItem()).getGas(slotStack).getGas() == GasRegistry.getGas("hydrogen"))
					{
						if(!mergeItemStack(slotStack, 0, 1, false))
						{
							return null;
						}
					}
				}
				else {
					if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else {
				if(slotID >= 2 && slotID <= 28)
				{
					if(!mergeItemStack(slotStack, 29, inventorySlots.size(), false))
					{
						return null;
					}
				}
				else if(slotID > 28)
				{
					if(!mergeItemStack(slotStack, 2, 28, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
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
