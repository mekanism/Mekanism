package mekanism.common;

import java.util.ArrayList;

import mekanism.api.ITileNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityControlPanel extends TileEntity implements ITileNetwork
{
	/** A counter to send packets at defined intervals. */
	public int packetTick = 0;
	
	/** x value stored in the GUI */
	public int xCached;
	
	/** y value stored in the GUI */
	public int yCached;
	
	/** z value stored in the GUI */
	public int zCached;
	
	@Override
	public void updateEntity()
	{
		packetTick++;
		
		if(packetTick == 5 && !worldObj.isRemote)
		{
			PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
		}
		if(packetTick % 20 == 0 && worldObj.isRemote)
		{
			PacketHandler.sendTileEntityPacketToServer(this, getNetworkedData(new ArrayList()));
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        xCached = nbtTags.getInteger("xCached");
        yCached = nbtTags.getInteger("yCached");
        zCached = nbtTags.getInteger("zCached");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("xCached", xCached);
        nbtTags.setInteger("yCached", yCached);
        nbtTags.setInteger("zCached", zCached);
    }

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		xCached = dataStream.readInt();
		yCached = dataStream.readInt();
		zCached = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(xCached);
		data.add(yCached);
		data.add(zCached);
		return data;
	}
}
