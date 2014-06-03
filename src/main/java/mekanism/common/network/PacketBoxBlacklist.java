package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.api.ItemInfo;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;

public class PacketBoxBlacklist extends MekanismPacket
{
	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(MekanismAPI.getBoxIgnore().size());

		for(ItemInfo info : MekanismAPI.getBoxIgnore())
		{
			dataStream.writeInt(Block.getIdFromBlock(info.block));
			dataStream.writeInt(info.meta);
		}
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		MekanismAPI.getBoxIgnore().clear();

		int amount = dataStream.readInt();

		for(int i = 0; i < amount; i++)
		{
			MekanismAPI.addBoxBlacklist(Block.getBlockById(dataStream.readInt()), dataStream.readInt());
		}

		Mekanism.logger.info("Received Cardboard Box blacklist entries from server (" + amount + " total)");
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
