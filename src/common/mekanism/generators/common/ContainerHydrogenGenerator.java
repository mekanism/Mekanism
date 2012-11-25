package mekanism.generators.common;

import ic2.api.IElectricItem;
import universalelectricity.core.implement.IItemElectric;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.common.SlotEnergy;
import net.minecraft.src.*;

public class ContainerHydrogenGenerator extends Container
{
    private TileEntityHydrogenGenerator tileEntity;

    public ContainerHydrogenGenerator(InventoryPlayer inventory, TileEntityHydrogenGenerator tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 17, 35));
        addSlotToContainer(new SlotEnergy(tentity, 1, 143, 35));
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
            
        	if(slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem)
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
            		if(((IStorageTank)slotStack.getItem()).gasType() == EnumGas.HYDROGEN)
            		{
	                    if (!mergeItemStack(slotStack, 0, 1, false))
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
        	else if(slotStack.itemID == Item.bucketEmpty.shiftedIndex)
        	{
        		if(!mergeItemStack(slotStack, 2, inventorySlots.size(), true))
        		{
        			return null;
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
