package mekanism.common;

import ic2.api.IElectricItem;
import universalelectricity.implement.IItemElectric;
import mekanism.api.*;
import mekanism.api.IStorageTank.EnumGas;
import net.minecraft.src.*;

public class ContainerGasTank extends Container
{
	private TileEntityGasTank tileEntity;
	
	public ContainerGasTank(InventoryPlayer inventory, TileEntityGasTank tentity)
	{
		tileEntity = tentity;
		addSlotToContainer(new SlotStorageTank(tentity, EnumGas.NONE, true, 0, 8, 8));
		addSlotToContainer(new SlotStorageTank(tentity, EnumGas.NONE, true, 1, 8, 40));
		
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

            if(slotStack.getItem() instanceof IStorageTank)
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

            currentSlot.onPickupFromSlot(player, slotStack);
        }

        return stack;
    }
}
