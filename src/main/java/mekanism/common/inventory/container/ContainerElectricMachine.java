package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.ItemStackInput;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerElectricMachine extends Container
{
	private TileEntityElectricMachine tileEntity;

	public ContainerElectricMachine(InventoryPlayer inventory, TileEntityElectricMachine tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new Slot(tentity, 0, 56, 17));
		addSlotToContainer(new SlotDischarge(tentity, 1, 56, 53));
		addSlotToContainer(new SlotOutput(tentity, 2, 116, 35));
		
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
		return tileEntity.isUsableByPlayer(entityplayer);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotID)
	{
		ItemStack stack = ItemStack.EMPTY;
		Slot currentSlot = (Slot)inventorySlots.get(slotID);

		if(currentSlot != null && currentSlot.getHasStack())
		{
			ItemStack slotStack = currentSlot.getStack();
			stack = slotStack.copy();

			if(slotID == 2)
			{
				if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(RecipeHandler.getRecipe(new ItemStackInput(slotStack), tileEntity.getRecipes()) != null)
			{
				if(slotID != 0 && slotID != 1 && slotID != 2)
				{
					if(!mergeItemStack(slotStack, 0, 1, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else if(ChargeUtils.canBeDischarged(slotStack))
			{
				if(slotID != 1)
				{
					if(!mergeItemStack(slotStack, 1, 2, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if(slotID == 1)
				{
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
					{
						return ItemStack.EMPTY;
					}
				}
			}
			else {
				if(slotID >= 3 && slotID <= 29)
				{
					if(!mergeItemStack(slotStack, 30, inventorySlots.size(), false))
					{
						return ItemStack.EMPTY;
					}
				}
				else if(slotID > 29)
				{
					if(!mergeItemStack(slotStack, 3, 29, false))
					{
						return ItemStack.EMPTY;
					}
				}
				else {
					if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
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
