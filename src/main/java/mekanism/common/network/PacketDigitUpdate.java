package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketDigitUpdate extends MekanismPacket
{
	public int index;
	public int digit;

	public PacketDigitUpdate(int ind, int dig)
	{
		index = ind;
		digit = dig;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(index);
		dataStream.writeInt(digit);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		int index = dataStream.readInt();
		int digit = dataStream.readInt();

		ItemStack currentStack = player.getCurrentEquippedItem();

		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setDigit(currentStack, index, digit);
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
