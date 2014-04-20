package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.api.ItemInfo;
import mekanism.api.MekanismAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketBoxBlacklist extends MekanismPacket
{
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		MekanismAPI.getBoxIgnore().clear();

		int amount = dataStream.readInt();

		for(int i = 0; i < amount; i++)
		{
			MekanismAPI.addBoxBlacklist(dataStream.readInt(), dataStream.readInt());
		}

		System.out.println("[Mekanism] Received Cardboard Box blacklist entries from server (" + amount + " total)");
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(MekanismAPI.getBoxIgnore().size());

		for(ItemInfo info : MekanismAPI.getBoxIgnore())
		{
			dataStream.writeInt(info.id);
			dataStream.writeInt(info.meta);
		}
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
