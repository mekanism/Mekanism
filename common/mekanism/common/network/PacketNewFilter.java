package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketNewFilter implements IMekanismPacket
{
	public Object3D object3D;
	
	public TransporterFilter filter;
	
	@Override
	public String getName()
	{
		return "FilterUpdate";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		filter = (TransporterFilter)data[1];
		
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
		
		if(worldServer != null && worldServer.getBlockTileEntity(x, y, z) instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)worldServer.getBlockTileEntity(x, y, z);
			TransporterFilter filter = TransporterFilter.readFromPacket(dataStream);
			
			sorter.filters.add(filter);
			
			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(sorter), sorter.getFilterPacket(new ArrayList())), player);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(object3D.xCoord);
		dataStream.writeInt(object3D.yCoord);
		dataStream.writeInt(object3D.zCoord);
		
		dataStream.writeInt(object3D.dimensionId);
		
		ArrayList data = new ArrayList();
		filter.write(data);
		PacketHandler.encode(data.toArray(), dataStream);
	}
}
