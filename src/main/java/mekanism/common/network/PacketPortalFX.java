package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.Random;

import mekanism.api.Coord4D;
import net.minecraft.entity.player.EntityPlayer;

public class PacketPortalFX extends MekanismPacket
{
	public Coord4D coord4D;

	public PacketPortalFX() {}
	
	public PacketPortalFX(Coord4D coord)
	{
		coord4D = coord;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		Random random = new Random();

		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();

		for(int i = 0; i < 50; i++)
		{
			player.worldObj.spawnParticle("portal", x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
			player.worldObj.spawnParticle("portal", x + random.nextFloat(), y + 1 + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
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
