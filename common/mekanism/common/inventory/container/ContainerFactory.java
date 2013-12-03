package mekanism.common.inventory.container;

import mekanism.common.Tier;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.block.BlockMachine.MachineType;
import mekanism.common.inventory.slot.SlotMachineUpgrade;
import mekanism.common.inventory.slot.SlotOutput;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemMachineUpgrade;
import mekanism.common.tileentity.TileEntityFactory;
import mekanism.common.util.ChargeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFactory extends Container
{
    private TileEntityFactory tileEntity;

    public ContainerFactory(InventoryPlayer inventory, TileEntityFactory tentity)
    {
        tileEntity = tentity;
        
        addSlotToContainer(new SlotMachineUpgrade(tentity, 0, 180, 11));
        addSlotToContainer(new SlotDischarge(tentity, 1, 7, 13));
        addSlotToContainer(new Slot(tentity, 2, 180, 75));
        addSlotToContainer(new Slot(tentity, 3, 180, 112));
        addSlotToContainer(new Slot(tentity, 4, 7, 57));
        
        if(tileEntity.tier == FactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 55 + (i*38);
        		
		        addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 55 + (i*38);
        		
		        addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
        	}
        }
        else if(tileEntity.tier == FactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 35 + (i*26);
	        	
	        	addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 35 + (i*26);
	        	
	        	addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
        	}
        }
        else if(tileEntity.tier == FactoryTier.ELITE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 29 + (i*19);
	        	
	        	addSlotToContainer(new Slot(tentity, 5+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 29 + (i*19);
	        	
	        	addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+5+i, xAxis, 57));
        	}
        }
        
        int slotX;

        for(slotX = 0; slotX < 3; slotX++)
        {
            for(int slotY = 0; slotY < 9; ++slotY)
            {
                addSlotToContainer(new Slot(inventory, slotY + slotX * 9 + 9, 8 + slotY * 18, 95 + slotX * 18));
            }
        }

        for(slotX = 0; slotX < 9; slotX++)
        {
            addSlotToContainer(new Slot(inventory, slotX, 8 + slotX * 18, 153));
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

            if(isOutputSlot(slotID))
            {
            	if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
            	{
            		return null;
            	}
            }
            else if(slotID != 2 && slotID != 3 && isProperMachine(slotStack) && !slotStack.isItemEqual(tileEntity.getMachineStack()))
            {
            	if(!mergeItemStack(slotStack, 2, 3, false))
            	{
            		return null;
            	}
            }
            else if(slotID == 3)
            {
            	if(!mergeItemStack(slotStack, tileEntity.inventory.length, inventorySlots.size(), true))
            	{
            		return null;
            	}
            }
        	else if(ChargeUtils.canBeDischarged(slotStack))
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
            else if(RecipeType.values()[tileEntity.recipeType].getFuelTicks(slotStack) > 0)
            {
            	if(slotID > tileEntity.inventory.length-1)
            	{
                    if(!mergeItemStack(slotStack, 4, 5, false))
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
            else if(RecipeType.values()[tileEntity.recipeType].getCopiedOutput(slotStack, false) != null)
    		{
            	if(!isInputSlot(slotID))
            	{
            		if(!mergeItemStack(slotStack, 5, 5+tileEntity.tier.processes, false))
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
    
    public boolean isProperMachine(ItemStack itemStack)
    {
    	if(itemStack != null && itemStack.getItem() instanceof ItemBlockMachine)
    	{
    		MachineType type = MachineType.get(itemStack);
    		
    		if(type == MachineType.ENERGIZED_SMELTER || type == MachineType.ENRICHMENT_CHAMBER || 
    				type == MachineType.CRUSHER || type == MachineType.OSMIUM_COMPRESSOR ||
    				type == MachineType.COMBINER || type == MachineType.PURIFICATION_CHAMBER)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public boolean isInputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.FactoryTier.BASIC)
    		return slot >= 5 && slot <= 7;
    	if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
    		return slot >= 5 && slot <= 9;
    	if(tileEntity.tier == Tier.FactoryTier.ELITE)
    		return slot >= 5 && slot <= 11;
    	
    	return false;
    }
    
    public boolean isOutputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.FactoryTier.BASIC)
    		return slot >= 8 && slot <= 10;
    	if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
    		return slot >= 10 && slot <= 14;
    	if(tileEntity.tier == Tier.FactoryTier.ELITE)
    		return slot >= 12 && slot <= 18;
    	
    	return false;
    }
}
