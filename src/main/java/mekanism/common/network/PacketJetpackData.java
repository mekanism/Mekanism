package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemJetpack.JetpackMode;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class PacketJetpackData implements IMessageHandler<JetpackDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(JetpackDataMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		PacketHandler.handlePacket(() ->
        {
            if(message.packetType == JetpackPacket.UPDATE)
            {
                if(message.value)
                {
                    Mekanism.jetpackOn.add(message.userId);
                }
                else {
                    Mekanism.jetpackOn.remove(message.userId);
                }

                if(!player.world.isRemote)
                {
                    Mekanism.packetHandler.sendToDimension(new JetpackDataMessage(JetpackPacket.UPDATE, message.userId, message.value), player.world.provider.getDimension());
                }
            }
            else if(message.packetType == JetpackPacket.MODE)
            {
                ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

                if(!stack.isEmpty() && stack.getItem() instanceof ItemJetpack)
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
        }, player);
		
		return null;
	}
	
	public static class JetpackDataMessage implements IMessage
	{
		public JetpackPacket packetType;
	
		public UUID userId;
		public boolean value;
		
		public JetpackDataMessage() {}
	
		public JetpackDataMessage(JetpackPacket type, UUID name, boolean state)
		{
			packetType = type;
			value = state;
	
			if(packetType == JetpackPacket.UPDATE)
			{
				userId = name;
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
				PacketHandler.writeUUID(dataStream, userId);
				dataStream.writeBoolean(value);
			}
			else if(packetType == JetpackPacket.FULL)
			{
				dataStream.writeInt(Mekanism.jetpackOn.size());

				synchronized(Mekanism.jetpackOn)
				{
					for(UUID uuid : Mekanism.jetpackOn)
					{
						PacketHandler.writeUUID(dataStream, uuid);
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
				userId = PacketHandler.readUUID(dataStream);
				value = dataStream.readBoolean();
			}
			else if(packetType == JetpackPacket.FULL)
			{
				Mekanism.jetpackOn.clear();
	
				int amount = dataStream.readInt();
	
				for(int i = 0; i < amount; i++)
				{
					Mekanism.jetpackOn.add(PacketHandler.readUUID(dataStream));
				}
			}
		}
	}
	
	public enum JetpackPacket
	{
		UPDATE,
		FULL,
		MODE
    }
}
