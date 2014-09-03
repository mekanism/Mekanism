package mekanism.common.network;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.network.PacketKey.KeyMessage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketKey implements IMessageHandler<KeyMessage, IMessage>
{
	@Override
	public IMessage onMessage(KeyMessage message, MessageContext context) 
	{
		if(message.add)
		{
			Mekanism.keyMap.add(PacketHandler.getPlayer(context), message.key);
		}
		else {
			Mekanism.keyMap.remove(PacketHandler.getPlayer(context), message.key);
		}
		
		return null;
	}
	
	public static class KeyMessage implements IMessage
	{
		public int key;
		public boolean add;
		
		public KeyMessage() {}
	
		public KeyMessage(int k, boolean a)
		{
			key = k;
			add = a;
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(key);
			dataStream.writeBoolean(add);
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			key = dataStream.readInt();
			add = dataStream.readBoolean();
		}
	}
}
