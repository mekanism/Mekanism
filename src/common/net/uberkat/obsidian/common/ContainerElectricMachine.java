package net.uberkat.obsidian.common;

import ic2.api.IElectricItem;
import universalelectricity.implement.IItemElectric;
import net.minecraft.src.*;
import net.uberkat.obsidian.api.IEnergizedItem;

public class ContainerElectricMachine extends Container
{
    private TileEntityElectricMachine tileEntity;

    public ContainerElectricMachine(InventoryPlayer inventory, TileEntityElectricMachine tentity)
    {
        this.tileEntity = tentity;
        this.addSlotToContainer(new Slot(tentity, 0, 56, 17));
        this.addSlotToContainer(new SlotEnergy(tentity, 1, 56, 53));
        this.addSlotToContainer(new SlotFurnace(inventory.player, tentity, 2, 116, 35));
        int var3;

        for (var3 = 0; var3 < 3; ++var3)
        {
            for (int var4 = 0; var4 < 9; ++var4)
            {
                this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.addSlotToContainer(new Slot(inventory, var3, 8 + var3 * 18, 142));
        }
    }

    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return this.tileEntity.isUseableByPlayer(par1EntityPlayer);
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    public ItemStack transferStackInSlot(int slotID)
    {
        ItemStack stack = null;
        Slot currentSlot = (Slot)inventorySlots.get(slotID);

        if(currentSlot != null && currentSlot.getHasStack())
        {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();

            if(slotID == 2)
            {
            	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
            	{
            		return null;
            	}
            }
        
        	else if(slotStack.getItem() instanceof IEnergizedItem || slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem || slotStack.itemID == Item.redstone.shiftedIndex)
            {
	            if(slotID != 1)
	            {
	                if (!mergeItemStack(slotStack, 1, 2, false))
	                {
	                	return null;
	                }
	            }
	            else if(slotID == 1)
	            {
	            	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), false))
	            	{
	            		return null;
	            	}
	            }
            }
            
            else if(RecipeHandler.getOutput(slotStack, false, tileEntity.getRecipes()) != null)
    		{
            	if(slotID != 0 && slotID != 1 && slotID != 2)
            	{
                    if (!mergeItemStack(slotStack, 0, 1, false))
	                {
	                    return null;
	                }
            	}
            	else {
	            	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
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
