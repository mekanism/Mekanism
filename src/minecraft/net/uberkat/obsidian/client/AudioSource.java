package net.uberkat.obsidian.client;

import java.lang.reflect.Field;
import java.net.URL;

import paulscode.sound.SoundSystem;
import net.minecraft.src.*;
import net.uberkat.obsidian.common.Sound;

public class AudioSource 
{
	public boolean isPlaying;
	public int xPos;
	public int yPos;
	public int zPos;
	public int soundTicks;
	public SoundSystem system;
	public String sourceName;
	public boolean valid;
	
	public AudioSource(SoundSystem sys, Sound sound, String id)
	{
		valid = false;
		sourceName = sound.name;
		system = sys;
		xPos = sound.x;
		yPos = sound.y;
		zPos = sound.z;
		isPlaying = false;
		URL url = (AudioSource.class).getClassLoader().getResource((new StringBuilder()).append("sounds/").append(sound.name).toString());
		if(url != null)
		{
            sys.newSource(false, sound.name, url, id, false, sound.x, sound.y, sound.z, 0, AudioManager.fadingDistance * Math.max(AudioManager.defaultVolume, 1.0F));
            valid = true;
		}
	}
	
	public void activate()
	{
		if(valid)
		{
			system.activate(sourceName);
		}
	}
	
	public void remove()
	{
		if(valid)
		{
			stopSound();
			system.removeSource(sourceName);
			sourceName = null;
		}
	}
	
	public void playSound()
	{
		if(!isPlaying)
		{
			system.play(sourceName);
			isPlaying = true;
		}
	}
	
	public void pauseSound()
	{
		if(isPlaying)
		{
			system.pause(sourceName);
			isPlaying = false;
		}
	}
	
	public void stopSound()
	{
		if(isPlaying)
		{
			system.stop(sourceName);
			isPlaying = false;
		}
	}
	
	public void flushSound()
	{
		if(isPlaying)
		{
			system.flush(sourceName);
		}
	}
}
