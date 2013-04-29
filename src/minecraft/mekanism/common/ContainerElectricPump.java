package mekanism.common;

import ic2.api.IElectricItem;
import mekanism.common.SlotEnergy.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import universalelectricity.core.item.IItemElectric;

public class ContainerElectricPump extends Container
{
    private TileEntityElectricPump tileEntity;

    public ContainerElectricPump(InventoryPlayer inventory, TileEntityElectricPump tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 28, 20));
        addSlotToContainer(new SlotOutput(tentity, 1, 28, 51));
        addSlotToContainer(new SlotDischarge(tentity, 2, 143, 35));
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
    public void onCraftGuiClosed(EntityPlayer entityplayer)
    {
		super.onCraftGuiClosed(entityplayer);
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
            
            if((slotStack.getItem() instanceof IElectricItem && ((IElectricItem)slotStack.getItem()).canProvideEnergy(slotStack)) || (slotStack.getItem() instanceof IItemElectric && ((IItemElectric)slotStack.getItem()).getProvideRequest(slotStack).amperes != 0) || slotStack.itemID == Item.redstone.itemID)
            {
	            if(slotID != 2)
	            {
	                if (!mergeItemStack(slotStack, 2, 3, false))
	                {
	                	return null;
	                }
	            }
	            else if(slotID == 2)
	            {
	            	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
	            }
            }
            else if(LiquidContainerRegistry.isEmptyContainer(slotStack))
            {
            	if(slotID != 0)
            	{
            		if(!mergeItemStack(slotStack, 0, 1, false))
            		{
            			return null;
            		}
            	}
            	else if(slotID == 0)
            	{
            	 	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
            	}
            }
            else if(slotID == 1)
            {
              	if(!mergeItemStack(slotStack, 3, inventorySlots.size(), true))
            	{
            		return null;
            	}
            }
            else {
	        	if(slotID >= 3 && slotID <= 29)
	        	{
	        		if(!mergeItemStack(slotStack, 30, inventorySlots.size(), false))
	        		{
	        			return null;
	        		}
	        	}
	        	else if(slotID > 28)
	        	{
	        		if(!mergeItemStack(slotStack, 3, 29, false))
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
