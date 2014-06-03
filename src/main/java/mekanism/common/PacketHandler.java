package mekanism.common;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;


/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler //implements IPacketHandler
{
	/*
	/** The ArrayList of registered packet classes, who's index tells which packet is which. *
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
					Mekanism.logger.error("Received unknown packet identifier '" + packetIndex + ".' Ignorning!");
					return;
				}

				IMekanismPacket packetType = packets.get(packetIndex).newInstance();

				if(packetType == null)
				{
					Mekanism.logger.error("Unable to create instance of packet type '" + packetIndex + ".' Ignoring!");
					return;
				}

				try {
					packetType.read(dataStream, entityplayer, entityplayer.worldObj);
				} catch(Exception e) {
					Mekanism.logger.error("Error while reading '" + packetType.getName() + "' packet.");
					e.printStackTrace();
				}
			} catch(Exception e) {
				Mekanism.logger.error("Error while handling packet.");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Registers a packet class for identification and reflection purposes.  This MUST be called both server-side and client-side,
	 * otherwise the packet will not be handled correctly.
	 * @param packetClass - class of the packet to register
	 *
	public static void registerPacket(Class<? extends IMekanismPacket> packetClass)
	{
		if(!packets.contains(packetClass))
		{
			packets.add(packetClass);
		}
	}*/

	/**
	 * Encodes an Object[] of data into a DataOutputStream.
	 * @param dataValues - an Object[] of data to encode
	 * @param output - the output stream to write to
	 */
	public static void encode(Object[] dataValues, ByteBuf output)
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
					writeString(output, (String)data);
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
			Mekanism.logger.error("Error while encoding packet data.");
			e.printStackTrace();
		}
	}
	
	public static void writeString(ByteBuf output, String s)
	{
		output.writeInt(s.getBytes().length);
		output.writeBytes(s.getBytes());
	}
	
	public static String readString(ByteBuf input)
	{
		return new String(input.readBytes(input.readInt()).array());
	}
	
	/*/**
	 * Sends a packet with the defined type of transmission.
	 * @param trans - the type of transmission to use with this packet
	 * @param packetType - the object representing this packet, both registered and properly using write()
	 * @param transParams - any extra parameters the transmission type requires
	 *
	public static void sendPacket(Transmission trans, IMekanismPacket packetType, Object... transParams)
	{
		if(packetType == null)
		{
			Mekanism.logger.error("Attempted to send null packet, ignoring!");
			return;
		}

		if(!packets.contains(packetType.getClass()))
		{
			Mekanism.logger.error("Attempted to send unregistered packet '" + packetType.getName() + ",' ignoring!");
			return;
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try {
			data.writeInt(packets.indexOf(packetType.getClass()));
			packetType.write(data);
		} catch(Exception e) {
			Mekanism.logger.error("Error while encoding packet data.");
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
				Coord4D obj = (Coord4D)transParams[0];
				PacketDispatcher.sendPacketToAllAround(obj.xCoord, obj.yCoord, obj.zCoord, (Double)transParams[1], obj.dimensionId, packet);
				break;
			case CLIENTS_DIM:
				PacketDispatcher.sendPacketToAllInDimension(packet, (Integer)transParams[0]);
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
	 *
	private static void log(Transmission trans, IMekanismPacket packetType, Object[] transParams)
	{
		if(Mekanism.logPackets)
		{
			switch(trans)
			{
				case SERVER:
					Mekanism.logger.info("Sent '" + packetType.getName() + "' packet to server.");
					break;
				case ALL_CLIENTS:
					Mekanism.logger.info("Sent '" + packetType.getName() + "' packet to all clients.");
					break;
				case CLIENTS_RANGE:
					Mekanism.logger.info("Sent '" + packetType.getName() + "' packet to clients in a " + (Double)transParams[1] + " block range.");
					break;
				case CLIENTS_DIM:
					Mekanism.logger.info("Sent '" + packetType.getName() + "' packet to clients in dimension ID " + (Integer)transParams[0] + ".");
					break;
				case SINGLE_CLIENT:
					Mekanism.logger.info("Sent '" + packetType.getName() + "' packet to " + ((EntityPlayer)transParams[0]).username);
					break;
			}
		}
	}

	public static enum Transmission
	{
		/** No additional parameters. *
		SERVER(0),

		/** No additional parameters. *
		ALL_CLIENTS(0),

		/** 2 parameters - Object3D representing the location of the transmission, and a double of the distance this packet can be sent in. *
		CLIENTS_RANGE(2),

		/** 1 parameter - int representing the dimension ID to send this packet to. *
		CLIENTS_DIM(1),

		/** 1 parameter - EntityPlayer to send this packet to. *
		SINGLE_CLIENT(1),

		public int parameters;

		private Transmission(int params)
		{
			parameters = params;
		}
	}*/
}
