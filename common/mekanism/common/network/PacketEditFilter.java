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

public class PacketEditFilter implements IMekanismPacket
{
	public Object3D object3D;
	
	public TransporterFilter edited;
	
	public boolean delete;
	
	@Override
	public String getName()
	{
		return "EditFilter";
	}
	
	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Object3D)data[0];
		delete = (Boolean)data[1];
		edited = (TransporterFilter)data[2];
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		delete = dataStream.readBoolean();
		edited = TransporterFilter.readFromPacket(dataStream);
			
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);
		
		if(worldServer != null && object3D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
		{
			TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)object3D.getTileEntity(worldServer);
			TransporterFilter filter = TransporterFilter.readFromPacket(dataStream);
			
			if(!sorter.filters.contains(filter))
			{
				return;
			}
			
			int index = sorter.filters.indexOf(filter);
			
			sorter.filters.remove(index);
			
			if(!delete)
			{
				sorter.filters.add(index, edited);
			}
			
			for(EntityPlayer iterPlayer : sorter.playersUsing)
			{
				PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(sorter), sorter.getFilterPacket(new ArrayList())), iterPlayer);
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
		
		dataStream.writeBoolean(delete);
		
		ArrayList data = new ArrayList();
		edited.write(data);
		PacketHandler.encode(data.toArray(), dataStream);
	}
}
