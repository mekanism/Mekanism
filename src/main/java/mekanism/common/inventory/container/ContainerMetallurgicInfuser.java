package mekanism.common.inventory.container;

import mekanism.api.infuse.InfuseRegistry;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.util.ChargeUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMetallurgicInfuser extends Container
{
	private TileEntityMetallurgicInfuser tileEntity;

	public ContainerMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new SlotDischarge(tentity, 0, 143, 35));
		addSlotToContainer(new Slot(tentity, 1, 17, 35));
		addSlotToContainer(new Slot(tentity, 2, 51, 43));
		addSlotToContainer(new SlotOutput(tentity, 3, 109, 43));
		
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

			if(slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3)
			{
				if(InfuseRegistry.getObject(slotStack) != null && (tileEntity.infuseStored.type == null || tileEntity.infuseStored.type == InfuseRegistry.getObject(slotStack).type))
				{
					if(!mergeItemStack(slotStack, 1, 2, false))
					{
						return null;
					}
				}
				else if(ChargeUtils.canBeDischarged(slotStack))
				{
					if(!mergeItemStack(slotStack, 0, 1, false))
					{
						return null;
					}
				}
				else if(isInputItem(slotStack))
				{
					if(!mergeItemStack(slotStack, 2, 3, false))
					{
						return null;
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
			}
			else {
				if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
				{
					return null;
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

	public boolean isInputItem(ItemStack itemStack)
	{
		if(tileEntity.infuseStored.type != null)
		{
			if(RecipeHandler.getMetallurgicInfuserRecipe(new InfusionInput(tileEntity.infuseStored, itemStack)) != null)
			{
				return true;
			}
		}
		else {
			for(Object obj : Recipe.METALLURGIC_INFUSER.get().keySet())
			{
				InfusionInput input = (InfusionInput)obj;
				if(input.inputStack.isItemEqual(itemStack))
				{
					return true;
				}
			}
		}

		return false;
	}
}
