package mekanism.common.network;

import mekanism.common.PacketHandler;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketStatusUpdate.StatusUpdateMessage;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketStatusUpdate implements IMessageHandler<StatusUpdateMessage, IMessage>
{
	@Override
	public IMessage onMessage(StatusUpdateMessage message, MessageContext context) 
	{
		ItemStack currentStack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setStatus(currentStack, message.status);
		}
		
		return null;
	}
	
	public static class StatusUpdateMessage implements IMessage
	{
		public int status;
		
		public StatusUpdateMessage() {}
	
		public StatusUpdateMessage(int state)
		{
			status = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(status);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			status = dataStream.readInt();
		}
	}
}
