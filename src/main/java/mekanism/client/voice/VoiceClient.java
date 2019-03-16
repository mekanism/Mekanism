package mekanism.client.voice;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import javax.sound.sampled.AudioFormat;

import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoiceClient extends Thread
{
	public Socket socket;

	public String ip;

	public AudioFormat format = new AudioFormat(16000F, 16, 1, true, true);

	public VoiceInput inputThread;
	public VoiceOutput outputThread;

	public DataInputStream input;
	public DataOutputStream output;

	public boolean running;

	public VoiceClient(String s)
	{
		ip = s;
	}

	@Override
	public void run()
	{
		Mekanism.logger.info("VoiceServer: Starting client connection...");

		try {
			socket = new Socket(ip, MekanismConfig.current().general.VOICE_PORT.val());
			running = true;

			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

			(outputThread = new VoiceOutput(this)).start();
			(inputThread = new VoiceInput(this)).start();

			Mekanism.logger.info("VoiceServer: Successfully connected to server.");
		} catch(ConnectException e) {
			Mekanism.logger.error("VoiceServer: Server's VoiceServer is disabled.");
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while starting client connection.");
			e.printStackTrace();
		}
	}

	public void disconnect()
	{
		Mekanism.logger.info("VoiceServer: Stopping client connection...");

		try {
			inputThread.interrupt();
			outputThread.interrupt();
			inputThread.close();
			outputThread.close();
			output.close();
			input.close();
			socket.close();
			
			interrupt();
			
			running = false;
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while stopping client connection.");
			e.printStackTrace();
		}
	}
}
