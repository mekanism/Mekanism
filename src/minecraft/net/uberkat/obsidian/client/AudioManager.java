package net.uberkat.obsidian.client;

import java.lang.reflect.Field;
import java.net.URL;

import net.minecraft.src.SoundManager;
import net.uberkat.obsidian.common.Sound;

import paulscode.sound.SoundSystem;

public class AudioManager 
{
	public static SoundSystem system;
	public static float defaultVolume = 1.2F;
    public static float fadingDistance = 16F;
    public static float masterVolume = 0.5F;
    public static int nextId = 0;
	
	public static void getSoundSystem()
	{
        Field afield[] = (SoundManager.class).getDeclaredFields();
        int i = afield.length;
        int j = 0;

        do
        {
            if (j >= i)
            {
                break;
            }

            Field field = afield[j];

            if (field.getType() == (paulscode.sound.SoundSystem.class))
            {
                field.setAccessible(true);

                try
                {
                    Object obj = field.get(null);

                    if (obj instanceof SoundSystem)
                    {
                        system = (SoundSystem)obj;
                    }
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                    system = null;
                }

                break;
            }

            j++;
        }
        while (true);
	}
	
	public static AudioSource getSource(Sound sound)
	{
        if (system == null)
        {
            getSoundSystem();
        }

        if (system == null)
        {
            return null;
        }
        
        String id = getSourceName(nextId);
        nextId++;
        AudioSource audiosource = new AudioSource(system, sound, id);

        return audiosource;
	}
	
    private static String getSourceName(int i)
    {
        return (new StringBuilder()).append("asm_snd").append(i).toString();
    }
	
	public static void playSoundOnce(Sound sound)
	{
		URL url = (SoundHandler.class).getClassLoader().getResource((new StringBuilder()).append("sounds/").append(sound.name).toString());
		
        if (system == null)
        {
            getSoundSystem();
        }

        if (system == null)
        {
            return;
        }
        
        String audio = system.quickPlay(false, url, sound.name, false, sound.x, sound.y, sound.z, 2, fadingDistance * Math.max(defaultVolume, 1.0F));
        system.setVolume(audio, masterVolume * defaultVolume);
	}
}
