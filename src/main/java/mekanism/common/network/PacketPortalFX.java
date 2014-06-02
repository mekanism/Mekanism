package mekanism.common.network;

import java.io.DataOutputStream;
import java.util.Random;

import mekanism.api.Coord4D;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketPortalFX extends MekanismPacket
{
	public Coord4D coord4D;

	public PacketPortalFX(Coord4D coord)
	{
		coord4D = coord;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		Random random = new Random();

		int x = dataStream.readInt();
		int y = dataStream.readInt();
		int z = dataStream.readInt();

		for(int i = 0; i < 50; i++)
		{
			world.spawnParticle("portal", x + random.nextFloat(), y + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
			world.spawnParticle("portal", x + random.nextFloat(), y + 1 + random.nextFloat(), z + random.nextFloat(), 0.0F, 0.0F, 0.0F);
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(coord4D.xCoord);
		dataStream.writeInt(coord4D.yCoord);
		dataStream.writeInt(coord4D.zCoord);
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void read(ChannelHandlerContext ctx, ByteBuf buffer)
	{

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
