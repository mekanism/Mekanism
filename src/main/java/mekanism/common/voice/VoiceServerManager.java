package mekanism.common.voice;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.MekanismConfig.general;
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
		Mekanism.logger.info("VoiceServer: Starting up server...");

		try {
			running = true;
			serverSocket = new ServerSocket(general.VOICE_PORT);
			(listenThread = new ListenThread()).start();
		} catch(Exception e) {}
	}

	public void stop()
	{
		try {
			Mekanism.logger.info("VoiceServer: Shutting down server...");

			try {
				listenThread.interrupt();
			} catch(Exception e) {}

			foundLocal = false;

			try {
				serverSocket.close();
				serverSocket = null;
			} catch(Exception e) {}
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while shutting down server.");
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

					Mekanism.logger.info("VoiceServer: Accepted new connection.");
				} catch(SocketException e) {
				} catch(NullPointerException e) {
				} catch(Exception e) {
					Mekanism.logger.error("VoiceServer: Error while accepting connection.");
					e.printStackTrace();
				}
			}
		}
	}
}
