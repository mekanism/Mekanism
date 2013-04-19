package mekanism.common;

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
    public boolean canTakeStack(EntityPlayer par1EntityPlayer)
    {
		ItemStack itemstack = inventory.getStackInSlot(getSlotIndex());
		
		if(itemstack == null)
		{
			return true;
		}
		
		if(itemstack.getItem() instanceof IElectricChest)
		{
			if(((IElectricChest)itemstack.getItem()).isElectricChest(itemstack))
			{
				return false;
			}
		}
		
		return true;
    }
}
