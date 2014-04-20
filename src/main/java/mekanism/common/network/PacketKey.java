package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketKey extends MekanismPacket
{
	public int key;
	public boolean add;

	public PacketKey(int k, boolean a)
	{
		key = k;
		add = a;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
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

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(key);
		dataStream.writeBoolean(add);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
	{

	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer)
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
