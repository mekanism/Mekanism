package mekanism.client.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mekanism.client.MekanismClient;

public class SoundMap implements Iterable<Sound>
{
	public Map<String, Sound> soundMap = new HashMap<String, Sound>();
	
	public Object objRef;
	
	public SoundMap(Object obj)
	{
		objRef = obj;
	}
	
	public SoundMap(Object obj, String channel, Sound sound)
	{
		this(obj);
		
		soundMap.put(channel, sound);
	}

	@Override
	public Iterator<Sound> iterator() 
	{
		return soundMap.values().iterator();
	}
	
	public Sound getSound(String channel)
	{
		return soundMap.get(channel);
	}
	
	public boolean hasSound(String channel)
	{
		return soundMap.containsKey(channel);
	}
	
	public void remove(String channel)
	{
		soundMap.remove(channel);
	}
	
	public void add(String channel, Sound sound)
	{
		soundMap.put(channel, sound);
	}
	
	public void stopLoops()
	{
		for(Sound sound : soundMap.values())
		{
			if(sound.isPlaying)
			{
				sound.stopLoop();
			}
		}
	}
	
	public void kill()
	{
		for(Sound sound : soundMap.values())
		{
			sound.remove();
		}
		
		MekanismClient.audioHandler.soundMaps.remove(objRef);
	}
	
	public boolean isEmpty()
	{
		return soundMap.isEmpty();
	}
	
	public int size()
	{
		return soundMap.size();
	}
}
