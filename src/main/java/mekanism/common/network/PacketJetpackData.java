package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketJetpackData extends MekanismPacket
{
	public JetpackPacket packetType;

	public String username;
	public boolean value;

	public PacketJetpackData(JetpackPacket type, String name, boolean state)
	{
		packetType = type;

		if(packetType == JetpackPacket.UPDATE)
		{
			username = name;
			value = state;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(packetType.ordinal());

		if(packetType == JetpackPacket.UPDATE)
		{
			PacketHandler.writeString(dataStream, username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == JetpackPacket.FULL)
		{
			dataStream.writeInt(Mekanism.jetpackOn.size());

			for(String username : Mekanism.jetpackOn)
			{
				PacketHandler.writeString(dataStream, username);
			}
		}
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		packetType = JetpackPacket.values()[dataStream.readInt()];

		if(packetType == JetpackPacket.FULL)
		{
			Mekanism.jetpackOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.jetpackOn.add(PacketHandler.readString(dataStream));
			}
		}
		else if(packetType == JetpackPacket.UPDATE)
		{
			String username = PacketHandler.readString(dataStream);
			boolean value = dataStream.readBoolean();

			if(value)
			{
				Mekanism.jetpackOn.add(username);
			}
			else {
				Mekanism.jetpackOn.remove(username);
			}

			if(!player.worldObj.isRemote)
			{
				Mekanism.packetPipeline.sendToDimension(new PacketJetpackData(JetpackPacket.UPDATE, username, value), player.worldObj.provider.dimensionId);
			}
		}
		else if(packetType == JetpackPacket.MODE)
		{
			ItemStack stack = player.getEquipmentInSlot(3);

			if(stack != null && stack.getItem() instanceof ItemJetpack)
			{
				((ItemJetpack)stack.getItem()).incrementMode(stack);
			}
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

	public static enum JetpackPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
