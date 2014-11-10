package mekanism.client.voice;

import mekanism.common.Mekanism;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

@SideOnly(Side.CLIENT)
public class VoiceOutput extends Thread
{
	public VoiceClient voiceClient;

	public DataLine.Info speaker;

	public SourceDataLine sourceLine;

	public VoiceOutput(VoiceClient client)
	{
		voiceClient = client;
		speaker = new DataLine.Info(SourceDataLine.class, voiceClient.format, 2200);

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
				try {
					short byteCount = voiceClient.input.readShort();
					byte[] audioData = new byte[byteCount];
					voiceClient.input.readFully(audioData);

					sourceLine.write(audioData, 0, audioData.length);
				} catch(Exception e) {}
			}
		} catch(Exception e) {
			Mekanism.logger.error("VoiceServer: Error while running client output thread.");
			e.printStackTrace();
		}
	}

	public void close()
	{
		sourceLine.flush();
		sourceLine.close();
	}
}
