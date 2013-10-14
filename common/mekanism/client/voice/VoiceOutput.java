package mekanism.client.voice;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class VoiceOutput extends Thread
{
	public VoiceClient voiceClient;
	
	public DataLine.Info speaker = new DataLine.Info(SourceDataLine.class, voiceClient.format, 2200);
	
	public SourceDataLine sourceLine;
	
	public VoiceOutput(VoiceClient client)
	{
		voiceClient = client;
		
		setDaemon(true);
		setName("VoiceServer Client Output Thread");
	}
	
	@Override
	public void run()
	{
		try {
			sourceLine = ((SourceDataLine)AudioSystem.getLine(speaker));
			sourceLine.open(voiceClient.format, 2200);
			sourceLine.start();
			
			while(voiceClient.running)
			{
				short byteCount = voiceClient.input.readShort();
				byte[] audioData = new byte[byteCount];
				voiceClient.input.readFully(audioData);
				
				sourceLine.write(audioData, 0, audioData.length);
			}
		} catch(Exception e) {
			System.err.println("Error while running speaker loop.");
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		sourceLine.flush();
		sourceLine.close();
	}
}
