package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.item.ItemPortableTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class PacketStatusUpdate extends MekanismPacket
{
	public int status;

	public PacketStatusUpdate(int state)
	{
		status = state;
	}

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		ItemStack currentStack = player.getCurrentEquippedItem();

		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setStatus(currentStack, dataStream.readInt());
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(status);
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
