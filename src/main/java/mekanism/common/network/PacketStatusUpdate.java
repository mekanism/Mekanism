package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketStatusUpdate extends MekanismPacket
{
	public int status;
	
	public PacketStatusUpdate() {}

	public PacketStatusUpdate(int state)
	{
		status = state;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(status);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		ItemStack currentStack = player.getCurrentEquippedItem();

		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setStatus(currentStack, dataStream.readInt());
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
