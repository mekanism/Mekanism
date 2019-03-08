package mekanism.common.inventory.container;

import mekanism.api.infuse.InfuseRegistry;
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

import javax.annotation.Nonnull;
import java.util.Arrays;

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
	public boolean canInteractWith(@Nonnull EntityPlayer entityplayer)
	{
		return tileEntity.isUsableByPlayer(entityplayer);
	}

	@Nonnull
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot currentSlot = inventorySlots.get(slotID);
		
		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if(isOutputSlot(slotID))
			{
				if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(slotID != 1 && slotID != 2 && isProperMachine(slotStack) && !slotStack.isItemEqual(tileEntity.getMachineStack()))
			{
				if(!mergeItemStack(slotStack, 1, 2, false))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(slotID == 2)
			{
				if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(tileEntity.getRecipeType().getAnyRecipe(slotStack, inventorySlots.get(4).getStack(), tileEntity.gasTank.getGasType(), tileEntity.infuseStored) != null)
			{
				if(!isInputSlot(slotID))
				{
					if(!mergeItemStack(slotStack, 4, 4+tileEntity.tier.processes, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(ChargeUtils.canBeDischarged(slotStack))
			{
				if(slotID != 0)
				{
					if(!mergeItemStack(slotStack, 0, 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else
				{
					if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(tileEntity.getRecipeType().getItemGas(slotStack) != null)
			{
				if(slotID >= tileEntity.inventory.size()-1)
				{
					if(!mergeItemStack(slotStack, 3, 4, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(tileEntity.getRecipeType() == RecipeType.INFUSING && InfuseRegistry.getObject(slotStack) != null && (tileEntity.infuseStored.type == null || tileEntity.infuseStored.type == InfuseRegistry.getObject(slotStack).type))
			{
				if(slotID >= tileEntity.inventory.size()-1)
				{
					if(!mergeItemStack(slotStack, 3, 4, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, tileEntity.inventory.size()-1, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else {
				int slotEnd = tileEntity.inventory.size()-1;

				if(slotID >= slotEnd && slotID <= (slotEnd+26))
				{
					if(!mergeItemStack(slotStack, (slotEnd+27), inventorySlots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if(slotID > (slotEnd+26))
				{
					if(!mergeItemStack(slotStack, slotEnd, (slotEnd+26), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, slotEnd, inventorySlots.size(), true))
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

	public boolean isProperMachine(ItemStack itemStack)
	{
		if(!itemStack.isEmpty() && itemStack.getItem() instanceof ItemBlockMachine)
		{
			return Arrays.stream(RecipeType.values()).findFirst().filter(type -> itemStack.isItemEqual(type.getStack())).isPresent();
		}

		return false;
	}

	public boolean isInputSlot(int slot)
	{
		return slot >= 4 && slot < 4+tileEntity.tier.processes;
	}

	public boolean isOutputSlot(int slot)
	{
		return slot >= 4+tileEntity.tier.processes && slot < 4+tileEntity.tier.processes*2;
	}
}
