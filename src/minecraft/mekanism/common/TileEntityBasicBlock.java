package mekanism.common;

import ic2.api.tile.IWrenchable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;

public abstract class TileEntityBasicBlock extends TileEntity implements IWrenchable, ITileNetwork
{
	/** Whether or not this machine has initialized and registered with other mods. */
	public boolean initialized;
	
	/** The direction this block is facing. */
	public int facing;
	
	/** The players currently using this block. */
	public Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();
	
	/** A timer used to send packets to clients. */
	public int packetTick;
	
	@Override
	public void updateEntity()
	{
		onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(playersUsing.size() > 0)
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
