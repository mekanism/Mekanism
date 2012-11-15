package mekanism.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import mekanism.api.ITileNetwork;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.server.FMLServerHandler;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler implements IPacketHandler
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		ByteArrayDataInput dataStream = ByteStreams.newDataInput(packet.data);
        EntityPlayer entityplayer = (EntityPlayer)player;
        
		if(packet.channel.equals("Mekanism"))
		{
			try {
				int packetType = dataStream.readInt();
				
			    if(packetType == EnumPacketType.TIME.id)
			    {
			        System.out.println("[Mekanism] Received time update packet from " + entityplayer.username + ".");
			        MekanismUtils.setHourForward(FMLServerHandler.instance().getServer().worldServerForDimension(0), dataStream.readInt());
			    }
			    if(packetType == EnumPacketType.WEATHER.id)
			    {
			    	System.out.println("[Mekanism] Received weather update packet from " + entityplayer.username + ".");
			    	int weatherType = dataStream.readInt();
			    	if(weatherType == EnumWeatherType.CLEAR.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(false);
				        entityplayer.worldObj.getWorldInfo().setThundering(false);
			    	}
			    	if(weatherType == EnumWeatherType.HAZE.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(true);
				        entityplayer.worldObj.getWorldInfo().setThundering(true);
			    	}
			    	if(weatherType == EnumWeatherType.RAIN.id)
			    	{
			    		entityplayer.worldObj.getWorldInfo().setRaining(true);
			    	}
			    	if(weatherType == EnumWeatherType.STORM.id)
			    	{
				    	entityplayer.worldObj.getWorldInfo().setThundering(true);
			    	}
			    }
			    if(packetType == EnumPacketType.TILE_ENTITY.id)
			    {
			    	try {
						int x = dataStream.readInt();
						int y = dataStream.readInt();
						int z = dataStream.readInt();
						
						World world = ((EntityPlayer)player).worldObj;
						TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
						if(tileEntity instanceof ITileNetwork)
						{
							((ITileNetwork)tileEntity).handlePacketData(manager, packet, ((EntityPlayer)player), dataStream);
						}
					} catch (Exception e)
					{
						System.err.println("[Mekanism] Error while handling tile entity packet.");
						e.printStackTrace();
					}
			    }
			}
			catch (Exception e)
			{
				System.err.println("[Mekanism] Error while handling data int packet.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sends a packet from server to client with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans.
	 * @param sender - sending tile entity
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacket(TileEntity sender, Object... dataValues)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(EnumPacketType.TILE_ENTITY.id);
        	output.writeInt(sender.xCoord);
        	output.writeInt(sender.yCoord);
        	output.writeInt(sender.zCoord);
        	
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
        	}  
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            if(FMLCommonHandler.instance().getMinecraftServerInstance() != null)
            {
            	FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
            }
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * Sends a packet from server to client with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans. This will
	 * also be sent with a defined range, so players far away won't receive the packet.
	 * @param sender - sending tile entity
	 * @param distance - distance to send the packet
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacketWithRange(TileEntity sender, double distance, Object... dataValues)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(EnumPacketType.TILE_ENTITY.id);
        	output.writeInt(sender.xCoord);
        	output.writeInt(sender.yCoord);
        	output.writeInt(sender.zCoord);
        	
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
        	}
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            PacketDispatcher.sendPacketToAllAround(sender.xCoord, sender.yCoord, sender.zCoord, distance, sender.worldObj.provider.dimensionId, packet);
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * Sends the server the defined packet data int.
	 * @param type - packet type
	 * @param i - int to send
	 */
	public static void sendPacketDataInt(EnumPacketType type, int i)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
        	data.writeInt(type.id);
			data.writeInt(i);
		} catch (IOException e) {
			System.out.println("[Mekanism] An error occured while writing packet data.");
			e.printStackTrace();
		}
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        System.out.println("[Mekanism] Sent data int packet '" + i + "' to server");
	}
}
