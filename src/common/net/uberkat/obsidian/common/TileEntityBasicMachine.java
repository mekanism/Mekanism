package net.uberkat.obsidian.common;

import obsidian.api.IElectricMachine;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import net.minecraft.src.*;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;

public abstract class TileEntityBasicMachine extends TileEntity implements IElectricMachine
{
	/** The direction this block is facing. */
	public int facing;
	
	/** A timer used to send packets to clients. */
	public int packetTick = 0;
	
	/** Whether or not this block is in it's active state. */
	public boolean isActive;
	
	/** The previous active state for this block. */
	public boolean prevActive;
	
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized;
	
	/** The full name of this machine. */
	public String fullName;
	
	/** The GUI texture path for this machine. */
	public String guiTexturePath;
	
	/**
	 * The most basic of machines - a simple tile entity with a facing, active state, initialized state, and animated texture.
	 * @param name - full name of this machine
	 * @param path - GUI texture path of this machine
	 */
	public TileEntityBasicMachine(String name, String path)
	{
		fullName = name;
		guiTexturePath = path;
		isActive = false;
	}
	
	public void updateEntity()
	{
		if(!initialized)
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
			if(packetTick == 5)
			{
				sendPacket();
			}
			
			if(packetTick % 100 == 0)
			{
				sendPacketWithRange();
			}
			packetTick++;
		}
	}
	
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64.0D;
	}
	
	public void openChest() {}

	public void closeChest() {}
	
	public String getInvName() 
	{
		return fullName;
	}
	
	public int getInventoryStackLimit() 
	{
		return 64;
	}
	
	public void invalidate()
	{
		//To be used
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

    public void setActive(boolean active)
    {
    	isActive = active;
    	
    	if(prevActive != active)
    	{
    		sendPacket();
    	}
    	
    	prevActive = active;
    }
}
