package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemConfigurator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketConfiguratorState extends MekanismPacket
{
	public byte state;

	public PacketConfiguratorState(byte s)
	{
		state = s;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		byte state = dataStream.readByte();

		ItemStack itemstack = player.getCurrentEquippedItem();

		if(itemstack != null && itemstack.getItem() instanceof ItemConfigurator)
		{
			((ItemConfigurator)itemstack.getItem()).setState(itemstack, (byte)state);
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeByte(state);
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
