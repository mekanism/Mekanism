package mekanism.client.voice;

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

import mekanism.client.MekanismKeyHandler;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.NetLoginHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet1Login;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class VoiceClient
{
	public Socket socket;
	
	public AudioFormat format = new AudioFormat(11025.0F, 8, 1, true, true);
	
	public VoiceInput inputThread;
	public VoiceOutput outputThread;
	
	public DataInputStream input;
	public DataOutputStream output;
	
	public boolean running;
	
	public void start(String ip, int port)
	{
		System.out.println("Started client connection.");
		
		try {
			socket = new Socket(ip, port);
			running = true;
			
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			(outputThread = new VoiceOutput(this)).start();
			(inputThread = new VoiceInput(this)).start();
		} catch(Exception e) {
			System.err.println("Error in core client initiation.");
			e.printStackTrace();
		}
	}
	
	public void stop()
	{
		System.out.println("Stopped client connection.");
		
		try {
			inputThread.interrupt();
			outputThread.interrupt();
			
			inputThread.close();
			outputThread.close();
			
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
