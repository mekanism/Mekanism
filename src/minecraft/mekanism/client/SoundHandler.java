package mekanism.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mekanism.common.TileEntityBasicMachine;
import net.minecraft.world.World;
import paulscode.sound.SoundSystem;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * SoundHandler - a class that handles all Sounds used by Mekanism.
 * Runs off of PaulsCode's SoundSystem.
 * @author AidanBrady
 *
 */
public class SoundHandler 
{
	/** The PaulsCode SoundSystem */
	public SoundSystem soundSystem;
	
	public List<Sound> sounds = Collections.synchronizedList(new ArrayList<Sound>());
	
	public float masterVolume = 0;
	
	/** SoundHandler -- a class that handles all Sounds used by Mekanism. */
	public SoundHandler()
	{
		if(soundSystem == null)
		{
			soundSystem = FMLClientHandler.instance().instance().getClient().sndManager.sndSystem;
			System.out.println("[Mekanism] Successfully set up SoundHandler.");
		}
	}
	
	public void onTick()
	{
		synchronized(sounds)
		{
			for(Sound sound : sounds)
			{
				if(FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().theWorld != null)
				{
					sound.distanceUpdate(FMLClientHandler.instance().getClient().thePlayer);
				}
			}
			
			masterVolume = FMLClientHandler.instance().getClient().gameSettings.soundVolume;
		}
	}
	
	/** Create and return an instance of a Sound.
	 * 
	 * @param name - unique identifier for this sound
	 * @param path - bundled path to the sound effect
	 * @param world - world to play sound in
	 * @param x - x coord
	 * @param y - y coord
	 * @param z - z coord
	 * @return Sound instance
	 */
	public Sound getSound(String path, World world, int x, int y, int z)
	{
		synchronized(sounds)
		{
			return new Sound(getIdentifier(), path, world, x, y, z);
		}
	}
	
	/**
	 * Get a unique identifier for a sound effect instance by combining the mod's name,
	 * Mekanism, the new sound's unique position on the 'sounds' ArrayList, and a random
	 * number between 0 and 10,000. Example: "Mekanism_6_6123"
	 * @return unique identifier
	 */
	public String getIdentifier()
	{
		synchronized(sounds)
		{
			return "Mekanism_" + sounds.size()+1 + "_" + new Random().nextInt(10000);
		}
	}
}
