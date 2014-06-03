package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.item.ItemElectricBow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketElectricBowState extends MekanismPacket
{
	public boolean fireMode;
	
	public PacketElectricBowState() {}

	public PacketElectricBowState(boolean state)
	{
		fireMode = state;
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeBoolean(fireMode);
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		boolean state = dataStream.readBoolean();

		ItemStack itemstack = player.getCurrentEquippedItem();

		if(itemstack != null && itemstack.getItem() instanceof ItemElectricBow)
		{
			((ItemElectricBow)itemstack.getItem()).setFireState(itemstack, state);
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
