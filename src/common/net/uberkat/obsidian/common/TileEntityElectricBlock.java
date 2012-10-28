package net.uberkat.obsidian.common;

import obsidian.api.ITileNetwork;
import universalelectricity.prefab.TileEntityDisableable;
import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityElectricBlock extends TileEntityDisableable implements IWrenchable, ISidedInventory, IInventory, ITileNetwork
{
	/** The inventory slot itemstacks used by this block. */
	public ItemStack[] inventory;
	
	/** How much energy is stored in this block. */
	public int energyStored;
	
	/** The direction this block is facing. */
	public int facing;
	
	/** Maximum amount of energy this machine can hold. */
	public int MAX_ENERGY;
	
	/** The full name of this machine. */
	public String fullName;
	
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized;
	
	/** The amount of players using this block */
	public int playersUsing = 0;
	
	/** A timer used to send packets to clients. */
	public int packetTick;
	
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
	}
	
	public void updateEntity()
	{
		if(!initialized && worldObj != null)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			
			initialized = true;
		}
		
		onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(playersUsing > 0)
			{
				sendPacketWithRange();
			}
			else {
				if(packetTick % 100 == 0)
				{
					sendPacketWithRange();
				}
			}
		}
	}
	
	/**
	 * Update call for machines. Use instead of updateEntity -- it's called every tick.
	 */
	public abstract void onUpdate();
	
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
		return inventory.length;
	}

	public ItemStack getStackInSlot(int par1) 
	{
		return inventory[par1];
	}

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

    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        inventory[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit())
        {
            par2ItemStack.stackSize = getInventoryStackLimit();
        }
    }
    
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}

	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return true;
	}

	public short getFacing() 
	{
		return (short)facing;
	}

	public void setFacing(short direction) 
	{
		if(initialized)
		{
			if(ObsidianIngots.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).removeTileEntity(this);
			}
		}
		
		initialized = false;
		facing = direction;
		sendPacket();
		if(ObsidianIngots.hooks.IC2Loaded)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
		}
		initialized = true;
	}

	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return true;
	}

	public float getWrenchDropRate() 
	{
		return 1.0F;
	}
	
	public boolean isAddedToEnergyNet()
	{
		return initialized;
	}
	
	public String getInvName()
	{
		return fullName;
	}
	
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	
	public void openChest() 
	{
		playersUsing++;
	}

	public void closeChest() 
	{
		playersUsing--;
	}
}
