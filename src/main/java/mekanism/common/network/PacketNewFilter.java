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

public class PacketNewFilter extends MekanismPacket
{
	public Coord4D coord4D;

	public TransporterFilter tFilter;

	public MinerFilter mFilter;

	public byte type = -1;
	
	public PacketNewFilter() {}

	public PacketNewFilter(Coord4D coord, Object filter)
	{
		coord4D = coord;

		if(filter instanceof TransporterFilter)
		{
			tFilter = (TransporterFilter)filter;
			type = 0;
		}
		else if(filter instanceof MinerFilter)
		{
			mFilter = (MinerFilter)filter;
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

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		coord4D = new Coord4D(dataStream.readInt(), dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
		type = dataStream.readByte();

		World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(coord4D.dimensionId);

		if(worldServer != null)
		{
			if(type == 0 && coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) coord4D.getTileEntity(worldServer);
				TransporterFilter filter = TransporterFilter.readFromPacket(dataStream);

				sorter.filters.add(filter);

				for(EntityPlayer iterPlayer : sorter.playersUsing)
				{
					Mekanism.packetPipeline.sendTo(new PacketTileEntity(Coord4D.get(sorter), sorter.getFilterPacket(new ArrayList())), (EntityPlayerMP)iterPlayer);
				}
			}
			else if(type == 1 && coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner)
			{
				TileEntityDigitalMiner miner = (TileEntityDigitalMiner) coord4D.getTileEntity(worldServer);
				MinerFilter filter = MinerFilter.readFromPacket(dataStream);

				miner.filters.add(filter);

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
