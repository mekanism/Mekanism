package mekanism.common.network;

import mekanism.common.PacketHandler;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketWalkieTalkieState.WalkieTalkieStateMessage;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketWalkieTalkieState implements IMessageHandler<WalkieTalkieStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(WalkieTalkieStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemWalkieTalkie)
		{
			((ItemWalkieTalkie)itemstack.getItem()).setChannel(itemstack, message.channel);
		}
		
		return null;
	}
	
	public static class WalkieTalkieStateMessage implements IMessage
	{
		public int channel;
		
		public WalkieTalkieStateMessage() {}
	
		public WalkieTalkieStateMessage(int chan)
		{
			channel = chan;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(channel);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			channel = dataStream.readInt();
		}
	}
}
