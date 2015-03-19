package mekanism.common.inventory.container;

import mekanism.common.Tier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.tile.TileEntityFactory;
import mekanism.common.util.ChargeUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFactory extends Container
{
	private TileEntityFactory tileEntity;

	public ContainerFactory(InventoryPlayer inventory, TileEntityFactory tentity)
	{
		tileEntity = tentity;

		addSlotToContainer(new SlotDischarge(tentity, 1, 7, 13));
		addSlotToContainer(new Slot(tentity, 2, 180, 75));
		addSlotToContainer(new Slot(tentity, 3, 180, 112));
		addSlotToContainer(new Slot(tentity, 4, 7, 57));

		if(tileEntity.tier == FactoryTier.BASIC)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 55 + (i*38);

				addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
			}

			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 55 + (i*38);

				addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
			}
		}
		else if(tileEntity.tier == FactoryTier.ADVANCED)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 35 + (i*26);

				addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
			}

			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 35 + (i*26);

				addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
			}
		}
		else if(tileEntity.tier == FactoryTier.ELITE)
		{
			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 29 + (i*19);

				addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
			}

			for(int i = 0; i < tileEntity.tier.processes; i++)
			{
				int xAxis = 29 + (i*19);

				addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
			}
		}

		int slotY;

		for(slotY = 0; slotY < 3; slotY++)
		{
			for(int slotX = 0; slotX < 9; slotX++)
			{
				addSlotToContainer(new Slot(inventory, slotX + slotY * 9 + 9, 8 + slotX * 18, 95 + slotY * 18));
			}
		}

		for(int slotX = 0; slotX < 9; slotX++)
		{
			addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 153));
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

			if(isOutputSlot(slotID))
			{
				if(!mergeItemStack(slotStack, tileEntity.inventory.length-1, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(slotID != 1 && slotID != 2 && isProperMachine(slotStack) && !slotStack.isItemEqual(tileEntity.getMachineStack()))
			{
				if(!mergeItemStack(slotStack, 1, 2, false))
				{
					return null;
				}
			}
			else if(slotID == 2)
			{
				if(!mergeItemStack(slotStack, tileEntity.inventory.length-1, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(tileEntity.recipeType.getAnyRecipe(slotStack, tileEntity.gasTank.getGasType()) != null)
			{
				if(!isInputSlot(slotID))
				{
					if(!mergeItemStack(slotStack, 4, 4+tileEntity.tier.processes, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, tileEntity.inventory.length-1, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else if(ChargeUtils.canBeDischarged(slotStack))
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
					if(!mergeItemStack(slotStack, tileEntity.inventory.length-1, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else if(tileEntity.recipeType.getItemGas(slotStack) != null)
			{
				if(slotID > tileEntity.inventory.length-1)
				{
					if(!mergeItemStack(slotStack, 3, 4, false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, tileEntity.inventory.length-1, inventorySlots.size(), true))
					{
						return null;
					}
				}
			}
			else {
				int slotEnd = tileEntity.inventory.length-1;

				if(slotID >= slotEnd && slotID <= (slotEnd+26))
				{
					if(!mergeItemStack(slotStack, (slotEnd+27), inventorySlots.size(), false))
					{
						return null;
					}
				}
				else if(slotID > (slotEnd+26))
				{
					if(!mergeItemStack(slotStack, slotEnd, (slotEnd+26), false))
					{
						return null;
					}
				}
				else {
					if(!mergeItemStack(slotStack, slotEnd, inventorySlots.size(), true))
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

	public boolean isProperMachine(ItemStack itemStack)
	{
		if(itemStack != null && itemStack.getItem() instanceof ItemBlockMachine)
		{
			for(RecipeType type : RecipeType.values())
			{
				return itemStack.isItemEqual(type.getStack());
			}
		}

		return false;
	}

	public boolean isInputSlot(int slot)
	{
		if(tileEntity.tier == Tier.FactoryTier.BASIC)
			return slot >= 4 && slot <= 6;
		if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
			return slot >= 4 && slot <= 8;
		if(tileEntity.tier == Tier.FactoryTier.ELITE)
			return slot >= 4 && slot <= 10;

		return false;
	}

	public boolean isOutputSlot(int slot)
	{
		if(tileEntity.tier == Tier.FactoryTier.BASIC)
			return slot >= 7 && slot <= 9;
		if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
			return slot >= 9 && slot <= 13;
		if(tileEntity.tier == Tier.FactoryTier.ELITE)
			return slot >= 11 && slot <= 17;

		return false;
	}
}
