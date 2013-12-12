package mekanism.common.voice;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import mekanism.common.Mekanism;

public class VoiceServerManager
{
	public Set<VoiceConnection> connections = new HashSet<VoiceConnection>();
	
	public ServerSocket serverSocket;
	
	public boolean running;
	
	public boolean foundLocal = false;
	
	public Thread listenThread;
	
	public void start()
	{
		System.out.println("[Mekanism] VoiceServer: Starting up server...");
		
		try {
			running = true;
			serverSocket = new ServerSocket(Mekanism.VOICE_PORT);
			(listenThread = new ListenThread()).start();
		} catch(Exception e) {}
	}
	
	public void stop()
	{
		try {
			listenThread.interrupt();
			
			foundLocal = false;
			
			serverSocket.close();
			serverSocket = null;
			
			System.out.println("[Mekanism] VoiceServer: Shutting down server...");
		} catch(SocketException e) {
			if(!e.getLocalizedMessage().toLowerCase().equals("socket closed"))
			{
				e.printStackTrace();
			}
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while shutting down server.");
			e.printStackTrace();
		}
		
		running = false;
	}
	
	public void sendToPlayers(short byteCount, byte[] audioData, VoiceConnection connection)
	{
		if(connection.getPlayer() == null)
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
			if(iterConn.getPlayer() == null || iterConn == connection || !iterConn.canListen(channel))
			{
				continue;
			}
			
			iterConn.sendToPlayer(byteCount, audioData, connection);
		}
	}
	
	public class ListenThread extends Thread
	{
		public ListenThread()
		{
			setDaemon(true);
			setName("VoiceServer Listen Thread");
		}
		
		@Override
		public void run()
		{
			while(running)
			{
				try {
					Socket s = serverSocket.accept();
					VoiceConnection connection = new VoiceConnection(s);
					connection.start();
					connections.add(connection);
					
					System.out.println("[Mekanism] VoiceServer: Accepted new connection.");
				} catch(SocketException e) {
				} catch(NullPointerException e) {
				} catch(Exception e) {
					System.err.println("[Mekanism] VoiceServer: Error while accepting connection.");
					e.printStackTrace();
				}
			}
		}
	}
}
