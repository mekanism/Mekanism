package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.Frequency;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.security.SecurityData;
import mekanism.common.security.SecurityFrequency;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSecurityUpdate implements IMessageHandler<SecurityUpdateMessage, IMessage>
{
	@Override
	public IMessage onMessage(SecurityUpdateMessage message, MessageContext context) 
	{
		if(message.packetType == SecurityPacket.UPDATE)
		{
			MekanismClient.clientSecurityMap.put(message.playerUsername, message.securityData);
		}
		
		return null;
	}
	
	public static class SecurityUpdateMessage implements IMessage
	{
		public SecurityPacket packetType;
		
		public String playerUsername;
		public SecurityData securityData;
		
		public SecurityUpdateMessage() {}
	
		public SecurityUpdateMessage(SecurityPacket type, String username, SecurityData data)
		{
			packetType = type;
			
			if(packetType == SecurityPacket.UPDATE)
			{
				playerUsername = username;
				securityData = data;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
			
			if(packetType == SecurityPacket.UPDATE)
			{
				PacketHandler.writeString(dataStream, playerUsername);
				securityData.write(dataStream);
			}
			else if(packetType == SecurityPacket.FULL)
			{
				List<SecurityFrequency> frequencies = new ArrayList<SecurityFrequency>();
				
				for(Frequency frequency : Mekanism.securityFrequencies.getFrequencies())
				{
					if(frequency instanceof SecurityFrequency)
					{
						frequencies.add((SecurityFrequency)frequency);
					}
				}
				
				dataStream.writeInt(frequencies.size());
				
				for(SecurityFrequency frequency : frequencies)
				{
					PacketHandler.writeString(dataStream, frequency.owner);
					new SecurityData(frequency).write(dataStream);
				}
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = SecurityPacket.values()[dataStream.readInt()];
			
			if(packetType == SecurityPacket.UPDATE)
			{
				playerUsername = PacketHandler.readString(dataStream);
				securityData = SecurityData.read(dataStream);
			}
			else if(packetType == SecurityPacket.FULL)
			{
				MekanismClient.clientSecurityMap.clear();
				
				int amount = dataStream.readInt();
				
				for(int i = 0; i < amount; i++)
				{
					String owner = PacketHandler.readString(dataStream);
					SecurityData data = SecurityData.read(dataStream);
					
					MekanismClient.clientSecurityMap.put(owner, data);
				}
			}
		}
	}
	
	public static enum SecurityPacket
	{
		UPDATE,
		FULL;
	}
}
