package mekanism.common.inventory.container;

import ic2.api.item.IElectricItem;
import mekanism.common.inventory.slot.SlotEnergy;
import mekanism.common.inventory.slot.SlotEnergy.SlotCharge;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.tileentity.TileEntityEnergyCube;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;

public class ContainerEnergyCube extends Container
{
	private TileEntityEnergyCube tileEntity;
	
	public ContainerEnergyCube(InventoryPlayer inventory, TileEntityEnergyCube unit)
	{
		tileEntity = unit;
		addSlotToContainer(new SlotCharge(unit, 0, 8, 8));
		addSlotToContainer(new SlotDischarge(unit, 1, 8, 40));
		
		int slotX;
		
        for(slotX = 0; slotX < 3; ++slotX)
        {
            for(int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 84 + slotX * 18));
            }
        }

        for(slotX = 0; slotX < 9; ++slotX)
        {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 142));
        }
        
        tileEntity.openChest();
        tileEntity.playersUsing.add(inventory.player);
    }
    
	@Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
		super.onContainerClosed(entityplayer);
		tileEntity.closeChest();
		tileEntity.playersUsing.remove(entityplayer);
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

            if(slotStack.getItem() instanceof IElectricItem || slotStack.getItem() instanceof IItemElectric || slotStack.itemID == Item.redstone.itemID)
            {
            	if(slotStack.itemID == Item.redstone.itemID)
            	{
            		if(slotID != 1)
            		{
            			if(!mergeItemStack(slotStack, 1, 2, false))
            			{
            				return null;
            			}
            		}
            		else {
            			if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
            			{
            				return null;
            			}
            		}
            	}
            	else {
		            if(slotID != 1 && slotID != 0)
		            {
		            	if(ChargeUtils.canBeDischarged(slotStack))
		            	{
			                if(!mergeItemStack(slotStack, 1, 2, false))
			                {
			                	if(canTransfer(slotStack))
			                	{
			                		if(!mergeItemStack(slotStack, 0, 1, false))
			                		{
			                			return null;
			                		}
			                	}
			                }
		            	}
		            	else if(canTransfer(slotStack))
		            	{
		              		if(!mergeItemStack(slotStack, 0, 1, false))
	                		{
	                			return null;
	                		}
		            	}
		            }
		            else if(slotID == 1)
		            {
		            	if(canTransfer(slotStack))
		            	{
			            	if(!mergeItemStack(slotStack, 0, 1, false))
			            	{
				            	if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
				            	{
				            		return null;
				            	}
			            	}
		            	}
		            	else {
		            	  	if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
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
	        	else {
            		if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
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
	
	private boolean canTransfer(ItemStack slotStack)
	{
		return (slotStack.getItem() instanceof IItemElectric && 
				((IItemElectric)slotStack.getItem()).recharge(slotStack, 1, false) != 0) || 
				(slotStack.getItem() instanceof IElectricItem && (!(slotStack.getItem() instanceof IItemElectric) || 
						((IItemElectric)slotStack.getItem()).discharge(slotStack, 1, false) == 0));
	}
}
