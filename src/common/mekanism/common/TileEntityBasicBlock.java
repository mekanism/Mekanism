package mekanism.common;

import com.google.common.io.ByteArrayDataInput;

import universalelectricity.prefab.TileEntityDisableable;
import ic2.api.EnergyNet;
import ic2.api.IWrenchable;
import mekanism.api.ITileNetwork;
import net.minecraft.src.*;

public abstract class TileEntityBasicBlock extends TileEntityDisableable implements IWrenchable, ITileNetwork
{
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized;
	
	/** The direction this block is facing. */
	public int facing;
	
	/** The amount of players using this block */
	public int playersUsing = 0;
	
	/** A timer used to send packets to clients. */
	public int packetTick;
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		if(!initialized && worldObj != null)
		{
			if(Mekanism.hooks.IC2Loaded)
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
				if(packetTick % 3 == 0)
				{
					sendPacketWithRange();
				}
			}
			else {
				if(packetTick % 20 == 0)
				{
					sendPacketWithRange();
				}
			}
			packetTick++;
		}
	}
	
	/**
	 * Update call for machines. Use instead of updateEntity -- it's called every tick.
	 */
	public abstract void onUpdate();
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        facing = nbtTags.getInteger("facing");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        nbtTags.setInteger("facing", facing);
    }

	@Override
	public boolean wrenchCanSetFacing(EntityPlayer entityPlayer, int side)
	{
		return true;
	}

	@Override
	public short getFacing() 
	{
		return (short)facing;
	}

	@Override
	public void setFacing(short direction) 
	{
		if(initialized)
		{
			if(Mekanism.hooks.IC2Loaded)
			{
				EnergyNet.getForWorld(worldObj).removeTileEntity(this);
			}
		}
		
		initialized = false;
		facing = direction;
		
		sendPacket();
		
		if(Mekanism.hooks.IC2Loaded)
		{
			EnergyNet.getForWorld(worldObj).addTileEntity(this);
		}
		initialized = true;
	}

	@Override
	public boolean wrenchCanRemove(EntityPlayer entityPlayer) 
	{
		return true;
	}

	@Override
	public float getWrenchDropRate() 
	{
		return 1.0F;
	}
}
