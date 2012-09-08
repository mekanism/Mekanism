package net.uberkat.obsidian.client;

import java.net.URL;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class SoundHandler 
{
	public static final SoundHandler instance = new SoundHandler();
	
	public final URL[] sounds = { getURL("chamber.ogg"), getURL("combiner.ogg"), getURL("compressor.ogg"), getURL("crusher.ogg"), getURL("elementizer.ogg") };
	
	@ForgeSubscribe
	public void loadSoundEvents(SoundLoadEvent event)
	{
		for(int i = 0; i < sounds.length; i++)
		{
			URL url = getClass().getResource("/obsidian/" + sounds[i]);
			event.manager.soundPoolSounds.addSound("obsidian/" + sounds[i], url);
			
			if(url != null)
				continue;
			System.err.println("[ObsidianIngots] Error loading sound file '" + sounds[i] + ".'");
		}
	}
	
	public URL getURL(String url)
	{
		return getClass().getResource("/obsidian/" + url);
	}
}
