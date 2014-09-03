package mekanism.generators.common;

import java.util.ArrayList;
import java.util.List;

import mekanism.common.Mekanism;
import mekanism.generators.common.network.PacketGeneratorsGui;
import mekanism.generators.common.network.PacketGeneratorsGui.GeneratorsGuiMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class GeneratorsPacketHandler
{
	public SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("MEKGEN");

	public void initialize()
	{
		netHandler.registerMessage(PacketGeneratorsGui.class, GeneratorsGuiMessage.class, 0, Side.SERVER);
		netHandler.registerMessage(PacketGeneratorsGui.class, GeneratorsGuiMessage.class, 0, Side.CLIENT);
	}

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

	public static EntityPlayer getPlayer(MessageContext context)
	{
		return Mekanism.proxy.getPlayer(context);
	}

	/**
	 * Send this message to everyone.
	 * @param message - the message to send
	 */
	public void sendToAll(IMessage message)
	{
		netHandler.sendToAll(message);
	}

	/**
	 * Send this message to the specified player.
	 * @param message - the message to send
	 * @param player - the player to send it to
	 */
	public void sendTo(IMessage message, EntityPlayerMP player)
	{
		netHandler.sendTo(message, player);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 *
	 * @param message - the message to send
	 * @param point - the TargetPoint around which to send
	 */
	public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point)
	{
		netHandler.sendToAllAround(message, point);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * @param message - the message to send
	 * @param dimensionId - the dimension id to target
	 */
	public void sendToDimension(IMessage message, int dimensionId)
	{
		netHandler.sendToDimension(message, dimensionId);
	}

	/**
	 * Send this message to the server.
	 * @param message - the message to send
	 */
	public void sendToServer(IMessage message)
	{
		netHandler.sendToServer(message);
	}

	/**
	 * Send this message to all players within a defined AABB cuboid.
	 * @param message - the message to send
	 * @param cuboid - the AABB cuboid to send the packet in
	 * @param dimId - the dimension the cuboid is in
	 */
	public void sendToCuboid(IMessage message, AxisAlignedBB cuboid, int dimId)
	{
		MinecraftServer server = MinecraftServer.getServer();

		if(server != null && cuboid != null)
		{
			for(EntityPlayerMP player : (List<EntityPlayerMP>)server.getConfigurationManager().playerEntityList)
			{
				if(player.dimension == dimId && cuboid.isVecInside(Vec3.createVectorHelper(player.posX, player.posY, player.posZ)))
				{
					sendTo(message, player);
				}
			}
		}
	}
}
