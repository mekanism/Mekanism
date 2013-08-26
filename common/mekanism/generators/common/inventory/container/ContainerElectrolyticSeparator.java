package mekanism.generators.common.inventory.container;

import mekanism.api.IStorageTank;
import mekanism.api.gas.EnumGas;
import mekanism.common.inventory.slot.SlotStorageTank;
import mekanism.common.inventory.slot.SlotEnergy.SlotDischarge;
import mekanism.common.util.ChargeUtils;
import mekanism.generators.common.tileentity.TileEntityElectrolyticSeparator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ContainerElectrolyticSeparator extends Container
{
    private TileEntityElectrolyticSeparator tileEntity;

    public ContainerElectrolyticSeparator(InventoryPlayer inventory, TileEntityElectrolyticSeparator tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new Slot(tentity, 0, 17, 35));
        addSlotToContainer(new SlotStorageTank(tentity, EnumGas.HYDROGEN, false, 1, 59, 52));
        addSlotToContainer(new SlotStorageTank(tentity, EnumGas.OXYGEN, false, 2, 101, 52));
        addSlotToContainer(new SlotDischarge(tentity, 3, 143, 35));
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
    public void onContainerClosed(EntityPlayer entityplayer)
    {
		super.onContainerClosed(entityplayer);
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
            	else if(ChargeUtils.canBeDischarged(slotStack))
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
    
    public boolean isWater(ItemStack itemStack)
    {
    	FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
    	
    	if(fluid != null)
    	{
    		if(fluid.getFluid() == FluidRegistry.WATER)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
}
