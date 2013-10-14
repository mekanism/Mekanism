package mekanism.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
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
			start();
		} catch(Exception e) {}
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager) 
	{
		//connecting to LAN server on same instance
		try {
			socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 36123);
			running = true;
			start();
		} catch(Exception e) {}
	}

	@Override
	public void connectionClosed(INetworkManager manager) 
	{
		stop();
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
	{

	}
	
	public void start()
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
						
						while(running)
						{
							System.out.println("Looped");
							short byteCount = VoiceClientManager.this.input.readShort();
							byte[] audioData = new byte[byteCount];
							VoiceClientManager.this.input.readFully(audioData);
							
							VoiceClientManager.this.sourceLine.write(audioData, 0, audioData.length);
						}
					} catch(Exception e) {
						System.err.println("Error while running speaker loop.");
					}
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
						AudioInputStream audioInput = new AudioInputStream(VoiceClientManager.this.targetLine);
						
						boolean doFlush = false;
						
						while(running)
						{
							if(MekanismKeyHandler.voiceDown)
							{
								targetLine.flush();
								
								while(running && MekanismKeyHandler.voiceDown)
								{
									int availableBytes = audioInput.available();
									byte[] audioData = new byte[availableBytes > 2200 ? 2200 : availableBytes];
									int bytesRead = audioInput.read(audioData, 0, audioData.length);
									
									if(bytesRead > 0)
									{
										System.out.println("Writing");
										output.writeShort(audioData.length);
										output.write(audioData);
									}
								}
								
								try {
									Thread.sleep(200L);
								} catch(Exception e) {
									e.printStackTrace();
								}
								
								doFlush = true;
							}
							else if(doFlush)
							{
								VoiceClientManager.this.output.flush();
								doFlush = false;
							}
							
							try {
								Thread.sleep(20L);
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
						
						audioInput.close();
					} catch(Exception e) {
						System.err.println("Error while running microphone loop.");
						e.printStackTrace();
					}
				}
			}).start();
		} catch(Exception e) {}
	}
	
	public void stop()
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
		} catch(Exception e) {
			System.err.println("Error while ending client connection.");
			e.printStackTrace();
		}
	}
}
