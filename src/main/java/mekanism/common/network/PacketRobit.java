package mekanism.common.network;

import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.PacketRobit.RobitMessage;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

public class PacketRobit implements IMessageHandler<RobitMessage, IMessage>
{
	@Override
	public IMessage onMessage(RobitMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.activeType == RobitPacketType.GUI)
		{
			if(message.guiType == 0)
			{
				player.openGui(Mekanism.instance, 21, player.worldObj, message.entityId, 0, 0);
			}
			else if(message.guiType == 1)
			{
				player.openGui(Mekanism.instance, 22, player.worldObj, message.entityId, 0, 0);
			}
			else if(message.guiType == 2)
			{
				player.openGui(Mekanism.instance, 23, player.worldObj, message.entityId, 0, 0);
			}
			else if(message.guiType == 3)
			{
				player.openGui(Mekanism.instance, 24, player.worldObj, message.entityId, 0, 0);
			}
			else if(message.guiType == 4)
			{
				player.openGui(Mekanism.instance, 25, player.worldObj, message.entityId, 0, 0);
			}
		}
		else if(message.activeType == RobitPacketType.FOLLOW)
		{
			EntityRobit robit = (EntityRobit)player.worldObj.getEntityByID(message.entityId);

			if(robit != null)
			{
				robit.setFollowing(!robit.getFollowing());
			}
		}
		else if(message.activeType == RobitPacketType.NAME)
		{
			EntityRobit robit = (EntityRobit)player.worldObj.getEntityByID(message.entityId);

			if(robit != null)
			{
				robit.setName(message.name);
			}
		}
		else if(message.activeType == RobitPacketType.GO_HOME)
		{
			EntityRobit robit = (EntityRobit)player.worldObj.getEntityByID(message.entityId);

			if(robit != null)
			{
				robit.goHome();
			}
		}
		else if(message.activeType == RobitPacketType.DROP_PICKUP)
		{
			EntityRobit robit = (EntityRobit)player.worldObj.getEntityByID(message.entityId);

			if(robit != null)
			{
				robit.setDropPickup(!robit.getDropPickup());
			}
		}
		
		return null;
	}
	
	public static class RobitMessage implements IMessage
	{
		public RobitPacketType activeType;
	
		public int guiType;
		public int entityId;
	
		public String name;
		
		public RobitMessage() {}
	
		public RobitMessage(RobitPacketType type, int i1, int i2, String s)
		{
			activeType = type;
	
			switch(activeType)
			{
				case GUI:
					guiType = i1;
					entityId = i2;
					break;
				case FOLLOW:
					entityId = i1;
					break;
				case NAME:
					name = s;
					entityId = i1;
					break;
				case GO_HOME:
					entityId = i1;
					break;
				case DROP_PICKUP:
					entityId = i1;
					break;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(activeType.ordinal());
	
			switch(activeType)
			{
				case GUI:
					dataStream.writeInt(guiType);
					dataStream.writeInt(entityId);
					break;
				case FOLLOW:
					dataStream.writeInt(entityId);
					break;
				case NAME:
					PacketHandler.writeString(dataStream, name);
					dataStream.writeInt(entityId);
					break;
				case GO_HOME:
					dataStream.writeInt(entityId);
					break;
				case DROP_PICKUP:
					dataStream.writeInt(entityId);
					break;
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			activeType = RobitPacketType.values()[dataStream.readInt()];
	
			if(activeType == RobitPacketType.GUI)
			{
				guiType = dataStream.readInt();
				entityId = dataStream.readInt();
			}
			else if(activeType == RobitPacketType.FOLLOW)
			{
				entityId = dataStream.readInt();
			}
			else if(activeType == RobitPacketType.NAME)
			{
				name = PacketHandler.readString(dataStream);
				entityId = dataStream.readInt();
			}
			else if(activeType == RobitPacketType.GO_HOME)
			{
				entityId = dataStream.readInt();
			}
			else if(activeType == RobitPacketType.DROP_PICKUP)
			{
				entityId = dataStream.readInt();
			}
		}
	}
	
	public static enum RobitPacketType
	{
		GUI,
		FOLLOW,
		NAME,
		GO_HOME,
		DROP_PICKUP;
	}
}
