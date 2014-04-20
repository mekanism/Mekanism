package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemJetpack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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

	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception
	{
		packetType = JetpackPacket.values()[dataStream.readInt()];

		if(packetType == JetpackPacket.FULL)
		{
			Mekanism.jetpackOn.clear();

			int amount = dataStream.readInt();

			for(int i = 0; i < amount; i++)
			{
				Mekanism.jetpackOn.add(dataStream.readUTF());
			}
		}
		else if(packetType == JetpackPacket.UPDATE)
		{
			String username = dataStream.readUTF();
			boolean value = dataStream.readBoolean();

			if(value)
			{
				Mekanism.jetpackOn.add(username);
			}
			else {
				Mekanism.jetpackOn.remove(username);
			}

			if(!world.isRemote)
			{
				Mekanism.packetPipeline.sendToDimension(new PacketJetpackData(JetpackPacket.UPDATE, username, value), world.provider.dimensionId);
			}
		}
		else if(packetType == JetpackPacket.MODE)
		{
			ItemStack stack = player.getCurrentItemOrArmor(3);

			if(stack != null && stack.getItem() instanceof ItemJetpack)
			{
				((ItemJetpack)stack.getItem()).incrementMode(stack);
			}
		}
	}

	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeInt(packetType.ordinal());

		if(packetType == JetpackPacket.UPDATE)
		{
			dataStream.writeUTF(username);
			dataStream.writeBoolean(value);
		}
		else if(packetType == JetpackPacket.FULL)
		{
			dataStream.writeInt(Mekanism.jetpackOn.size());

			for(String username : Mekanism.jetpackOn)
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

	public static enum JetpackPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
