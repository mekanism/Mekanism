package mekanism.generators.common;

import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.common.SlotEnergy;
import mekanism.common.SlotStorageTank;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import universalelectricity.core.item.IItemElectric;

public class ContainerElectrolyticSeparator extends Container
{
    private TileEntityElectrolyticSeparator tileEntity;

    public ContainerElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 17, 35));
        addSlotToContainer(new SlotStorageTank(tentity, EnumGas.HYDROGEN, false, 1, 59, 52));
        addSlotToContainer(new SlotStorageTank(tentity, EnumGas.OXYGEN, false, 2, 101, 52));
        addSlotToContainer(new SlotEnergy(tentity, 3, 143, 35));
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

            if(slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3)
            {
            	if(isWater(slotStack))
            	{
            		if(!mergeItemStack(slotStack, 0, 1, false))
            		{
            			return null;
            		}
            	}
            	else if(slotStack.getItem() instanceof IStorageTank)
            	{
            		if(((IStorageTank)slotStack.getItem()).getGasType(slotStack) == EnumGas.HYDROGEN)
            		{
            			if(!mergeItemStack(slotStack, 1, 2, false))
            			{
            				return null;
            			}
            		}
            		else if(((IStorageTank)slotStack.getItem()).getGasType(slotStack) == EnumGas.OXYGEN)
            		{
            			if(!mergeItemStack(slotStack, 2, 3, false))
            			{
            				return null;
            			}
            		}
            		else if(((IStorageTank)slotStack.getItem()).getGasType(slotStack) == EnumGas.NONE)
            		{
            			if(!mergeItemStack(slotStack, 1, 2, false))
            			{
            				if(!mergeItemStack(slotStack, 2, 3, false))
            				{
            					return null;
            				}
            			}
            		}
            	}
            	else if(slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem)
            	{
            		if(!mergeItemStack(slotStack, 3, 4, false))
            		{
            			return null;
            		}
            	}
            	else {
	            	if(slotID >= 4 && slotID <= 30)
	            	{
	            		if(!mergeItemStack(slotStack, 31, inventorySlots.size(), false))
	            		{
	            			return null;
	            		}
	            	}
	            	else if(slotID > 30)
	            	{
	            		if(!mergeItemStack(slotStack, 4, 30, false))
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
            }
            else {
            	if(!mergeItemStack(slotStack, 4, inventorySlots.size(), true))
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
    
    public boolean isWater(ItemStack itemStack)
    {
    	LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(itemStack);
    	
    	if(liquid != null)
    	{
    		if(liquid.itemID == Block.waterStill.blockID)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
