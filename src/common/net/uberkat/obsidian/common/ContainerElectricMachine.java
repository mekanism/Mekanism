package net.uberkat.obsidian.common;

import obsidian.api.IEnergizedItem;
import obsidian.api.IMachineUpgrade;
import ic2.api.IElectricItem;
import universalelectricity.implement.IItemElectric;
import net.minecraft.src.*;

public class ContainerElectricMachine extends Container
{
    private TileEntityElectricMachine tileEntity;

    public ContainerElectricMachine(InventoryPlayer inventory, TileEntityElectricMachine tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 56, 17));
        addSlotToContainer(new SlotEnergy(tentity, 1, 56, 53));
        addSlotToContainer(new SlotFurnace(inventory.player, tentity, 2, 116, 35));
        addSlotToContainer(new SlotMachineUpgrade(tentity, 3, 7, 7));
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
    
    public void onCraftGuiClosed(EntityPlayer entityplayer)
    {
		super.onCraftGuiClosed(entityplayer);
		tileEntity.closeChest();
    }

    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return tileEntity.isUseableByPlayer(par1EntityPlayer);
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    public ItemStack func_82846_b(EntityPlayer player, int slotID)
    {
        ItemStack stack = null;
        Slot currentSlot = (Slot)inventorySlots.get(slotID);

        if(currentSlot != null && currentSlot.getHasStack())
        {
            ItemStack slotStack = currentSlot.getStack();
            stack = slotStack.copy();

            if(slotID == 2)
            {
            	if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
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
	            	if(!mergeItemStack(slotStack, 4, inventorySlots.size(), false))
	            	{
	            		return null;
	            	}
	            }
            }
            else if(RecipeHandler.getOutput(slotStack, false, tileEntity.getRecipes()) != null)
    		{
            	if(slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3)
            	{
                    if (!mergeItemStack(slotStack, 0, 1, false))
	                {
	                    return null;
	                }
            	}
            	else {
	            	if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
            	}
    		}
            else if(slotStack.getItem() instanceof IMachineUpgrade)
            {
            	if(slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3)
            	{
            		if(!mergeItemStack(slotStack, 3, 4, false))
            		{
            			return null;
            		}
            	}
            	else {
            		if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
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

            currentSlot.func_82870_a(player, slotStack);
        }

        return stack;
    }
}
