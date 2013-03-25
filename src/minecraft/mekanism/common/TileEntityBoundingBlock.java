package mekanism.common;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.api.EnumGas;
import mekanism.api.ITileNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityBoundingBlock extends TileEntity implements ITileNetwork
{
	public int mainX;
	public int mainY;
	public int mainZ;

	public void setMainLocation(int x, int y, int z)
	{
		if(!worldObj.isRemote)
		{
			mainX = x;
			mainY = y;
			mainZ = z;
			
			PacketHandler.sendTileEntityPacketToClients(this, 0, getNetworkedData(new ArrayList()));
		}
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
	
	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		mainX = dataStream.readInt();
		mainY = dataStream.readInt();
		mainZ = dataStream.readInt();
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        mainX = nbtTags.getInteger("mainX");
        mainY = nbtTags.getInteger("mainY");
        mainZ = nbtTags.getInteger("mainZ");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("mainX", mainX);
        nbtTags.setInteger("mainY", mainY);
        nbtTags.setInteger("mainZ", mainZ);
    }
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data.add(mainX);
		data.add(mainY);
		data.add(mainZ);
		return data;
	}
}
