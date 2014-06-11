package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketDigitUpdate.DigitUpdateMessage;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketDigitUpdate implements IMessageHandler<DigitUpdateMessage, IMessage>
{
	@Override
	public IMessage onMessage(DigitUpdateMessage message, MessageContext context) 
	{
		ItemStack currentStack = PacketHandler.getPlayer(context).getCurrentEquippedItem();

		if(currentStack != null && currentStack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)currentStack.getItem();
			item.setDigit(currentStack, message.index, message.digit);
		}
		
		return null;
	}
	
	public static class DigitUpdateMessage implements IMessage
	{
		public int index;
		public int digit;
		
		public DigitUpdateMessage() {}
	
		public DigitUpdateMessage(int ind, int dig)
		{
			index = ind;
			digit = dig;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(index);
			dataStream.writeInt(digit);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			index = dataStream.readInt();
			digit = dataStream.readInt();
		}
	}
}
