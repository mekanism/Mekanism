package mekanism.common.network;

import mekanism.common.PacketHandler;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.network.PacketConfiguratorState.ConfiguratorStateMessage;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketConfiguratorState implements IMessageHandler<ConfiguratorStateMessage, IMessage>
{
	@Override
	public IMessage onMessage(ConfiguratorStateMessage message, MessageContext context) 
	{
		ItemStack itemstack = PacketHandler.getPlayer(context).getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemConfigurator)
		{
			((ItemConfigurator)itemstack.getItem()).setState(itemstack, (byte)message.state);
		}
		
		return null;
	}
	
	public static class ConfiguratorStateMessage implements IMessage
	{
		public byte state;
		
		public ConfiguratorStateMessage() {}
	
		public ConfiguratorStateMessage(byte s)
		{
			state = s;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeByte(state);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			state = dataStream.readByte();
		}
	}
}
