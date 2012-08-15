package net.uberkat.obsidian.common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler
{
	public void onPacketData(NetworkManager manager, Packet250CustomPayload packet, Player player)
	{
        DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        EntityPlayer entityplayer = (EntityPlayer)player;
        
		if(packet.channel.equals("ObsidianIngots"))
		{
			try {
				int packetData = dataStream.readInt();
				
			    if(packetData == 0)
			    {
			        System.out.println("[ObsidianIngots] Received '0' packet from " + entityplayer.username + ".");
			        ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 0);
					entityplayer.inventory.getCurrentItem().damageItem(4999, entityplayer);
			    }
			        
			    if(packetData == 1)
			    {
			        System.out.println("[ObsidianIngots] Received '1' packet from " + entityplayer.username + ".");
			        ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 1);
			        entityplayer.inventory.getCurrentItem().damageItem(4999, entityplayer);
			    }
			        
			    if(packetData == 2)
			    {
			    	System.out.println("[ObsidianIngots] Received '2' packet from " + entityplayer.username + ".");
			    	ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 2);
			    	entityplayer.inventory.getCurrentItem().damageItem(4999, entityplayer);
			    }
			    	
			    if(packetData == 3)
			    {
			    	System.out.println("[ObsidianIngots] Received '3' packet from " + entityplayer.username + ".");
			    	ObsidianUtils.setHourForward(ModLoader.getMinecraftServerInstance().worldServerForDimension(0), 3);
			    	entityplayer.inventory.getCurrentItem().damageItem(4999, entityplayer);
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
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
