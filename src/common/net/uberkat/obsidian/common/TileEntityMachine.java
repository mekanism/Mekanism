package net.uberkat.obsidian.common;

import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public class TileEntityMachine extends TileEntity implements IInventory, ISidedInventory
{
     /** The ItemStacks that hold the items currently being used in the furnace */
    protected ItemStack[] machineItemStacks = new ItemStack[3];

    /** The number of ticks that the furnace will keep burning */
    public int machineBurnTime = 0;

    /** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
    public int currentItemBurnTime = 0;

    /** The number of ticks that the current item has been cooking for */
    public int machineCookTime = 0;
    
    /** The number of ticks it takes to cook an item */
    public int maxBurnTime = 0;
    
    /** The full name of this tile entity */
    public String fullName;
    
    /** Whether the machine is in it's active state or not */
    public boolean isActive;
    
    public TileEntityMachine(int time, String name)
    {
    	maxBurnTime = time;
    	fullName = name;
    }
	
	public int getStartInventorySide(ForgeDirection side) 
	{
        if (side == ForgeDirection.DOWN) return 1;
        if (side == ForgeDirection.UP) return 0; 
        return 2;
	}

	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	public int getSizeInventory() 
	{
		return machineItemStacks.length;
	}

	public ItemStack getStackInSlot(int var1) 
	{
		return machineItemStacks[var1];
	}

    public ItemStack decrStackSize(int par1, int par2)
    {
        if (machineItemStacks[par1] != null)
        {
            ItemStack var3;

            if (machineItemStacks[par1].stackSize <= par2)
            {
                var3 = machineItemStacks[par1];
                machineItemStacks[par1] = null;
                return var3;
            }
            else
            {
                var3 = machineItemStacks[par1].splitStack(par2);

                if (machineItemStacks[par1].stackSize == 0)
                {
                    machineItemStacks[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (machineItemStacks[par1] != null)
        {
            ItemStack var2 = machineItemStacks[par1];
            machineItemStacks[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        machineItemStacks[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }

	public String getInvName()
	{
		return fullName;
	}

	public int getInventoryStackLimit() 
	{
		return 64;
	}

    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : par1EntityPlayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
    }
    
    public int getBurnTimeRemainingScaled(int par1)
    {
        if (currentItemBurnTime == 0)
        {
            currentItemBurnTime = maxBurnTime;
        }

        return machineBurnTime * par1 / currentItemBurnTime;
    }
    
    public boolean isBurning()
    {
        return machineBurnTime > 0;
    }
    
    public int getCookProgressScaled(int par1)
    {
        return machineCookTime * par1 / maxBurnTime;
    }
    
    public void smeltItem()
    {
    	
    }
    
    protected boolean canSmelt()
    {
    	return false;
    }

	public void openChest()
	{

	}

	public void closeChest()
	{

	}
}
