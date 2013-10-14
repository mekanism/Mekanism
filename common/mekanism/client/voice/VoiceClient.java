package mekanism.client.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
		System.out.println("[Mekanism] VoiceServer: Starting client connection...");
		
		try {
			socket = new Socket(ip, port);
			running = true;
			
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			(outputThread = new VoiceOutput(this)).start();
			(inputThread = new VoiceInput(this)).start();
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while starting client connection.");
			e.printStackTrace();
		}
	}
	
	public void stop()
	{
		System.out.println("[Mekanism] VoiceServer: Stopping client connection...");
		
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
			System.err.println("[Mekanism] VoiceServer: Error while stopping client connection.");
			e.printStackTrace();
		}
	}
}
