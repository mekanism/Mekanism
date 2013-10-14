package mekanism.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import net.minecraft.entity.player.EntityPlayerMP;
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
			open = false;
		}
			
		//Main client listen thread
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try {
					while(open)
					{
						
					}
				} catch(Exception e) {
					open = false;
				}
				
				if(!open)
				{
					try {
						input.close();
						output.close();
						socket.close();
					} catch(Exception e) {}
				}
			}
		}).start();
	}
}
