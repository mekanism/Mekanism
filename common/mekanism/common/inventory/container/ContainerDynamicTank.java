package mekanism.common.inventory.container;

import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.tileentity.TileEntityDynamicTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class ContainerDynamicTank extends Container
{
    private TileEntityDynamicTank tileEntity;

    public ContainerDynamicTank(InventoryPlayer inventory, TileEntityDynamicTank tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 146, 20));
        addSlotToContainer(new SlotOutput(tentity, 1, 146, 51));
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
        
        tileEntity.playersUsing.add(inventory.player);
        tileEntity.openChest();
    }
    
    @Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
		super.onContainerClosed(entityplayer);
		tileEntity.playersUsing.remove(entityplayer);
		tileEntity.closeChest();
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
            
            if(FluidContainerRegistry.isEmptyContainer(slotStack) || FluidContainerRegistry.isFilledContainer(slotStack))
            {
            	if(slotID != 0 && slotID != 1)
            	{
            		if(!mergeItemStack(slotStack, 0, 1, false))
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
	        	if(slotID >= 2 && slotID <= 8)
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
}
