package mekanism.common.network;

import mekanism.common.PacketHandler;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.network.PacketPortableTankState.PortableTankStateMessage;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketPortableTankState implements IMessageHandler<PortableTankStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(PortableTankStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemBlockMachine)
		{
			((ItemBlockMachine)itemstack.getItem()).setBucketMode(itemstack, message.bucketMode);
		}
		
		return null;
	}
	
	public static class PortableTankStateMessage implements IMessage
	{
		public boolean bucketMode;
		
		public PortableTankStateMessage() {}
	
		public PortableTankStateMessage(boolean state)
		{
			bucketMode = state;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeBoolean(bucketMode);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			bucketMode = dataStream.readBoolean();
		}
	}
}