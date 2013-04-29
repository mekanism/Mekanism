package mekanism.generators.common;

import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.common.SlotEnergy;
import mekanism.common.SlotEnergy.SlotCharge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import universalelectricity.core.item.IItemElectric;

public class ContainerHydrogenGenerator extends Container
{
    private TileEntityHydrogenGenerator tileEntity;

    public ContainerHydrogenGenerator(InventoryPlayer inventory, TileEntityHydrogenGenerator tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 17, 35));
        addSlotToContainer(new SlotCharge(tentity, 1, 143, 35));
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
            
            if((slotStack.getItem() instanceof IItemElectric && ((IItemElectric)slotStack.getItem()).getReceiveRequest(slotStack).amperes != 0) || slotStack.getItem() instanceof IElectricItem)
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
	            	if(!mergeItemStack(slotStack, 2, inventorySlots.size(), false))
	            	{
	            		return null;
	            	}
	            }
            }
        	else if(slotStack.getItem() instanceof IStorageTank)
        	{
            	if(slotID != 0 && slotID != 1)
            	{
            		if(((IStorageTank)slotStack.getItem()).getGasType(slotStack) == EnumGas.HYDROGEN)
            		{
	                    if(!mergeItemStack(slotStack, 0, 1, false))
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
}
