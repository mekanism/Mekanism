package mekanism.common.inventory.slot;

import mekanism.common.base.IElectricChest;
import mekanism.common.block.states.BlockStateMachine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotElectricChest extends Slot
{
	public SlotElectricChest(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		ItemStack itemstack = inventory.getStackInSlot(getSlotIndex());

		if(itemstack == null)
		{
			return true;
		}

		if(itemstack.getItem() instanceof IElectricChest)
		{
			if(BlockStateMachine.MachineType.get(itemstack) == BlockStateMachine.MachineType.ELECTRIC_CHEST)
			{
				return false;
			}
		}

		return true;
	}
}
