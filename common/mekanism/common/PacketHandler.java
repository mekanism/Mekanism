package mekanism.common;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import mekanism.api.Object3D;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler implements IPacketHandler
{
	/** The ArrayList of registered packet classes, who's index tells which packet is which. */
	public static List<Class<? extends IMekanismPacket>> packets = new ArrayList<Class<? extends IMekanismPacket>>();
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		ByteArrayDataInput dataStream = ByteStreams.newDataInput(packet.data);
        EntityPlayer entityplayer = (EntityPlayer)player;
        
		if(packet.channel.equals("MEK"))
		{
			try {
				int packetIndex = dataStream.readInt();
				
				if(packets.get(packetIndex) == null)
				{
					System.err.println("[Mekanism] Received unknown packet identifier '" + packetIndex + ".' Ignorning!");
					return;
				}
				
				IMekanismPacket packetType = packets.get(packetIndex).newInstance();
				
				if(packetType == null)
				{
					System.err.println("[Mekanism] Unable to create instance of packet type '" + packetIndex + ".' Ignoring!");
					return;
				}
				
				try {
					packetType.read(dataStream, entityplayer, entityplayer.worldObj);
				} catch(Exception e) {
					System.err.println("[Mekanism] Error while reading '" + packetType.getName() + "' packet.");
				}
			} catch(Exception e) {
				System.err.println("[Mekanism] Error while handling packet.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Registers a packet class for identification and reflection purposes.  This MUST be called both server-side and client-side,
	 * otherwise the packet will not be handled correctly.
	 * @param packetClass - class of the packet to register
	 */
	public static void registerPacket(Class<? extends IMekanismPacket> packetClass)
	{
		for(Class<? extends IMekanismPacket> iteration : packets)
		{
			if(iteration == packetClass)
			{
				return;
			}
		}
		
		packets.add(packetClass);
	}
	
	/**
	 * Encodes an Object[] of data into a DataOutputStream.
	 * @param dataValues - an Object[] of data to encode
	 * @param output - the output stream to write to
	 */
	public static void encode(Object[] dataValues, DataOutputStream output)
	{
		try {
	    	for(Object data : dataValues)
	    	{
	    		if(data instanceof Integer)
	    		{
	    			output.writeInt((Integer)data);
	    		}
	    		else if(data instanceof Boolean)
	    		{
	    			output.writeBoolean((Boolean)data);
	    		}
	    		else if(data instanceof Double)
	    		{
	    			output.writeDouble((Double)data);
	    		}
	    		else if(data instanceof Float)
	    		{
	    			output.writeFloat((Float)data);
	    		}
	    		else if(data instanceof String)
	    		{
	    			output.writeUTF((String)data);
	    		}
	    		else if(data instanceof Byte)
	    		{
	    			output.writeByte((Byte)data);
	    		}
	    		else if(data instanceof int[])
	    		{
	    			for(int i : (int[])data)
	    			{
	    				output.writeInt(i);
	    			}
	    		}
	    		else if(data instanceof byte[])
	    		{
	    			for(byte b : (byte[])data)
	    			{
	    				output.writeByte(b);
	    			}
	    		}
	    		else if(data instanceof ArrayList)
	    		{
	    			encode(((ArrayList)data).toArray(), output);
	    		}
	    	}
		} catch(Exception e) {
    		System.err.println("[Mekanism] Error while encoding packet data.");
    		e.printStackTrace();
		}
	}
	
	/**
	 * Sends a packet with the defined type of transmission.
	 * @param trans - the type of transmission to use with this packet
	 * @param packetType - the object representing this packet, both registered and properly using write()
	 * @param transParams - any extra parameters the transmission type requires
	 */
	public static void sendPacket(Transmission trans, IMekanismPacket packetType, Object... transParams)
	{
		if(packetType == null)
		{
			System.err.println("[Mekanism] Attempted to send null packet, ignoring!");
			return;
		}
		
		if(!packets.contains(packetType.getClass()))
		{
			System.err.println("[Mekanism] Attempted to send unregistered packet '" + packetType.getName() + ",' ignoring!");
			return;
		}
		
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(packets.indexOf(packetType.getClass()));
        	packetType.write(data);
        } catch(Exception e) {
        	System.err.println("[Mekanism] Error while encoding packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "MEK";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        
        switch(trans)
        {
        	case SERVER:
        		PacketDispatcher.sendPacketToServer(packet);
        		break;
        	case ALL_CLIENTS:
        		PacketDispatcher.sendPacketToAllPlayers(packet);
        		break;
        	case CLIENTS_RANGE:
        		Object3D obj = (Object3D)transParams[0];
        		PacketDispatcher.sendPacketToAllAround(obj.xCoord, obj.yCoord, obj.zCoord, (Double)transParams[1], obj.dimensionId, packet);
        		break;
        	case SINGLE_CLIENT:
        		((EntityPlayerMP)transParams[0]).playerNetServerHandler.sendPacketToPlayer(packet);
        		break;
        }
        
        log(trans, packetType, transParams);
	}
	
	/**
	 * Writes a log to the console with information about a packet recently sent.
	 * @param trans - transmission type this packet used when it was sent
	 * @param packetType - object representing this packet
	 * @param transParams - any extra parameters the transmission type requires
	 */
	private static void log(Transmission trans, IMekanismPacket packetType, Object[] transParams)
	{
		if(Mekanism.logPackets)
		{
			switch(trans)
			{
				case SERVER:
					System.out.println("[Mekanism] Sent '" + packetType.getName() + "' packet to server.");
					break;
				case ALL_CLIENTS:
					System.out.println("[Mekanism] Sent '" + packetType.getName() + "' packet to all clients.");
					break;
				case CLIENTS_RANGE:
					System.out.println("[Mekanism] Sent '" + packetType.getName() + "' packet to clients in a " + (Double)transParams[1] + " block range.");
					break;
				case SINGLE_CLIENT:
					System.out.println("[Mekanism] Sent '" + packetType.getName() + "' packet to " + ((EntityPlayer)transParams[0]).username);
					break;
			}
		}
	}
	
	public static enum Transmission
	{
		/** No additional parameters. */
		SERVER(0),
		
		/** No additional parameters. */
		ALL_CLIENTS(0),
		
		/** 2 parameters - Object3D representing the location of the transmission, and a double of the distance this packet can be sent in. */
		CLIENTS_RANGE(2),
		
		/** 1 parameter - EntityPlayer to send this packet to. */
		SINGLE_CLIENT(1);
		
		public int parameters;
		
		private Transmission(int params)
		{
			parameters = params;
		}
	}
}
