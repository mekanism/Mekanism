package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketPortableTankState.PortableTankStateMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketPortableTankState implements IMessageHandler<PortableTankStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(PortableTankStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getHeldItem(message.currentHand);
		
		if(itemstack != null && itemstack.getItem() instanceof ItemBlockMachine)
		{
			((ItemBlockMachine)itemstack.getItem()).setBucketMode(itemstack, message.bucketMode);
		}
		
		return null;
	}
	
	public static class PortableTankStateMessage implements IMessage
	{
		public EnumHand currentHand;
		
		public boolean bucketMode;
		
		public PortableTankStateMessage() {}
	
		public PortableTankStateMessage(EnumHand hand, boolean state)
		{
			currentHand = hand;
			bucketMode = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(currentHand.ordinal());
			dataStream.writeBoolean(bucketMode);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			currentHand = EnumHand.values()[dataStream.readInt()];
			bucketMode = dataStream.readBoolean();
		}
	}
}