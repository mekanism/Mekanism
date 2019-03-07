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

import java.util.HashSet;
import java.util.Set;

public class PacketScubaTankData implements IMessageHandler<ScubaTankDataMessage, IMessage>
{
	@Override
	public IMessage onMessage(ScubaTankDataMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		
		if(message.packetType == ScubaTankPacket.UPDATE) {
			Mekanism.playerState.setGasmaskState(message.username, message.value, false);

			// If we got this on the server, relay out to all players in the same dimension
			// TODO: Why is this a dimensional thing?!
			if(!player.world.isRemote) {
				Mekanism.packetHandler.sendToDimension(message, player.world.provider.getDimension());
			}
		}
		else if(message.packetType == ScubaTankPacket.MODE) {
			// Use has changed the mode of their gasmask; update it
			ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			if(!stack.isEmpty() && stack.getItem() instanceof ItemScubaTank) {
				((ItemScubaTank)stack.getItem()).toggleFlowing(stack);
			}
		}
		else if (message.packetType == ScubaTankPacket.FULL) {
			// This is a full sync; merge into our player state
			Mekanism.playerState.setActiveGasmasks(message.activeGasmasks);
		}
		
		return null;
	}
	
	public static class ScubaTankDataMessage implements IMessage
	{
		protected ScubaTankPacket packetType;

		protected Set<String> activeGasmasks;
	
		protected String username;
		protected boolean value;
		
		public ScubaTankDataMessage() {}

		public ScubaTankDataMessage(ScubaTankPacket type) { packetType = type; }

		public static ScubaTankDataMessage MODE_CHANGE(boolean change) {
			ScubaTankDataMessage m = new ScubaTankDataMessage(ScubaTankPacket.MODE);
			m.value = change;
			return m;
		}

		public static ScubaTankDataMessage UPDATE(String name, boolean state) {
			ScubaTankDataMessage m = new ScubaTankDataMessage(ScubaTankPacket.UPDATE);
			m.username = name;
			m.value = state;
			return m;
		}

		public static ScubaTankDataMessage FULL(Set<String> activeNames) {
			ScubaTankDataMessage m = new ScubaTankDataMessage((ScubaTankPacket.FULL));
			m.activeGasmasks = activeNames;
			return m;
		}

		@Override
		public void toBytes(ByteBuf dataStream)
		{
			dataStream.writeInt(packetType.ordinal());

			if (packetType == ScubaTankPacket.MODE) {
				dataStream.writeBoolean(value);
			}
			else if(packetType == ScubaTankPacket.UPDATE) {
				PacketHandler.writeString(dataStream, username);
				dataStream.writeBoolean(value);
			}
			else if(packetType == ScubaTankPacket.FULL) {
				dataStream.writeInt(activeGasmasks.size());
				for (String name : activeGasmasks) {
					PacketHandler.writeString(dataStream, name);
				}
			}
		}
	
		@Override
		public void fromBytes(ByteBuf dataStream)
		{
			packetType = ScubaTankPacket.values()[dataStream.readInt()];

			if (packetType == ScubaTankPacket.MODE) {
				value = dataStream.readBoolean();
			}
			else if(packetType == ScubaTankPacket.UPDATE) {
				username = PacketHandler.readString(dataStream);
				value = dataStream.readBoolean();
			}
			else if(packetType == ScubaTankPacket.FULL) {
				activeGasmasks = new HashSet<>();

				int amount = dataStream.readInt();
				for(int i = 0; i < amount; i++) {
					activeGasmasks.add(PacketHandler.readString(dataStream));
				}
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
