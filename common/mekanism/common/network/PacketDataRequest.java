package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tileentity.TileEntityDynamicTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketDataRequest implements IMekanismPacket
{
	public Coord4D object3D;
	
	@Override
	public String getName()
	{
		return "DataRequest";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Coord4D)data[0];
		
		return this;
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
			TileEntity tileEntity = worldServer.getBlockTileEntity(x, y, z);
			
			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).sendStructure = true;
			}
			
			if(tileEntity instanceof IGridTransmitter)
			{
				IGridTransmitter transmitter = (IGridTransmitter)tileEntity;
				
				if(transmitter.getTransmitterNetwork() instanceof DynamicNetwork)
				{
					((DynamicNetwork)transmitter.getTransmitterNetwork()).addUpdate(player);
				}
			}
			
			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Coord4D.get(worldServer.getBlockTileEntity(x, y, z)), ((ITileNetwork)worldServer.getBlockTileEntity(x, y, z)).getNetworkedData(new ArrayList())));
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
