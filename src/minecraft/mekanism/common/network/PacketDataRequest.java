package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketDataRequest implements IMekanismPacket
{
	public Object3D object3D;
	
	public PacketDataRequest(Object3D obj)
	{
		object3D = obj;
	}
	
	public PacketDataRequest() {}
	
	@Override
	public String getName()
	{
		return "DataRequest";
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		int id = dataStream.readInt();
		
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(id);
		
		if(worldServer != null && worldServer.getBlockTileEntity(x, y, z) instanceof ITileNetwork)
		{
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity(Object3D.get(worldServer.getBlockTileEntity(x, y, z)), ((ITileNetwork)worldServer.getBlockTileEntity(x, y, z)).getNetworkedData(new ArrayList())));
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(object3D.dimensionId);
	}
}
