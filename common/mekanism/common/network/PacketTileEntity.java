package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.common.ITileNetwork;
import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketTileEntity implements IMekanismPacket
{
	public Object3D object3D;
	
	public ArrayList parameters;
	
	@Override
	public String getName() 
	{
		return "TileEntity";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		parameters = (ArrayList)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity instanceof ITileNetwork)
		{
			((ITileNetwork)tileEntity).handlePacketData(dataStream);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		PacketHandler.encode(new Object[] {parameters}, dataStream);
	}
}
