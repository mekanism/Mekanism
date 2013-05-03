package mekanism.common;

import ic2.api.item.IElectricItem;
import mekanism.common.SlotEnergy.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;

public class ContainerElectricChest extends Container
{
    private TileEntityElectricChest tileEntity;
    private IInventory itemInventory;
    private boolean isBlock;

    public ContainerElectricChest(InventoryPlayer inventory, TileEntityElectricChest tentity, IInventory inv, boolean b)
    {
        tileEntity = tentity;
        itemInventory = inv;
        isBlock = b;
        
        if(isBlock)
        {
        	tileEntity.openChest();
        	tileEntity.playersUsing.add(inventory.player);
        }
        else {
        	itemInventory.openChest();
        }
        
        for(int slotY = 0; slotY < 6; slotY++)
        {
            for(int slotX = 0; slotX < 9; slotX++)
            {
                addSlotToContainer(new SlotElectricChest(getInv(), slotX + slotY * 9, 8 + slotX * 18, 26 + slotY * 18));
            }
        }

        addSlotToContainer(new SlotDischarge(getInv(), 54, 180, 11));
        
        int slotX;

        for(slotX = 0; slotX < 3; ++slotX)
        {
            for(int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new SlotElectricChest(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 148 + slotX * 18));
            }
        }

        for(slotX = 0; slotX < 9; ++slotX)
        {
            addSlotToContainer(new SlotElectricChest(inventory, slotX, 8 + slotX * 18, 206));
        }
    }
    
    public IInventory getInv()
    {
    	if(isBlock)
    	{
    		return tileEntity;
    	}
    	else {
    		return itemInventory;
    	}
    }
    
    @Override
    public void onCraftGuiClosed(EntityPlayer entityplayer)
    {
		super.onCraftGuiClosed(entityplayer);
		
		if(isBlock)
		{
			tileEntity.closeChest();
			tileEntity.playersUsing.remove(entityplayer);
		}
		else {
			itemInventory.closeChest();
		}
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
    	if(isBlock)
    	{
    		return tileEntity.isUseableByPlayer(entityplayer);
    	}
    	
    	return true;
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
	            if(slotID != 54)
	            {
	                if(!mergeItemStack(slotStack, 54, 55, false))
	                {
	                	return null;
	                }
	            }
	            else if(slotID == 54)
	            {
	            	if(!mergeItemStack(slotStack, 55, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
	            }
            }
            else {
            	if(slotID >= 55 && slotID <= 81)
            	{
            		if(!mergeItemStack(slotStack, 82, inventorySlots.size(), false))
            		{
            			return null;
            		}
            	}
            	else if(slotID > 81)
            	{
            		if(!mergeItemStack(slotStack, 55, 81, false))
            		{
            			return null;
            		}
            	}
            	else {
            		if(!mergeItemStack(slotStack, 55, inventorySlots.size(), true))
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
