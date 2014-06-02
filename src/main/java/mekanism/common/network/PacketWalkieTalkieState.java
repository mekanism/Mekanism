package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemWalkieTalkie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketWalkieTalkieState extends MekanismPacket
{
	public int channel;

	public PacketWalkieTalkieState(Object... data)
	{
		channel = (Integer)data[0];
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		int chan = dataStream.readInt();

		ItemStack itemstack = player.getCurrentEquippedItem();

		if(itemstack != null && itemstack.getItem() instanceof ItemWalkieTalkie)
		{
			((ItemWalkieTalkie)itemstack.getItem()).setChannel(itemstack, chan);
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(channel);
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
