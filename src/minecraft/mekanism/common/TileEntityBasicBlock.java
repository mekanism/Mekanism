package mekanism.common;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.IWrenchable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import universalelectricity.prefab.tile.TileEntityDisableable;

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
		
		onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(playersUsing > 0)
			{
				PacketHandler.sendTileEntityPacketToClients(this, 50, getNetworkedData(new ArrayList()));
			}
			
			packetTick++;
		}
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		facing = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(facing);
		return data;
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendDataRequest(this);
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
		if(canSetFacing(direction))
		{
			facing = direction;
		}
		
		PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
	}
	
	/**
	 * Whether or not this block's orientation can be changed to a specific direction. True by default.
	 * @param facing - facing to check
	 * @return if the block's orientation can be changed
	 */
	public boolean canSetFacing(int facing)
	{
		return true;
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
	
	@Override
	public ItemStack getWrenchDrop(EntityPlayer entityPlayer)
	{
        return Block.blocksList[getBlockType().blockID].getPickBlock(null, worldObj, xCoord, yCoord, zCoord);
	}
}
