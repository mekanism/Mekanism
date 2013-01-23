package mekanism.common;

import ic2.api.IElectricItem;
import mekanism.api.InfusionInput;
import mekanism.api.InfusionOutput;
import mekanism.api.InfusionType;
import mekanism.common.RecipeHandler.Recipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import universalelectricity.core.implement.IItemElectric;

public class ContainerMetallurgicInfuser extends Container
{
    private TileEntityMetallurgicInfuser tileEntity;

    public ContainerMetallurgicInfuser(InventoryPlayer inventory, TileEntityMetallurgicInfuser tentity)
    {
        tileEntity = tentity;
        addSlotToContainer(new SlotMachineUpgrade(tentity, 0, 7, 7));
        addSlotToContainer(new Slot(tentity, 1, 17, 35));
        addSlotToContainer(new Slot(tentity, 2, 51, 43));
        addSlotToContainer(new SlotFurnace(inventory.player, tentity, 3, 109, 43));
        addSlotToContainer(new SlotEnergy(tentity, 4, 143, 35));
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

            if(slotID != 0 && slotID != 1 && slotID != 2 && slotID != 3 && slotID != 4)
            {
            	if(MekanismUtils.oreDictCheck(slotStack, "dustTin") && (tileEntity.type == InfusionType.TIN || tileEntity.type == InfusionType.NONE))
            	{
            		if(!mergeItemStack(slotStack, 1, 2, false))
            		{
            			return null;
            		}
            	}
            	else if(slotStack.getItem() instanceof ItemMachineUpgrade)
            	{
            		if(!mergeItemStack(slotStack, 0, 1, false))
            		{
            			return null;
            		}
            	}
            	else if(slotStack.isItemEqual(new ItemStack(Mekanism.CompressedCarbon)) && (tileEntity.type == InfusionType.COAL || tileEntity.type == InfusionType.NONE))
            	{
            		if(!mergeItemStack(slotStack, 1, 2, false))
            		{
            			return null;
            		}
            	}
            	else if(slotStack.getItem() instanceof IItemElectric || slotStack.getItem() instanceof IElectricItem)
            	{
            		if(!mergeItemStack(slotStack, 4, 5, false))
            		{
            			return null;
            		}
            	}
            	else if(RecipeHandler.getOutput(InfusionInput.getInfusion(tileEntity.type, tileEntity.infuseStored, slotStack), false, Recipe.METALLURGIC_INFUSER.get()) != null)
            	{
            		if(!mergeItemStack(slotStack, 2, 3, false))
            		{
            			return null;
            		}
            	}
                else {
                	if(slotID >= 5 && slotID <= 31)
                	{
                		if(!mergeItemStack(slotStack, 32, inventorySlots.size(), false))
                		{
                			return null;
                		}
                	}
                	else if(slotID > 31)
                	{
                		if(!mergeItemStack(slotStack, 5, 31, false))
                		{
                			return null;
                		}
                	}
                	else {
                		if(!mergeItemStack(slotStack, 5, inventorySlots.size(), true))
                		{
                			return null;
                		}
                	}
                }
            }
            else {
            	if(!mergeItemStack(slotStack, 5, inventorySlots.size(), true))
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
