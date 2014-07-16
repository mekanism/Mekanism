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
	
	public SoundMap(Object obj, String path, Sound sound)
	{
		this(obj);
		
		soundMap.put(path, sound);
	}

	@Override
	public Iterator<Sound> iterator() 
	{
		return soundMap.values().iterator();
	}
	
	public Sound getSound(String path)
	{
		return soundMap.get(path);
	}
	
	public boolean hasSound(String path)
	{
		return soundMap.containsKey(path);
	}
	
	public void remove(String path)
	{
		soundMap.remove(path);
	}
	
	public void add(String path, Sound sound)
	{
		soundMap.put(path, sound);
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
}
