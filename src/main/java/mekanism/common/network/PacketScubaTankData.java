package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PacketScubaTankData extends MekanismPacket
{
	public ScubaTankPacket packetType;

	public String username;
	public boolean value;

	public PacketScubaTankData(ScubaTankPacket type, String name, boolean state)
	{
		packetType = type;

		if(packetType == ScubaTankPacket.UPDATE)
		{
			username = name;
			value = state;
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, ByteBuf dataStream)
	{
		dataStream.writeInt(packetType.ordinal());

		if(packetType == ScubaTankPacket.UPDATE)
		{
			PacketHandler.writeString(dataStream, username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == ScubaTankPacket.FULL)
		{
			dataStream.writeInt(Mekanism.gasmaskOn.size());

			for(String name : Mekanism.gasmaskOn)
			{
				PacketHandler.writeString(dataStream, name);
			}
		}
	}

	@Override
	public void read(ChannelHandlerContext ctx, EntityPlayer player, ByteBuf dataStream)
	{
		packetType = ScubaTankPacket.values()[dataStream.readInt()];

		if(packetType == ScubaTankPacket.FULL)
		{
			Mekanism.gasmaskOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.gasmaskOn.add(PacketHandler.readString(dataStream));
			}
		}
		else if(packetType == ScubaTankPacket.UPDATE)
		{
			String username = PacketHandler.readString(dataStream);
			boolean value = dataStream.readBoolean();

			if(value)
			{
				Mekanism.gasmaskOn.add(username);
			}
			else {
				Mekanism.gasmaskOn.remove(username);
			}

			if(!player.worldObj.isRemote)
			{
				Mekanism.packetPipeline.sendToDimension(new PacketScubaTankData(ScubaTankPacket.UPDATE, username, value), player.worldObj.provider.dimensionId);
			}
		}
		else if(packetType == ScubaTankPacket.MODE)
		{
			ItemStack stack = player.getEquipmentInSlot(3);

			if(stack != null && stack.getItem() instanceof ItemScubaTank)
			{
				((ItemScubaTank)stack.getItem()).toggleFlowing(stack);
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

	public static enum ScubaTankPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
