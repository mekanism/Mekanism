package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketScubaTankData implements IMessageHandler<ScubaTankDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(ScubaTankDataMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.packetType == ScubaTankPacket.UPDATE)
		{
			if(message.value)
			{
				Mekanism.gasmaskOn.add(message.username);
			}
			else {
				Mekanism.gasmaskOn.remove(message.username);
			}

			if(!player.world.isRemote)
			{
				Mekanism.packetHandler.sendToDimension(new ScubaTankDataMessage(ScubaTankPacket.UPDATE, message.username, message.value), player.world.provider.getDimension());
			}
		}
		else if(message.packetType == ScubaTankPacket.MODE)
		{
			ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

			if(!stack.isEmpty() && stack.getItem() instanceof ItemScubaTank)
			{
				((ItemScubaTank)stack.getItem()).toggleFlowing(stack);
			}
		}
		
		return null;
	}
	
	public static class ScubaTankDataMessage implements IMessage
	{
		public ScubaTankPacket packetType;
	
		public String username;
		public boolean value;
		
		public ScubaTankDataMessage() {}
	
		public ScubaTankDataMessage(ScubaTankPacket type, String name, boolean state)
		{
			packetType = type;
	
			if(packetType == ScubaTankPacket.UPDATE)
			{
				username = name;
				value = state;
			}
		}
	
		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());
	
			if(packetType == ScubaTankPacket.UPDATE)
			{
				PacketHandler.writeString(dataStream, username);
				dataStream.writeBoolean(value);
			}
			else if(packetType == ScubaTankPacket.FULL)
			{
				dataStream.writeInt(Mekanism.gasmaskOn.size());

				synchronized(Mekanism.gasmaskOn)
				{
					for (String name : Mekanism.gasmaskOn)
					{
						PacketHandler.writeString(dataStream, name);
					}
				}
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = ScubaTankPacket.values()[dataStream.readInt()];
	
			if(packetType == ScubaTankPacket.FULL)
			{
				Mekanism.gasmaskOn.clear();
	
				int amount = dataStream.readInt();
	
				for(int i = 0; i < amount; i++)
				{
					Mekanism.gasmaskOn.add(PacketHandler.readString(dataStream));
				}
			}
			else if(packetType == ScubaTankPacket.UPDATE)
			{
				username = PacketHandler.readString(dataStream);
				value = dataStream.readBoolean();
			}
		}
	}
	
	public enum ScubaTankPacket
	{
		UPDATE,
		FULL,
		MODE
    }
}
