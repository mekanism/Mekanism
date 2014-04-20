package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemScubaTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = ScubaTankPacket.values()[dataStream.readInt()];

		if(packetType == ScubaTankPacket.FULL)
		{
			Mekanism.gasmaskOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.gasmaskOn.add(dataStream.readUTF());
			}
		}
		else if(packetType == ScubaTankPacket.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();

			if(value)
			{
				Mekanism.gasmaskOn.add(username);
			}
			else {
				Mekanism.gasmaskOn.remove(username);
			}

			if(!world.isRemote)
			{
				Mekanism.packetPipeline.sendToDimension(new PacketScubaTankData(ScubaTankPacket.UPDATE, username, value), world.provider.dimensionId);
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

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());

		if(packetType == ScubaTankPacket.UPDATE)
		{
			dataStream.writeUTF(username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == ScubaTankPacket.FULL)
		{
			dataStream.writeInt(Mekanism.gasmaskOn.size());

			for(String username : Mekanism.gasmaskOn)
			{
				dataStream.writeUTF(username);
			}
		}
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

	public static enum ScubaTankPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
