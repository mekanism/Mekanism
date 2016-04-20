package mekanism.common.inventory.slot;

import mekanism.common.block.BlockMachine.MachineType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotPersonalChest extends Slot
{
	public SlotPersonalChest(IInventory inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		if(inventory.getStackInSlot(getSlotIndex()) == null)
		{
			return false;
		}
		
		return MachineType.get(inventory.getStackInSlot(getSlotIndex())) != MachineType.PERSONAL_CHEST;
	}
}
