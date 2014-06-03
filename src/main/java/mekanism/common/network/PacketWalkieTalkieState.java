package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.item.ItemWalkieTalkie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketWalkieTalkieState extends MekanismPacket
{
	public int channel;
	
	public PacketWalkieTalkieState() {}

	public PacketWalkieTalkieState(int chan)
	{
		channel = chan;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(channel);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		channel = dataStream.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player)
	{
		
	}

	@Override
	public void handleServerSide(EntityPlayer player)
	{
		ItemStack itemstack = player.getCurrentEquippedItem();

		if(itemstack != null && itemstack.getItem() instanceof ItemWalkieTalkie)
		{
			((ItemWalkieTalkie)itemstack.getItem()).setChannel(itemstack, channel);
		}
	}
}
