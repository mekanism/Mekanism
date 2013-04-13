package universalelectricity.prefab.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import universalelectricity.core.vector.Vector3;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * This class is used for sending and receiving packets between the server and the client. You can
 * directly use this by registering this packet manager with NetworkMod. Example:
 * 
 * @NetworkMod(channels = { "BasicComponents" }, clientSideRequired = true, serverSideRequired =
 * false, packetHandler = PacketManager.class)
 * 
 * Check out {@link #BasicComponents} for better reference.
 * 
 * @author Calclavia
 */
public class PacketManager implements IPacketHandler, IPacketReceiver
{
	public enum PacketType
	{
		UNSPECIFIED, TILEENTITY;

		public static PacketType get(int id)
		{
			if (id >= 0 && id < PacketType.values().length)
			{
				return PacketType.values()[id];
			}
			return UNSPECIFIED;
		}
	}

	/**
	 * Writes a compressed NBTTagCompound to the OutputStream
	 */
	public static void writeNBTTagCompound(NBTTagCompound tag, DataOutputStream dataStream) throws IOException
	{
		if (tag == null)
		{
			dataStream.writeShort(-1);
		}
		else
		{
			byte[] var2 = CompressedStreamTools.compress(tag);
			dataStream.writeShort((short) var2.length);
			dataStream.write(var2);
		}
	}

	/**
	 * Reads a compressed NBTTagCompount in a ByteStream.
	 */
	public static NBTTagCompound readNBTTagCompound(ByteArrayDataInput dataStream) throws IOException
	{
		short var1 = dataStream.readShort();

		if (var1 < 0)
		{
			return null;
		}
		else
		{
			byte[] var2 = new byte[var1];
			dataStream.readFully(var2);
			return CompressedStreamTools.decompress(var2);
		}
	}

	@SuppressWarnings("resource")
	public static Packet getPacketWithID(String channelName, int id, Object... sendData)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try
		{
			data.writeInt(id);
			data = encodeDataStream(data, sendData);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = channelName;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			return packet;
		}
		catch (IOException e)
		{
			System.out.println("Failed to create packet.");
			e.printStackTrace();
		}

		return null;
	}

	public static Packet getPacket(String channelName, Object... sendData)
	{
		return getPacketWithID(channelName, PacketType.UNSPECIFIED.ordinal(), sendData);
	}

	/**
	 * Gets a packet for the tile entity.
	 * 
	 * @return
	 */
	@SuppressWarnings("resource")
	public static Packet getPacket(String channelName, TileEntity sender, Object... sendData)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);

		try
		{
			data.writeInt(PacketType.TILEENTITY.ordinal());

			data.writeInt(sender.xCoord);
			data.writeInt(sender.yCoord);
			data.writeInt(sender.zCoord);
			data = encodeDataStream(data, sendData);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = channelName;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			return packet;
		}
		catch (IOException e)
		{
			System.out.println("Failed to create packet.");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Sends packets to clients around a specific coordinate. A wrapper using Vector3. See
	 * {@PacketDispatcher} for detailed information.
	 */
	public static void sendPacketToClients(Packet packet, World worldObj, Vector3 position, double range)
	{
		try
		{
			PacketDispatcher.sendPacketToAllAround(position.x, position.y, position.z, range, worldObj.provider.dimensionId, packet);
		}
		catch (Exception e)
		{
			System.out.println("Sending packet to client failed.");
			e.printStackTrace();
		}
	}

	/**
	 * Sends a packet to all the clients on this server.
	 */
	public static void sendPacketToClients(Packet packet, World worldObj)
	{
		try
		{
			PacketDispatcher.sendPacketToAllInDimension(packet, worldObj.provider.dimensionId);
		}
		catch (Exception e)
		{
			System.out.println("Sending packet to client failed.");
			e.printStackTrace();
		}
	}

	public static void sendPacketToClients(Packet packet)
	{
		try
		{
			PacketDispatcher.sendPacketToAllPlayers(packet);
		}
		catch (Exception e)
		{
			System.out.println("Sending packet to client failed.");
			e.printStackTrace();
		}
	}

	public static DataOutputStream encodeDataStream(DataOutputStream data, Object... sendData)
	{
		try
		{
			for (Object dataValue : sendData)
			{
				if (dataValue instanceof Integer)
				{
					data.writeInt((Integer) dataValue);
				}
				else if (dataValue instanceof Float)
				{
					data.writeFloat((Float) dataValue);
				}
				else if (dataValue instanceof Double)
				{
					data.writeDouble((Double) dataValue);
				}
				else if (dataValue instanceof Byte)
				{
					data.writeByte((Byte) dataValue);
				}
				else if (dataValue instanceof Boolean)
				{
					data.writeBoolean((Boolean) dataValue);
				}
				else if (dataValue instanceof String)
				{
					data.writeUTF((String) dataValue);
				}
				else if (dataValue instanceof Short)
				{
					data.writeShort((Short) dataValue);
				}
				else if (dataValue instanceof Long)
				{
					data.writeLong((Long) dataValue);
				}
				else if (dataValue instanceof NBTTagCompound)
				{
					writeNBTTagCompound((NBTTagCompound) dataValue, data);
				}
			}

			return data;
		}
		catch (IOException e)
		{
			System.out.println("Packet data encoding failed.");
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		try
		{
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);

			int packetTypeID = data.readInt();

			PacketType packetType = PacketType.get(packetTypeID);

			if (packetType == PacketType.TILEENTITY)
			{
				int x = data.readInt();
				int y = data.readInt();
				int z = data.readInt();

				World world = ((EntityPlayer) player).worldObj;

				if (world != null)
				{
					TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

					if (tileEntity != null)
					{
						if (tileEntity instanceof IPacketReceiver)
						{
							((IPacketReceiver) tileEntity).handlePacketData(network, packetTypeID, packet, ((EntityPlayer) player), data);
						}
					}
				}
			}
			else
			{
				this.handlePacketData(network, packetTypeID, packet, ((EntityPlayer) player), data);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{

	}
}