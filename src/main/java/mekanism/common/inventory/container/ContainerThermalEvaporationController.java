package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import mekanism.common.util.FluidContainerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public class ContainerThermalEvaporationController extends Container
{
	private TileEntityThermalEvaporationController tileEntity;

	public ContainerThermalEvaporationController(InventoryPlayer inventory, TileEntityThermalEvaporationController tentity)
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

			boolean isFluidContainer = FluidContainerUtils.isFluidContainer(slotStack);
			FluidStack containedFluid = FluidUtil.getFluidContained(slotStack);

			if(slotID == 1 || slotID == 3)
			{
				if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(isFluidContainer && containedFluid == null)
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
			else if(isFluidContainer && tileEntity.hasRecipe(containedFluid.getFluid()))
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
