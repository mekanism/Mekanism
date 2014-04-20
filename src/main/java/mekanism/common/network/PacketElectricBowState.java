package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemElectricBow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketElectricBowState extends MekanismPacket
{
	public boolean fireMode;

	public PacketElectricBowState(boolean state)
	{
		fireMode = state;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		boolean state = dataStream.readBoolean();

		ItemStack itemstack = player.getCurrentEquippedItem();

		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
		{
			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, state);
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeBoolean(fireMode);
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
