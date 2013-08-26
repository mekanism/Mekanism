package mekanism.common.tileentity;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

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
			
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(this), getNetworkedData(new ArrayList())));
		}
	}
	
	@Override
	public void validate()
	{
		super.validate();
		
		if(worldObj.isRemote)
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketDataRequest().setParams(Object3D.get(this)));
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
