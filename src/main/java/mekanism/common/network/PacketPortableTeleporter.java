package mekanism.common.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.PacketHandler;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyManager;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketPortableTeleporter implements IMessageHandler<PortableTeleporterMessage, IMessage>
{
	@Override
	public IMessage onMessage(PortableTeleporterMessage message, MessageContext context) 
	{
		EntityPlayer player = PacketHandler.getPlayer(context);
		ItemStack itemstack = player.getCurrentEquippedItem();
		World world = player.worldObj;
		
		if(itemstack != null && itemstack.getItem() instanceof ItemPortableTeleporter)
		{
			ItemPortableTeleporter item = (ItemPortableTeleporter)itemstack.getItem();
			
			switch(message.packetType)
			{
				case DATA_REQUEST:
					sendDataResponse(message.frequency, world, player, item, itemstack);
					break;
				case DATA_RESPONSE:
					Mekanism.proxy.handleTeleporterUpdate(message);
					break;
				case SET_FREQ:
					FrequencyManager manager1 = getManager(message.frequency.isPublic() ? null : player.getCommandSenderName(), world);
					Frequency toUse = null;
					
					for(Frequency freq : manager1.getFrequencies())
					{
						if(freq.name.equals(message.frequency.name))
						{
							toUse = freq;
							break;
						}
					}
					
					if(toUse == null)
					{
						toUse = new Frequency(message.frequency.name, player.getCommandSenderName()).setPublic(message.frequency.isPublic());
						manager1.addFrequency(toUse);
					}
					
					sendDataResponse(toUse, world, player, item, itemstack);
					
					break;
				case DEL_FREQ:
					FrequencyManager manager = getManager(message.frequency.isPublic() ? null : player.getCommandSenderName(), world);
					manager.remove(message.frequency.name, player.getCommandSenderName());
					
					break;
				case TELEPORT:
					FrequencyManager manager2 = getManager(message.frequency.isPublic() ? null : player.getCommandSenderName(), world);
					Frequency found = null;
					
					for(Frequency freq : manager2.getFrequencies())
					{
						if(message.frequency.name.equals(freq.name))
						{
							found = freq;
							break;
						}
					}
					
					if(found == null)
					{
						break;
					}
					
					Coord4D coords = found.getClosestCoords(new Coord4D(player));
					
					World teleWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(coords.dimensionId);
					TileEntityTeleporter teleporter = (TileEntityTeleporter)coords.getTileEntity(teleWorld);
					
					if(teleporter != null)
					{
						try {
							teleporter.didTeleport.add(player);
							teleporter.teleDelay = 5;
							
							item.setEnergy(itemstack, item.getEnergy(itemstack) - item.calculateEnergyCost(player, coords));
							
							if(player instanceof EntityPlayerMP)
							{
								MekanismUtils.setPrivateValue(((EntityPlayerMP)player).playerNetServerHandler, 0, NetHandlerPlayServer.class, ObfuscatedNames.NetHandlerPlayServer_floatingTickCount);
							}
							
							player.closeScreen();
							
							Mekanism.packetHandler.sendToAllAround(new PortalFXMessage(new Coord4D(player)), coords.getTargetPoint(40D));
							TileEntityTeleporter.teleportPlayerTo((EntityPlayerMP)player, coords, teleporter);
							
							world.playSoundAtEntity(player, "mob.endermen.portal", 1.0F, 1.0F);
							Mekanism.packetHandler.sendToReceivers(new PortalFXMessage(coords), new Range4D(coords));
						} catch(Exception e) {}
					}
					
					break;
			}
		}
		
		return null;
	}
	
	public void sendDataResponse(Frequency given, World world, EntityPlayer player, ItemPortableTeleporter item, ItemStack itemstack)
	{
		List<Frequency> publicFreqs = new ArrayList<Frequency>();
		
		for(Frequency f : getManager(null, world).getFrequencies())
		{
			publicFreqs.add(f);
		}
		
		List<Frequency> privateFreqs = new ArrayList<Frequency>();
		
		for(Frequency f : getManager(player.getCommandSenderName(), world).getFrequencies())
		{
			privateFreqs.add(f);
		}
		
		byte status = 3;
		
		if(given != null)
		{
			FrequencyManager manager = given.isPublic() ? getManager(null, world) : getManager(player.getCommandSenderName(), world);
			boolean found = false;
			
			for(Frequency iterFreq : manager.getFrequencies())
			{
				if(given.equals(iterFreq))
				{
					given = iterFreq;
					found = true;
					break;
				}
			}
			
			if(!found)
			{
				given = null;
			}
		}
		
		if(given != null)
		{
			if(given.activeCoords.size() == 0)
			{
				status = 3;
			}
			else {
				Coord4D coords = given.getClosestCoords(new Coord4D(player));
				double energyNeeded = item.calculateEnergyCost(player, coords);
				
				if(energyNeeded > item.getEnergy(itemstack))
				{
					status = 4;
				}
				else {
					status = 1;
				}
			}
		}
		
		Mekanism.packetHandler.sendTo(new PortableTeleporterMessage(given, status, publicFreqs, privateFreqs), (EntityPlayerMP)player);
	}
	
	public FrequencyManager getManager(String owner, World world)
	{
		if(owner == null)
		{
			return Mekanism.publicTeleporters;
		}
		else {
			if(!Mekanism.privateTeleporters.containsKey(owner))
			{
				FrequencyManager manager = new FrequencyManager(Frequency.class, owner);
				Mekanism.privateTeleporters.put(owner, manager);
				manager.createOrLoad(world);
			}
			
			return Mekanism.privateTeleporters.get(owner);
		}
	}
	
	public static class PortableTeleporterMessage implements IMessage
	{
		public PortableTeleporterPacketType packetType;
		public Frequency frequency;
		public byte status;
		
		public List<Frequency> publicCache = new ArrayList<Frequency>();
		public List<Frequency> privateCache = new ArrayList<Frequency>();
		
		public PortableTeleporterMessage() {}
		
		public PortableTeleporterMessage(PortableTeleporterPacketType type, Frequency freq)
		{
			packetType = type;
			
			if(type == PortableTeleporterPacketType.DATA_REQUEST)
			{
				frequency = freq;
			}
			else if(type == PortableTeleporterPacketType.SET_FREQ)
			{
				frequency = freq;
			}
			else if(type == PortableTeleporterPacketType.DEL_FREQ)
			{
				frequency = freq;
			}
			else if(type == PortableTeleporterPacketType.TELEPORT)
			{
				frequency = freq;
			}
		}
		
		public PortableTeleporterMessage(Frequency freq, byte b, List<Frequency> publicFreqs, List<Frequency> privateFreqs)
		{
			packetType = PortableTeleporterPacketType.DATA_RESPONSE;
			
			frequency = freq;
			status = b;
			
			publicCache = publicFreqs;
			privateCache = privateFreqs;
		}
		
		@Override
		public void toBytes(ByteBuf buffer)
		{
			buffer.writeInt(packetType.ordinal());
			
			if(packetType == PortableTeleporterPacketType.DATA_REQUEST)
			{
				if(frequency != null)
				{
					buffer.writeBoolean(true);
					PacketHandler.writeString(buffer, frequency.name);
					buffer.writeBoolean(frequency.publicFreq);
				}
				else {
					buffer.writeBoolean(false);
				}
			}
			else if(packetType == PortableTeleporterPacketType.DATA_RESPONSE)
			{
				if(frequency != null)
				{
					buffer.writeBoolean(true);
					PacketHandler.writeString(buffer, frequency.name);
					buffer.writeBoolean(frequency.publicFreq);
				}
				else {
					buffer.writeBoolean(false);
				}
				
				buffer.writeByte(status);
				
				ArrayList data = new ArrayList();
				data.add(publicCache.size());
				
				for(Frequency freq : publicCache)
				{
					freq.write(data);
				}
				
				data.add(privateCache.size());
				
				for(Frequency freq : privateCache)
				{
					freq.write(data);
				}
				
				PacketHandler.encode(data.toArray(), buffer);
			}
			else if(packetType == PortableTeleporterPacketType.SET_FREQ)
			{
				PacketHandler.writeString(buffer, frequency.name);
				buffer.writeBoolean(frequency.publicFreq);
			}
			else if(packetType == PortableTeleporterPacketType.DEL_FREQ)
			{
				PacketHandler.writeString(buffer, frequency.name);
				buffer.writeBoolean(frequency.publicFreq);
			}
			else if(packetType == PortableTeleporterPacketType.TELEPORT)
			{
				PacketHandler.writeString(buffer, frequency.name);
				buffer.writeBoolean(frequency.publicFreq);
			}
		}
	
		@Override
		public void fromBytes(ByteBuf buffer)
		{
			packetType = PortableTeleporterPacketType.values()[buffer.readInt()];
			
			if(packetType == PortableTeleporterPacketType.DATA_REQUEST)
			{
				if(buffer.readBoolean())
				{
					frequency = new Frequency(PacketHandler.readString(buffer), null).setPublic(buffer.readBoolean());
				}
			}
			else if(packetType == PortableTeleporterPacketType.DATA_RESPONSE)
			{
				if(buffer.readBoolean())
				{
					frequency = new Frequency(PacketHandler.readString(buffer), null).setPublic(buffer.readBoolean());
				}
				
				status = buffer.readByte();
				
				int amount = buffer.readInt();
				
				for(int i = 0; i < amount; i++)
				{
					publicCache.add(new Frequency(buffer));
				}
				
				amount = buffer.readInt();
				
				for(int i = 0; i < amount; i++)
				{
					privateCache.add(new Frequency(buffer));
				}
			}
			else if(packetType == PortableTeleporterPacketType.SET_FREQ)
			{
				frequency = new Frequency(PacketHandler.readString(buffer), null).setPublic(buffer.readBoolean());
			}
			else if(packetType == PortableTeleporterPacketType.DEL_FREQ)
			{
				frequency = new Frequency(PacketHandler.readString(buffer), null).setPublic(buffer.readBoolean());
			}
			else if(packetType == PortableTeleporterPacketType.TELEPORT)
			{
				frequency = new Frequency(PacketHandler.readString(buffer), null).setPublic(buffer.readBoolean());
			}
		}
	}
	
	public static enum PortableTeleporterPacketType
	{
		DATA_REQUEST,
		DATA_RESPONSE,
		SET_FREQ,
		DEL_FREQ,
		TELEPORT;
	}
}
