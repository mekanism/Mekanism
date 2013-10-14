package mekanism.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class VoiceClientManager implements IConnectionHandler
{
	public Socket socket;
	
	public AudioFormat format = new AudioFormat(11025.0F, 8, 1, true, true);
	
	public DataLine.Info microphone = new DataLine.Info(TargetDataLine.class, this.format, 2200);
	public DataLine.Info speaker = new DataLine.Info(SourceDataLine.class, this.format, 2200);

	public TargetDataLine targetLine;
	public SourceDataLine sourceLine;
	
	public DataInputStream input;
	public DataOutputStream output;
	
	public boolean running;

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
	{
		try {
			socket = new Socket(manager.getSocketAddress().toString(), 36123);
		} catch(Exception e) {}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
	{
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager) 
	{
		//connecting to foreign server
		try {
			socket = new Socket(server, 36123);
			running = true;
		} catch(Exception e) {}
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) 
	{
		//connecting to LAN server on same instance
		try {
			socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 36123);
			running = true;
		} catch(Exception e) {}
	}

	@Override
	public void connectionClosed(INetworkManager manager) 
	{
		try {
			sourceLine.flush();
			sourceLine.close();
			
			targetLine.flush();
			targetLine.close();
			
			output.flush();
			output.close();
			output = null;
			
			input.close();
			input = null;
			
			socket.close();
			socket = null;
			running = false;
		} catch(Exception e) {}
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{

	}
	
	public void init()
	{
		try {
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			//Speaker (Out)
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try {
						VoiceClientManager.this.sourceLine = ((SourceDataLine)AudioSystem.getLine(VoiceClientManager.this.speaker));
						VoiceClientManager.this.sourceLine.open(VoiceClientManager.this.format, 2200);
						VoiceClientManager.this.sourceLine.start();
					} catch(Exception e) {}
				}
			}).start();
			
			//Microphone (In)
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try {
						VoiceClientManager.this.targetLine = ((TargetDataLine)AudioSystem.getLine(microphone));
						VoiceClientManager.this.targetLine.open(VoiceClientManager.this.format, 2200);
						VoiceClientManager.this.targetLine.start();
					} catch(Exception e) {}
				}
			}).start();
		} catch(Exception e) {}
	}
}
