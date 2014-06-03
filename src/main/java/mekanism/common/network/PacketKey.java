package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;

public class PacketKey extends MekanismPacket
{
	public int key;
	public boolean add;
	
	public PacketKey() {}

	public PacketKey(int k, boolean a)
	{
		key = k;
		add = a;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(key);
		dataStream.writeBoolean(add);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		key = dataStream.readInt();
		add = dataStream.readBoolean();

		if(add)
		{
			Mekanism.keyMap.add(player, key);
		}
		else {
			Mekanism.keyMap.remove(player, key);
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
