package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketTileEntity implements IMekanismPacket
{
	public Object3D object3D;
	
	public Object[] parameters;
	
	public PacketTileEntity(Object3D obj, Object... params)
	{
		object3D = obj;
		parameters = params;
	}
	
	public PacketTileEntity() {}
	
	@Override
	public String getName() 
	{
		return "TileEntity";
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
		
		PacketHandler.encode(parameters, dataStream, 0);
	}
}
