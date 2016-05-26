package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.network.PacketWalkieTalkieState.WalkieTalkieStateMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketWalkieTalkieState implements IMessageHandler<WalkieTalkieStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(WalkieTalkieStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getHeldItem(message.currentHand);
		
		if(itemstack != null && itemstack.getItem() instanceof ItemWalkieTalkie)
		{
			((ItemWalkieTalkie)itemstack.getItem()).setChannel(itemstack, message.channel);
		}
		
		return null;
	}
	
	public static class WalkieTalkieStateMessage implements IMessage
	{
		public EnumHand currentHand;
		
		public int channel;
		
		public WalkieTalkieStateMessage() {}
	
		public WalkieTalkieStateMessage(EnumHand hand, int chan)
		{
			currentHand = hand;
			channel = chan;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(currentHand.ordinal());
			dataStream.writeInt(channel);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			currentHand = EnumHand.values()[dataStream.readInt()];
			channel = dataStream.readInt();
		}
	}
}
