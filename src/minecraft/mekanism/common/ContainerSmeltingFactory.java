package mekanism.common;

import ic2.api.IElectricItem;
import mekanism.api.Tier;
import mekanism.api.Tier.SmeltingFactoryTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import universalelectricity.core.implement.IItemElectric;

public class ContainerSmeltingFactory extends Container
{
    private TileEntitySmeltingFactory tileEntity;

    public ContainerSmeltingFactory(InventoryPlayer inventory, TileEntitySmeltingFactory tentity)
    {
        tileEntity = tentity;
        
        addSlotToContainer(new SlotMachineUpgrade(tentity, 0, 7, 7));
        addSlotToContainer(new SlotEnergy(tentity, 1, 7, 35));
        
        if(tileEntity.tier == SmeltingFactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 55 + (i*38);
        		
		        addSlotToContainer(new Slot(tentity, 2+i*2, xAxis, 13));
		        addSlotToContainer(new SlotFurnace(inventory.player, tentity, 3+i*2, xAxis, 57));
        	}
        }
        
        else if(tileEntity.tier == SmeltingFactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 35 + (i*26);
	        	
	        	addSlotToContainer(new Slot(tentity, 2+i*2, xAxis, 13));
	        	addSlotToContainer(new SlotFurnace(inventory.player, tentity, 3+i*2, xAxis, 57));
        	}
        }
        
        else if(tileEntity.tier == SmeltingFactoryTier.ELITE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 29 + (i*19);
	        	
	        	addSlotToContainer(new Slot(tentity, 2+i*2, xAxis, 13));
	        	addSlotToContainer(new SlotFurnace(inventory.player, tentity, 3+i*2, xAxis, 57));
        	}
        }
        
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

            if(isOutputSlot(slotID))
            {
            	if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
            	{
            		return null;
            	}
            }
        	else if(slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem || slotStack.itemID == Item.redstone.itemID)
            {
	            if(slotID != 1)
	            {
	                if(!mergeItemStack(slotStack, 1, 2, false))
	                {
	                	return null;
	                }
	            }
	            else if(slotID == 1)
	            {
	            	if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
	            	{
	            		return null;
	            	}
	            }
            }
            else if(FurnaceRecipes.smelting().getSmeltingResult(slotStack) != null)
    		{
            	if(!isInputSlot(slotID))
            	{
                    if(!mergeItemStack(slotStack, 2, 3, false))
	                {
	                    if(!mergeItemStack(slotStack, 4, 5, false))
	                    {
	                    	if(!mergeItemStack(slotStack, 6, 7, false))
	                    	{
	                    		if(tileEntity.tier != SmeltingFactoryTier.BASIC)
	                    		{
	                    			if(!mergeItemStack(slotStack, 8, 9, false))
	                    			{
	                    				if(!mergeItemStack(slotStack, 10, 11, false))
	                    				{
	                    					if(tileEntity.tier != SmeltingFactoryTier.ADVANCED)
	                    					{
	                    						if(!mergeItemStack(slotStack, 12, 13, false))
	                    						{
	                    							if(!mergeItemStack(slotStack, 14, 15, false))
	                    							{
	                    								return null;
	                    							}
	                    						}
	                    					}
	                    				}
	                    			}
	                    		}
	                    	}
	                    }
	                }
            	}
            	else {
            		if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
            		{
            			return null;
            		}
            	}
    		}
            else if(slotStack.getItem() instanceof ItemMachineUpgrade)
            {
            	if(slotID != 0)
            	{
            		if(!mergeItemStack(slotStack, 0, 1, false))
            		{
            			return null;
            		}
            	}
            	else {
            		if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
            		{
            			return null;
            		}
            	}
            }
            else {
            	int slotEnd = tileEntity.inventory.length;
            	
            	if(slotID >= slotEnd && slotID <= (slotEnd+26))
            	{
            		if(!mergeItemStack(slotStack, (slotEnd+27), inventorySlots.size(), false))
            		{
            			return null;
            		}
            	}
            	else if(slotID > (slotEnd+26))
            	{
            		if(!mergeItemStack(slotStack, slotEnd, (slotEnd+26), false))
            		{
            			return null;
            		}
            	}
            	else {
            		if(!mergeItemStack(slotStack, slotEnd, inventorySlots.size(), true))
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
    
    public boolean isInputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.BASIC)
    		return slot == 2 || slot == 4 || slot == 6;
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.ADVANCED)
    		return slot == 2 || slot == 4 || slot == 6 || slot == 8 || slot == 10;
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.ELITE)
    		return slot == 2 || slot == 4 || slot == 6 || slot == 8 || slot == 12 || slot == 14;
    	
    	return false;
    }
    
    public boolean isOutputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.BASIC)
    		return slot == 3 || slot == 5 || slot == 7;
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.ADVANCED)
    		return slot == 3 || slot == 5 || slot == 7 || slot == 9 || slot == 11;
    	if(tileEntity.tier == Tier.SmeltingFactoryTier.ELITE)
    		return slot == 3 || slot == 5 || slot == 7 || slot == 9 || slot == 11 || slot == 13 || slot == 15;
    	
    	return false;
    }
}
