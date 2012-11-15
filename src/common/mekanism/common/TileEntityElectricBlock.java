package mekanism.common;

import com.google.common.io.ByteArrayDataInput;

import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import universalelectricity.prefab.TileEntityDisableable;
import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import mekanism.api.ITileNetwork;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityElectricBlock extends TileEntityBasicBlock implements IWrenchable, ISidedInventory, IInventory, ITileNetwork, IPowerReceptor
{
	/** The inventory slot itemstacks used by this block. */
	public ItemStack[] inventory;
	
	/** How much energy is stored in this block. */
	public int energyStored;
	
	/** Maximum amount of energy this machine can hold. */
	public int MAX_ENERGY;
	
	/** The full name of this machine. */
	public String fullName;
	
	/** BuildCraft power provider. */
	public IPowerProvider powerProvider;
	
	/**
	 * The base of all blocks that deal with electricity. It has a facing state, initialized state,
	 * and a current amount of stored energy.
	 * @param name - full name of this block
	 * @param maxEnergy - how much energy this block can store
	 */
	public TileEntityElectricBlock(String name, int maxEnergy)
	{
		fullName = name;
		MAX_ENERGY = maxEnergy;
		if(PowerFramework.currentFramework != null)
		{
			powerProvider = PowerFramework.currentFramework.createPowerProvider();
			powerProvider.configure(5, 2, 10, 1, maxEnergy/10);
		}
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
        if (side == ForgeDirection.DOWN) return 1;
        if (side == ForgeDirection.UP) return 0; 
        return 2;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	@Override
	public int getSizeInventory() 
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int par1) 
	{
		return inventory[par1];
	}

	@Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        if (inventory[par1] != null)
        {
            ItemStack var3;

            if (inventory[par1].stackSize <= par2)
            {
                var3 = inventory[par1];
                inventory[par1] = null;
                return var3;
            }
            else
            {
                var3 = inventory[par1].splitStack(par2);

                if (inventory[par1].stackSize == 0)
                {
                    inventory[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

	@Override
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (inventory[par1] != null)
        {
            ItemStack var2 = inventory[par1];
            inventory[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

	@Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        inventory[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }
    
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.loadPowerProvider(this, nbtTags);
        }
        
        NBTTagList tagList = nbtTags.getTagList("Items");
        inventory = new ItemStack[getSizeInventory()];

        for (int slots = 0; slots < tagList.tagCount(); ++slots)
        {
            NBTTagCompound tagCompound = (NBTTagCompound)tagList.tagAt(slots);
            byte slotID = tagCompound.getByte("Slot");

            if (slotID >= 0 && slotID < inventory.length)
            {
                inventory[slotID] = ItemStack.loadItemStackFromNBT(tagCompound);
            }
        }

        energyStored = nbtTags.getInteger("energyStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(PowerFramework.currentFramework != null)
        {
        	PowerFramework.currentFramework.savePowerProvider(this, nbtTags);
        }
        
        nbtTags.setInteger("energyStored", energyStored);
        NBTTagList tagList = new NBTTagList();

        for (int slots = 0; slots < inventory.length; ++slots)
        {
            if (inventory[slots] != null)
            {
                NBTTagCompound tagCompound = new NBTTagCompound();
                tagCompound.setByte("Slot", (byte)slots);
                inventory[slots].writeToNBT(tagCompound);
                tagList.appendTag(tagCompound);
            }
        }

        nbtTags.setTag("Items", tagList);
    }
    
	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}
	
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}
	
	@Override
	public String getInvName()
	{
		return fullName;
	}
	
	@Override
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	
	@Override
	public void openChest() 
	{
		playersUsing++;
	}

	@Override
	public void closeChest() 
	{
		playersUsing--;
	}
	
	@Override
	public void setPowerProvider(IPowerProvider provider)
	{
		powerProvider = provider;
	}
	
	@Override
	public IPowerProvider getPowerProvider() 
	{
		return powerProvider;
	}
	
	@Override
	public int powerRequest() 
	{
		return getPowerProvider().getMaxEnergyReceived();
	}
	
	@Override
	public void doWork() {}
}
