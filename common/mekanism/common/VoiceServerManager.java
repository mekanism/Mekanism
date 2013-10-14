package mekanism.common;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class VoiceServerManager implements IConnectionHandler
{
	public Set<VoiceConnection> connections = new HashSet<VoiceConnection>();
	
	public ServerSocket serverSocket;
	
	public boolean running;
	
	public Thread listenThread;
	
	public void start()
	{
		try {
			serverSocket = new ServerSocket(36123);
			
			listenThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					while(running)
					{
						try {
							Socket s = serverSocket.accept();
							VoiceConnection connection = new VoiceConnection(s);
							connections.add(connection);
						} catch(Exception e) {
							System.err.println("Error while accepting connection.");
							e.printStackTrace();
						}
					}
				}
			});
			
			listenThread.start();
		} catch(Exception e) {}
		
		running = true;
	}
	
	public void stop()
	{
		try {
			listenThread.interrupt();
			
			serverSocket.close();
			serverSocket = null;
		} catch(Exception e) {
			System.err.println("Error while stopping voice server.");
			e.printStackTrace();
		}
		
		running = false;
	}
	
	public void sendToPlayers(short byteCount, byte[] audioData, VoiceConnection connection)
	{
		if(connection.entityPlayer == null)
		{
			return;
		}
		
		int channel = connection.getCurrentChannel();
		
		if(channel == 0)
		{
			return;
		}
		
		for(VoiceConnection iterConn : connections)
		{
			if(iterConn.entityPlayer == null || iterConn == connection || !iterConn.canListen(channel))
			{
				continue;
			}
			
			iterConn.sendToPlayer(byteCount, audioData, connection);
		}
	}
	
	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager) 
	{
		
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager) 
	{
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) 
	{
		
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) 
	{
		
	}

	@Override
	public void connectionClosed(INetworkManager manager) 
	{
		
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login) 
	{
		
	}
}
