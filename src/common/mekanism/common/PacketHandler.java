package mekanism.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import mekanism.api.EnumGas;
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
			    if(packetType == EnumPacketType.CONTROL_PANEL.id)
			    {
			    	try {
			    		String modClass = dataStream.readUTF();
			    		String modInstance = dataStream.readUTF();
			    		int x = dataStream.readInt();
			    		int y = dataStream.readInt();
			    		int z = dataStream.readInt();
			    		int guiId = dataStream.readInt();
			    		
			    		Class mod = Class.forName(modClass);
			    		
			    		if(mod == null)
			    		{
			    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			    			System.err.println(" ~ Unable to locate class '" + modClass + ".'");
			    			System.err.println(" ~ GUI Container may not function correctly.");
			    			return;
			    		}
			    		
			    		Object instance = mod.getField(modInstance).get(null);
			    		
			    		if(instance == null)
			    		{
			    			System.err.println("[Mekanism] Incorrectly implemented IAccessibleGui -- ignoring handler packet.");
			    			System.err.println(" ~ Unable to locate instance object '" + modInstance + ".'");
			    			System.err.println(" ~ GUI Container may not function correctly.");
			    			return;
			    		}
			    		
			    		entityplayer.openGui(instance, guiId, entityplayer.worldObj, x, y, z);
			    		
			    	} catch (Exception e)
			    	{
			    		System.err.println("[Mekanism] Error while handling control panel packet.");
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
	 * Sends a packet from client to server with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans. This can
	 * also be sent with a defined range, so players far away won't receive the packet.
	 * @param sender - sending tile entity
	 * @param distance - distance to send the packet, 0 if infinite range
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacketToServer(TileEntity sender, Object... dataValues)
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
        		else if(data instanceof String)
        		{
        			output.writeUTF((String)data);
        		}
        	}
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            PacketDispatcher.sendPacketToServer(packet);
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * Sends a packet from server to client with the TILE_ENTITY ID as well as an undefined amount of objects.
	 * While it won't give you an error, you cannot send anything other than integers or booleans. This can
	 * also be sent with a defined range, so players far away won't receive the packet.
	 * @param sender - sending tile entity
	 * @param distance - distance to send the packet, 0 if infinite range
	 * @param dataValues - data to send
	 */
	public static void sendTileEntityPacketToClients(TileEntity sender, double distance, Object... dataValues)
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
        		else if(data instanceof String)
        		{
        			output.writeUTF((String)data);
        		}
        	}
        	
            Packet250CustomPayload packet = new Packet250CustomPayload();
            packet.channel = "Mekanism";
            packet.data = bytes.toByteArray();
            packet.length = packet.data.length;
            
            if(distance == 0) PacketDispatcher.sendPacketToAllPlayers(packet);
            else PacketDispatcher.sendPacketToAllAround(sender.xCoord, sender.yCoord, sender.zCoord, distance, sender.worldObj.provider.dimensionId, packet);
        } catch (IOException e) {
        	System.err.println("[Mekanism] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
	}
	
	/**
	 * When the 'Access' button is clicked on the Control Panel's GUI, it both opens the client-side GUI, as well as
	 * send a packet to the server with enough data to open up the server-side object, or container. This packet does
	 * that function -- it sends over the data from IAccessibleGui (modClass, modInstance, guiId), and uses reflection
	 * to attempt and access the declared instance object from the mod's main class, which is also accessed using
	 * reflection. Upon being handled server-side, the data is put together, checked for NPEs, and then used inside
	 * EntityPlayer.openGui() along with the sent-over coords.
	 * @param modClass
	 * @param modInstance
	 * @param x
	 * @param y
	 * @param z
	 * @param guiId
	 */
	public static void sendGuiRequest(String modClass, String modInstance, int x, int y, int z, int guiId)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        
        try {
        	data.writeInt(EnumPacketType.CONTROL_PANEL.id);
        	data.writeUTF(modClass);
        	data.writeUTF(modInstance);
        	data.writeInt(x);
        	data.writeInt(y);
        	data.writeInt(z);
        	data.writeInt(guiId);
        } catch (IOException e) {
        	System.out.println("[Mekanism] An error occured while writing packet data.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "Mekanism";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        System.out.println("[Mekanism] Sent control panel packet to server.");
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
