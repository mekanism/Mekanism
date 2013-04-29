package mekanism.common;

import universalelectricity.core.item.IItemElectric;
import ic2.api.IElectricItem;
import mekanism.common.BlockMachine.MachineType;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.SlotEnergy.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContainerFactory extends Container
{
    private TileEntityFactory tileEntity;

    public ContainerFactory(InventoryPlayer inventory, TileEntityFactory tentity)
    {
        tileEntity = tentity;
        
        addSlotToContainer(new SlotMachineUpgrade(tentity, 0, 180, 11));
        addSlotToContainer(new SlotDischarge(tentity, 1, 7, 35));
        addSlotToContainer(new Slot(tentity, 2, 180, 75));
        addSlotToContainer(new Slot(tentity, 3, 180, 112));
        
        if(tileEntity.tier == FactoryTier.BASIC)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 55 + (i*38);
        		
		        addSlotToContainer(new Slot(tentity, 4+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
        		int xAxis = 55 + (i*38);
        		
		        addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+4+i, xAxis, 57));
        	}
        }
        else if(tileEntity.tier == FactoryTier.ADVANCED)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 35 + (i*26);
	        	
	        	addSlotToContainer(new Slot(tentity, 4+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 35 + (i*26);
	        	
	        	addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+4+i, xAxis, 57));
        	}
        }
        else if(tileEntity.tier == FactoryTier.ELITE)
        {
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 29 + (i*19);
	        	
	        	addSlotToContainer(new Slot(tentity, 4+i, xAxis, 13));
        	}
        	
        	for(int i = 0; i < tileEntity.tier.processes; i++)
        	{
	        	int xAxis = 29 + (i*19);
	        	
	        	addSlotToContainer(new SlotOutput(tentity, tileEntity.tier.processes+4+i, xAxis, 57));
        	}
        }
        
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
        	else if((slotStack.getItem() instanceof IElectricItem && ((IElectricItem)slotStack.getItem()).canProvideEnergy(slotStack)) || (slotStack.getItem() instanceof IItemElectric && ((IItemElectric)slotStack.getItem()).getProvideRequest(slotStack).amperes != 0) || slotStack.itemID == Item.redstone.itemID)
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
            else if(RecipeType.values()[tileEntity.recipeType].getCopiedOutput(slotStack, false) != null)
    		{
            	if(!isInputSlot(slotID))
            	{
            		if(!mergeItemStack(slotStack, 4, 4+tileEntity.tier.processes, false))
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
    		if(itemStack.getItemDamage() == MachineType.ENERGIZED_SMELTER.meta || 
    				itemStack.getItemDamage() == MachineType.ENRICHMENT_CHAMBER.meta || 
    				itemStack.getItemDamage() == MachineType.CRUSHER.meta)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public boolean isInputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.FactoryTier.BASIC)
    		return slot >= 4 && slot <= 6;
    	if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
    		return slot >= 4 && slot <= 8;
    	if(tileEntity.tier == Tier.FactoryTier.ELITE)
    		return slot >= 4 && slot <= 10;
    	
    	return false;
    }
    
    public boolean isOutputSlot(int slot)
    {
    	if(tileEntity.tier == Tier.FactoryTier.BASIC)
    		return slot >= 7 && slot <= 9;
    	if(tileEntity.tier == Tier.FactoryTier.ADVANCED)
    		return slot >= 9 && slot <= 13;
    	if(tileEntity.tier == Tier.FactoryTier.ELITE)
    		return slot >= 11 && slot <= 17;
    	
    	return false;
    }
}
