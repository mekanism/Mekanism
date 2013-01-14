package mekanism.common;

import ic2.api.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.implement.IItemElectric;

public class ContainerEnergyCube extends Container
{
	private TileEntityEnergyCube tileEntity;
	
	public ContainerEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube unit)
	{
		tileEntity = unit;
		addSlotToContainer(new SlotEnergy(unit, 0, 8, 8));
		addSlotToContainer(new SlotEnergy(unit, 1, 8, 40));
		
		int slotX;
		
        for (slotX = 0; slotX < 3; ++slotX)
        {
            for (int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
            }
        }

        for (slotX = 0; slotX < 9; ++slotX)
        {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
        }
        
        tileEntity.openChest();
    }
    
	@Override
    public void onCraftGuiClosed(EntityPlayer entityplayer)
    {
		super.onCraftGuiClosed(entityplayer);
		tileEntity.closeChest();
    }
	
	@Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return tileEntity.isUseableByPlayer(par1EntityPlayer);
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

            if(slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem || slotStack.itemID == Item.redstone.shiftedIndex)
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
            else {
	        	if(slotID >= 2 && slotID <= 28)
	        	{
	        		if(!mergeItemStack(slotStack, 29, inventorySlots.size(), false))
	        		{
	        			return null;
	        		}
	        	}
	        	else if(slotID > 28)
	        	{
	        		if(!mergeItemStack(slotStack, 2, 28, false))
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

            currentSlot.onPickupFromSlot(player, slotStack);
        }

        return stack;
    }
}
