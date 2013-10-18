package mekanism.common.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import mekanism.common.Mekanism;
import mekanism.common.item.ItemWalkieTalkie;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.FMLCommonHandler;

public class VoiceConnection 
{
	public Socket socket;
	
	public EntityPlayerMP entityPlayer;
	
	public boolean open = true;
	
	public DataInputStream input;
	public DataOutputStream output;
	
	public VoiceConnection(Socket s)
	{
		socket = s;
		
		start();
	}
	
	public void start()
	{
		try {
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			for(Object obj : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().playerEntityList)
			{
				if(obj instanceof EntityPlayerMP)
				{
					EntityPlayerMP playerMP = (EntityPlayerMP)obj;
					String playerIP = playerMP.getPlayerIP();
					
					if(playerIP.equals("127.0.0.1"))
					{
						entityPlayer = playerMP;
						break;
					}
					else if(playerIP.equals(socket.getInetAddress().getHostAddress()))
					{
						entityPlayer = playerMP;
						break;
					}
				}
			}
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while starting server-based connection.");
			e.printStackTrace();
			open = false;
		}
			
		//Main client listen thread
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while(open)
				{
					try {
						short byteCount = VoiceConnection.this.input.readShort();
						byte[] audioData = new byte[byteCount];
						VoiceConnection.this.input.readFully(audioData);
						
						if(byteCount > 0)
						{
							Mekanism.voiceManager.sendToPlayers(byteCount, audioData, VoiceConnection.this);
						}
					} catch(Exception e) {
						open = false;
					}
				}
				
				if(!open)
				{
					kill();
				}
			}
		}).start();
	}
	
	public void kill()
	{
		try {
			input.close();
			output.close();
			socket.close();
			
			Mekanism.voiceManager.connections.remove(this);
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while stopping server-based connection.");
			e.printStackTrace();
		}
	}
	
	public void sendToPlayer(short byteCount, byte[] audioData, VoiceConnection connection)
	{
		if(!open)
		{
			kill();
		}
		
		try {
			output.writeShort(byteCount);
			output.write(audioData);
			
			output.flush();
			
			System.out.println("Sent");
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while sending data to player.");
			e.printStackTrace();
		}
	}
	
	public boolean canListen(int channel)
	{
		for(ItemStack itemStack : entityPlayer.inventory.mainInventory)
		{
			if(itemStack != null)
			{
				if(itemStack.getItem() instanceof ItemWalkieTalkie)
				{
					if(((ItemWalkieTalkie)itemStack.getItem()).getOn(itemStack))
					{
						if(((ItemWalkieTalkie)itemStack.getItem()).getChannel(itemStack) == channel)
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public int getCurrentChannel()
	{
		ItemStack itemStack = entityPlayer.getCurrentEquippedItem();
		
		if(itemStack != null)
		{
			ItemWalkieTalkie walkieTalkie = (ItemWalkieTalkie)itemStack.getItem();
			
			if(walkieTalkie != null)
			{
				if(walkieTalkie.getOn(itemStack))
				{
					return walkieTalkie.getChannel(itemStack);
				}
			}
		}
		
		return 0;
	}
}
