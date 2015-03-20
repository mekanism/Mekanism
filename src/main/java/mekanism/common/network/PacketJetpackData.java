package mekanism.common.network;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketJetpackData implements IMessageHandler<JetpackDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(JetpackDataMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.packetType == JetpackPacket.UPDATE)
		{
			if(message.value)
			{
				Mekanism.jetpackOn.add(message.username);
			}
			else {
				Mekanism.jetpackOn.remove(message.username);
			}

			if(!player.worldObj.isRemote)
			{
				Mekanism.packetHandler.sendToDimension(new JetpackDataMessage(JetpackPacket.UPDATE, message.username, message.value), player.worldObj.provider.dimensionId);
			}
		}
		else if(message.packetType == JetpackPacket.MODE)
		{
			ItemStack stack = player.getEquipmentInSlot(3);

			if(stack != null && stack.getItem() instanceof ItemJetpack)
			{
				if(!message.value)
				{
					((ItemJetpack)stack.getItem()).incrementMode(stack);
				}
				else {
					((ItemJetpack)stack.getItem()).setMode(stack, JetpackMode.DISABLED);
				}
			}
		}
		
		return null;
	}
	
	public static class JetpackDataMessage implements IMessage
	{
		public JetpackPacket packetType;
	
		public String username;
		public boolean value;
		
		public JetpackDataMessage() {}
	
		public JetpackDataMessage(JetpackPacket type, String name, boolean state)
		{
			packetType = type;
			value = state;
	
			if(packetType == JetpackPacket.UPDATE)
			{
				username = name;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
	
			if(packetType == JetpackPacket.MODE)
			{
				dataStream.writeBoolean(value);
			}
			else if(packetType == JetpackPacket.UPDATE)
			{
				PacketHandler.writeString(dataStream, username);
				dataStream.writeBoolean(value);
			}
			else if(packetType == JetpackPacket.FULL)
			{
				dataStream.writeInt(Mekanism.jetpackOn.size());

				synchronized(Mekanism.jetpackOn)
				{
					for(String username : Mekanism.jetpackOn)
					{
						PacketHandler.writeString(dataStream, username);
					}
				}
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = JetpackPacket.values()[dataStream.readInt()];
	
			if(packetType == JetpackPacket.MODE)
			{
				value = dataStream.readBoolean();
			}
			else if(packetType == JetpackPacket.UPDATE)
			{
				username = PacketHandler.readString(dataStream);
				value = dataStream.readBoolean();
			}
			else if(packetType == JetpackPacket.FULL)
			{
				Mekanism.jetpackOn.clear();
	
				int amount = dataStream.readInt();
	
				for(int i = 0; i < amount; i++)
				{
					Mekanism.jetpackOn.add(PacketHandler.readString(dataStream));
				}
			}
		}
	}
	
	public static enum JetpackPacket
	{
		UPDATE,
		FULL,
		MODE;
	}
}
