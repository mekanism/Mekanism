package net.uberkat.obsidian.common;

import ic2.api.IElectricItem;
import universalelectricity.implement.IItemElectric;
import net.uberkat.obsidian.api.*;
import net.minecraft.src.*;

public class ContainerPowerUnit extends Container
{
	private TileEntityPowerUnit tileEntity;
	
	public ContainerPowerUnit(InventoryPlayer inventory, TileEntityPowerUnit unit)
	{
		tileEntity = unit;
		addSlotToContainer(new SlotEnergy(unit, 0, 8, 8));
		addSlotToContainer(new SlotEnergy(unit, 1, 8, 40));
		
		int var3;
		
        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            addSlotToContainer(new Slot(inventory, var3, 8 + var3 * 18, 142));
        }
	}
	
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return tileEntity.isUseableByPlayer(par1EntityPlayer);
    }
    
    public ItemStack transferStackInSlot(int slotID)
    {
        ItemStack stack = null;
        Slot currentSlot = (Slot)inventorySlots.get(slotID);

        if(currentSlot != null && currentSlot.getHasStack())
        {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();

            if(slotStack.getItem() instanceof IEnergizedItem || slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem || slotStack.itemID == Item.redstone.shiftedIndex)
            {
	            if(slotID != 0 && slotID != 1)
	            {
	                if (!mergeItemStack(slotStack, 1, 2, false))
	                {
		                if (!mergeItemStack(slotStack, 0, 1, false))
		                {
		                    return null;
		                }
	                }
	            }
	            else if(slotID == 1)
	            {
	            	if(!mergeItemStack(slotStack, 0, 1, false))
	            	{
		            	if(!mergeItemStack(slotStack, 2, inventorySlots.size(), false))
		            	{
		            		return null;
		            	}
	            	}
	            }
	            else if(slotID == 0)
	            {
	            	if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
	            }
            }
            
            if (slotStack.stackSize == 0)
            {
                currentSlot.putStack((ItemStack)null);
            }
            else
            {
                currentSlot.onSlotChanged();
            }

            if (slotStack.stackSize == stack.stackSize)
            {
                return null;
            }

            currentSlot.onPickupFromSlot(slotStack);
        }

        return stack;
    }
}
