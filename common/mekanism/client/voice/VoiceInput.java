package mekanism.client.voice;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import mekanism.client.MekanismKeyHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VoiceInput extends Thread
{
	public VoiceClient voiceClient;
	
	public DataLine.Info microphone;
	
	public TargetDataLine targetLine;
	
	public VoiceInput(VoiceClient client)
	{
		voiceClient = client;
		microphone = new DataLine.Info(TargetDataLine.class, voiceClient.format, 2200);
		
		setDaemon(true);
		setName("VoiceServer Client Input Thread");
	}
	
	@Override
	public void run()
	{
		try {
			targetLine = ((TargetDataLine)AudioSystem.getLine(microphone));
			targetLine.open(voiceClient.format, 2200);
			targetLine.start();
			AudioInputStream audioInput = new AudioInputStream(targetLine);
			
			boolean doFlush = false;
			
			while(voiceClient.running)
			{
				if(MekanismKeyHandler.voice.isPressed())
				{
					targetLine.flush();
					
					while(voiceClient.running && MekanismKeyHandler.voice.isPressed())
					{
						int availableBytes = audioInput.available();
						byte[] audioData = new byte[availableBytes > 2200 ? 2200 : availableBytes];
						int bytesRead = audioInput.read(audioData, 0, audioData.length);
						
						if(bytesRead > 0)
						{
							voiceClient.output.writeShort(audioData.length);
							voiceClient.output.write(audioData);
						}
					}
					
					try {
						Thread.sleep(200L);
					} catch(Exception e) {}
					
					doFlush = true;
				}
				else if(doFlush)
				{
					voiceClient.output.flush();
					doFlush = false;
				}
				
				try {
					Thread.sleep(20L);
				} catch(Exception e) {}
			}
			
			audioInput.close();
		} catch(Exception e) {
			System.err.println("[Mekanism] VoiceServer: Error while running client input thread.");
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		targetLine.flush();
		targetLine.close();
	}
}
