package mekanism.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	
	public List<Sound> sounds = new ArrayList<Sound>();
	
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
		for(Sound sound : sounds)
		{
			if(FMLClientHandler.instance().getClient().theWorld != null && FMLClientHandler.instance().getClient().thePlayer != null)
			sound.updateVolume(FMLClientHandler.instance().getClient().thePlayer);
		}
		
		masterVolume = FMLClientHandler.instance().getClient().gameSettings.soundVolume;
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
	public Sound getSound(String name, String path, World world, int x, int y, int z)
	{
		if(soundSystem != null)
		{
			return new Sound(soundSystem, getSoundName(name), path, world, x, y, z);
		}
		else {
			soundSystem = FMLClientHandler.instance().getClient().sndManager.sndSystem;
			return new Sound(soundSystem, getSoundName(name), path, world, x, y, z);
		}
	}
	
	/**
	 * Get a unique identifier for a sound effect instance by getting adding a random
	 * number between 0 and 10,000 to the end of the effect's name. Example:
	 * EnrichmentChamber_2859
	 * @param s - sound name
	 * @return unique identifier
	 */
	public String getSoundName(String s)
	{
		Random random = new Random();
		return s + "_" + random.nextInt(10000);
	}
}
