package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketLogisticalSorterGui implements IMekanismPacket
{
	public Object3D object3D;
	
	public int type;
	
	@Override
	public String getName()
	{
		return "LogisticalSorterGui";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		type = (Integer)data[1];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		int id = dataStream.readInt();
		
		int type = dataStream.readInt();
		
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(id);
		
		if(worldServer != null && worldServer.getBlockTileEntity(x, y, z) instanceof ITileNetwork)
		{
			TileEntity tileEntity = worldServer.getBlockTileEntity(x, y, z);
			
			if(type == 0)
			{
				player.openGui(Mekanism.instance, 27, worldServer, x, y, z);
			}
			else if(type == 1)
			{
				player.openGui(Mekanism.instance, 28, worldServer, x, y, z);
			}
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(object3D.dimensionId);
		
		dataStream.writeInt(type);
	}
}
