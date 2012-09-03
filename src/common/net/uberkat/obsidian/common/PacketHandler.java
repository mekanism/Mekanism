package net.uberkat.obsidian.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.server.FMLServerHandler;

/**
 * Obsidian Ingots packet handler.  As always, use packets sparingly!
 * @author AidanBrady
 *
 */
public class PacketHandler implements IPacketHandler
{
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		ByteArrayDataInput dataStream = ByteStreams.newDataInput(packet.data);
        EntityPlayer entityplayer = (EntityPlayer)player;
        
		if(packet.channel.equals("ObsidianIngots"))
		{
			try {
				int packetData = dataStream.readInt();
				
			    if(packetData == 0)
			    {
			        System.out.println("[ObsidianIngots] Received '0' packet from " + entityplayer.username + ".");
			        ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 0);
			    }
			        
			    if(packetData == 1)
			    {
			        System.out.println("[ObsidianIngots] Received '1' packet from " + entityplayer.username + ".");
			        ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 6);
			    }
			        
			    if(packetData == 2)
			    {
			    	System.out.println("[ObsidianIngots] Received '2' packet from " + entityplayer.username + ".");
			    	ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 12);
			    }
			    	
			    if(packetData == 3)
			    {
			    	System.out.println("[ObsidianIngots] Received '3' packet from " + entityplayer.username + ".");
			    	ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 18);
			    }
			    if(packetData == 4)
			    {
			    	try {
						int x = dataStream.readInt();
						int y = dataStream.readInt();
						int z = dataStream.readInt();
						
						World world = ((EntityPlayer)player).worldObj;
						TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
						if(tileEntity instanceof TileEntityMachine)
						{
							((TileEntityMachine)tileEntity).handlePacketData(manager, packet, ((EntityPlayer)player), dataStream);
						}
					} catch (Exception e)
					{
						System.err.println("[ObsidianIngots] Error while handling tile entity packet.");
						e.printStackTrace();
					}
			    }
			    if(packetData == 5)
			    {
			    	System.out.println("[ObsidianIngots] Recieved '5' packet from " + entityplayer.username + ".");
			        entityplayer.worldObj.getWorldInfo().setRaining(false);
			        entityplayer.worldObj.getWorldInfo().setThundering(false);
			    }
			    if(packetData == 6)
			    {
			    	System.out.println("[ObsidianIngots] Recieved '6' packet from " + entityplayer.username + ".");
			        entityplayer.worldObj.getWorldInfo().setRaining(true);
			        entityplayer.worldObj.getWorldInfo().setThundering(true);
			    }
			    if(packetData == 7)
			    {
			    	System.out.println("[ObsidianIngots] Recieved '7' packet from " + entityplayer.username + ".");
			    	entityplayer.worldObj.getWorldInfo().setThundering(true);
			    }
			    if(packetData == 8)
			    {
			    	System.out.println("[ObsidianIngots] Recieved '8' packet from " + entityplayer.username + ".");
			    	entityplayer.worldObj.getWorldInfo().setRaining(true);
			    }
			}
			catch (Exception e)
			{
				System.err.println("[ObsidianIngots] Error while handling data int packet.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Send a machine update packet from server to client. This will send the data int '4,' as well as the machine's
	 * x, y, and z coordinates, along with it's active state, burn time, cook time, and item burn time.
	 * @param sender - tile entity who is sending the packet
	 */
	public static void sendMachinePacket(TileEntityMachine sender)
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytes);
        
        try {
        	output.writeInt(4);
        	output.writeInt(sender.xCoord);
        	output.writeInt(sender.yCoord);
        	output.writeInt(sender.zCoord);
        	output.writeByte(sender.isActive ? 1 : 0);
        	output.writeInt(sender.machineBurnTime);
        	output.writeInt(sender.machineCookTime);
        	output.writeInt(sender.currentItemBurnTime);
        } catch (IOException e)
        {
        	System.err.println("[ObsidianIngots] Error while writing tile entity packet.");
        	e.printStackTrace();
        }
        
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "ObsidianIngots";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        
        if(FMLCommonHandler.instance().getMinecraftServerInstance() != null)
        {
        	FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
        }
	}
	
	/**
	 * Sends the server the defined packet data int.
	 * @param i - int to send
	 */
	public static void sendPacketDataInt(int i)
	{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        try {
			data.writeInt(i);
		} catch (IOException e) {
			System.out.println("[ObsidianIngots] An error occured while writing packet data.");
			e.printStackTrace();
		}
        Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "ObsidianIngots";
        packet.data = bytes.toByteArray();
        packet.length = packet.data.length;
        PacketDispatcher.sendPacketToServer(packet);
        System.out.println("[ObsidianIngots] Sent '" + i + "' packet to server");
	}
}
