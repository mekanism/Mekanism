package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.common.Object3D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.miner.MinerFilter;
import mekanism.common.tileentity.TileEntityDigitalMiner;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketEditFilter implements IMekanismPacket
{
	public Object3D object3D;
	
	public TransporterFilter tFilter;
	public TransporterFilter tEdited;
	
	public MinerFilter mFilter;
	public MinerFilter mEdited;
	
	public byte type = -1;
	
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
		
		if(data[2] instanceof TransporterFilter)
		{
			tFilter = (TransporterFilter)data[2];
			
			if(!delete)
			{
				tEdited = (TransporterFilter)data[3];
			}
			
			type = 0;
		}
		else if(data[2] instanceof MinerFilter)
		{
			mFilter = (MinerFilter)data[2];
			
			if(!delete)
			{
				mEdited = (MinerFilter)data[3];
			}
			
			type = 1;
		}
		
		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
		object3D = new Object3D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		
		type = dataStream.readByte();
		delete = dataStream.readBoolean();
		
		if(type == 0)
		{
			tFilter = TransporterFilter.readFromPacket(dataStream);
			
			if(!delete)
			{
				tEdited = TransporterFilter.readFromPacket(dataStream);
			}
		}
		else if(type == 1)
		{
			mFilter = MinerFilter.readFromPacket(dataStream);
			
			if(!delete)
			{
				mEdited = MinerFilter.readFromPacket(dataStream);
			}
		}
			
		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);
		
		if(worldServer != null)
		{
			if(type == 0 && object3D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)object3D.getTileEntity(worldServer);
				
				if(!sorter.filters.contains(tFilter))
				{
					return;
				}
				
				int index = sorter.filters.indexOf(tFilter);
				
				sorter.filters.remove(index);
				
				if(!delete)
				{
					sorter.filters.add(index, tEdited);
				}
				
				for(EntityPlayer iterPlayer : sorter.playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(sorter), sorter.getFilterPacket(new ArrayList())), iterPlayer);
				}
			}
			else if(type == 1 && object3D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				TileEntityDigitalMiner miner = (TileEntityDigitalMiner)object3D.getTileEntity(worldServer);
				
				if(!miner.filters.contains(mFilter))
				{
					return;
				}
				
				int index = miner.filters.indexOf(mFilter);
				
				miner.filters.remove(index);
				
				if(!delete)
				{
					miner.filters.add(index, mEdited);
				}
				
				for(EntityPlayer iterPlayer : miner.playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Object3D.get(miner), miner.getFilterPacket(new ArrayList())), iterPlayer);
				}
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
		
		dataStream.writeByte(type);
		
		dataStream.writeBoolean(delete);
		
		ArrayList data = new ArrayList();
		
		if(type == 0)
		{
			tFilter.write(data);
			
			if(!delete)
			{
				tEdited.write(data);
			}
		}
		else if(type == 1)
		{
			mFilter.write(data);
			
			if(!delete)
			{
				mEdited.write(data);
			}
		}
		
		PacketHandler.encode(data.toArray(), dataStream);
	}
}
