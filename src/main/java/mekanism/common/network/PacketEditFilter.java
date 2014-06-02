package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.miner.MinerFilter;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;

public class PacketEditFilter extends MekanismPacket
{
	public Coord4D coord4D;

	public TransporterFilter tFilter;
	public TransporterFilter tEdited;

	public MinerFilter mFilter;
	public MinerFilter mEdited;

	public byte type = -1;

	public boolean delete;

	public PacketEditFilter(Coord4D coord, boolean deletion, Object filter, Object edited)
	{
		coord4D = coord;
		delete = deletion;

		if(filter instanceof TransporterFilter)
		{
			tFilter = (TransporterFilter)filter;

			if(!delete)
			{
				tEdited = (TransporterFilter)edited;
			}

			type = 0;
		}
		else if(filter instanceof MinerFilter)
		{
			mFilter = (MinerFilter)filter;

			if(!delete)
			{
				mEdited = (MinerFilter)edited;
			}

			type = 1;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);

		dataStream.writeInt(coord4D.dimensionId);

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

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());

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

		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(coord4D.dimensionId);

		if(worldServer != null)
		{
			if(type == 0 && coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) coord4D.getTileEntity(worldServer);

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
					Mekanism.packetPipeline.sendTo(new PacketTileEntity(Coord4D.get(sorter), sorter.getFilterPacket(new ArrayList())), (EntityPlayerMP)iterPlayer);
				}
			}
			else if(type == 1 && coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				TileEntityDigitalMiner miner = (TileEntityDigitalMiner) coord4D.getTileEntity(worldServer);

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
					Mekanism.packetPipeline.sendTo(new PacketTileEntity(Coord4D.get(miner), miner.getFilterPacket(new ArrayList())), (EntityPlayerMP)iterPlayer);
				}
			}
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{

	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{

	}
}
