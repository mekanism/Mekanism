package mekanism.client.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoiceClient extends Thread
{
	public Socket socket;
	
	public String ip;
	public int port;
	
	public AudioFormat format = new AudioFormat(11025.0F, 8, 1, true, true);
	
	public VoiceInput inputThread;
	public VoiceOutput outputThread;
	
	public DataInputStream input;
	public DataOutputStream output;
	
	public boolean running;
	
	public VoiceClient(String s, int i)
	{
		ip = s;
		port = i;
	}
	
	@Override
	public void run()
	{
		System.out.println("[Mekanism] VoiceServer: Starting client connection...");
		
		try {
			socket = new Socket(ip, port);
			running = true;
			
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			
			(outputThread = new VoiceOutput(this)).start();
			(inputThread = new VoiceInput(this)).start();
			
			System.out.println("[Mekanism] VoiceServer: Successfully connected to server.");
		} catch(ConnectException e) {
			System.err.println("[Mekanism] VoiceServer: Server's VoiceServer is disabled.");
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while starting client connection.");
			e.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		System.out.println("[Mekanism] VoiceServer: Stopping client connection...");
		
		try {
			try {
				inputThread.interrupt();
				outputThread.interrupt();
			} catch(Exception e) {}
			
			try {
				interrupt();
			} catch(Exception e) {}
			
			try {
				inputThread.close();
				outputThread.close();
			} catch(Exception e) {}
			
			try {
				output.flush();
				output.close();
				output = null;
			} catch(Exception e) {}
			
			try {
				input.close();
				input = null;
			} catch(Exception e) {}
			
			try {
				socket.close();
				socket = null;
			} catch(Exception e) {}
			
			
			running = false;
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while stopping client connection.");
			e.printStackTrace();
		}
	}
}
