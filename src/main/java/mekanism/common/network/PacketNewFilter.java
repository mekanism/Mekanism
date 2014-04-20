package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.miner.MinerFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.FMLCommonHandler;

public class PacketNewFilter implements IMekanismPacket
{
	public Coord4D object3D;

	public TransporterFilter tFilter;

	public MinerFilter mFilter;

	public byte type = -1;

	@Override
	public String getName()
	{
		return "NewFilter";
	}

	@Override
	public IMekanismPacket setParams(Object... data)
	{
		object3D = (Coord4D)data[0];

		if(data[1] instanceof TransporterFilter)
		{
			tFilter = (TransporterFilter)data[1];
			type = 0;
		}
		else if(data[1] instanceof MinerFilter)
		{
			mFilter = (MinerFilter)data[1];
			type = 1;
		}

		return this;
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		object3D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		type = dataStream.readByte();

		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(object3D.dimensionId);

		if(worldServer != null)
		{
			if(type == 0 && object3D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)object3D.getTileEntity(worldServer);
				TransporterFilter filter = TransporterFilter.readFromPacket(dataStream);

				sorter.filters.add(filter);

				for(EntityPlayer iterPlayer : sorter.playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(sorter), sorter.getFilterPacket(new ArrayList())), iterPlayer);
				}
			}
			else if(type == 1 && object3D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				TileEntityDigitalMiner miner = (TileEntityDigitalMiner)object3D.getTileEntity(worldServer);
				MinerFilter filter = MinerFilter.readFromPacket(dataStream);

				miner.filters.add(filter);

				for(EntityPlayer iterPlayer : miner.playersUsing)
				{
					PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketTileEntity().setParams(Coord4D.get(miner), miner.getFilterPacket(new ArrayList())), iterPlayer);
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

		ArrayList data = new ArrayList();

		if(type == 0)
		{
			tFilter.write(data);
		}
		else if(type == 1)
		{
			mFilter.write(data);
		}

		PacketHandler.encode(data.toArray(), dataStream);
	}
}
