package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.common.ITileNetwork;
import mekanism.common.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketTileEntity extends MekanismPacket
{
	public Coord4D coord4D;

	public ArrayList parameters;
	
	public ByteBuf storedBuffer = null;

	public PacketTileEntity(Coord4D coord, ArrayList params)
	{
		coord4D = coord;
		parameters = params;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream) throws Exception
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);

		PacketHandler.encode(new Object[] {parameters}, dataStream);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream) throws Exception
	{
		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();
		
		storedBuffer = dataStream.copy();
	}

	@Override
	public void handleClientSide(EntityPlayer player) throws Exception
	{
		TileEntity tileEntity = coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof ITileNetwork)
		{
			((ITileNetwork)tileEntity).handlePacketData(storedBuffer);
		}
	}

	@Override
	public void handleServerSide(EntityPlayer player) throws Exception
	{
		TileEntity tileEntity = coord4D.getTileEntity(player.worldObj);
		
		if(tileEntity instanceof ITileNetwork)
		{
			((ITileNetwork)tileEntity).handlePacketData(storedBuffer);
		}
	}
}
