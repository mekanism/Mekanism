package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketFlamethrowerData implements IMessageHandler<FlamethrowerDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(FlamethrowerDataMessage message, MessageContext context)
	{
		EntityPlayer player = PacketHandler.getPlayer(context);

		PacketHandler.handlePacket(() ->
		{
			if(message.packetType == FlamethrowerPacket.UPDATE)
			{
				if(message.value)
				{
					Mekanism.flamethrowerActive.add(message.userId);
				}
				else
				{
					Mekanism.flamethrowerActive.remove(message.userId);
				}

				if(!player.world.isRemote)
				{
					Mekanism.packetHandler.sendToDimension(new FlamethrowerDataMessage(FlamethrowerPacket.UPDATE, message.currentHand, message.userId, message.value), player.world.provider.getDimension());
				}
			}
			else if(message.packetType == FlamethrowerPacket.MODE)
			{
				ItemStack stack = player.getHeldItem(message.currentHand);

				if(!stack.isEmpty() && stack.getItem() instanceof ItemFlamethrower)
				{
					((ItemFlamethrower) stack.getItem()).incrementMode(stack);
				}
			}
		}, player);

		return null;
	}

	public static class FlamethrowerDataMessage implements IMessage
	{
		public FlamethrowerPacket packetType;

		public EnumHand currentHand;
		public UUID userId;
		public boolean value;

		public FlamethrowerDataMessage()
		{
		}

		public FlamethrowerDataMessage(FlamethrowerPacket type, EnumHand hand, UUID name, boolean state)
		{
			packetType = type;

			if(type == FlamethrowerPacket.UPDATE)
			{
				userId = name;
				value = state;
			}
			else if(type == FlamethrowerPacket.MODE)
			{
				currentHand = hand;
			}
		}

		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());

			if(packetType == FlamethrowerPacket.UPDATE)
			{
				PacketHandler.writeUUID(dataStream, userId);
				dataStream.writeBoolean(value);
			}
			else
			{
				dataStream.writeInt(currentHand.ordinal());
			}
		}

		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = FlamethrowerPacket.values()[dataStream.readInt()];

			if(packetType == FlamethrowerPacket.UPDATE)
			{
				userId = PacketHandler.readUUID(dataStream);
				value = dataStream.readBoolean();
			}
			else if(packetType == FlamethrowerPacket.MODE)
			{
				currentHand = EnumHand.values()[dataStream.readInt()];
			}
		}

	}

	public enum FlamethrowerPacket
	{
		UPDATE,
		MODE
	}
}
