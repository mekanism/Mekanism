package net.uberkat.obsidian.client;

import java.util.Random;

import cpw.mods.fml.client.FMLClientHandler;
import paulscode.sound.SoundSystem;
import net.minecraft.src.*;

/**
 * SoundHandler - a class that handles all Sounds used by Obsidian Ingots.
 * Runs off of PaulsCode's SoundSystem.
 * @author AidanBrady
 *
 */
public class SoundHandler 
{
	/** The PaulsCode SoundSystem */
	public SoundSystem soundSystem;
	
	/** SoundHandler -- a class that handles all Sounds used by Obsidian Ingots. */
	public SoundHandler()
	{
		if(soundSystem == null)
		{
			soundSystem = FMLClientHandler.instance().instance().getClient().sndManager.sndSystem;
			System.out.println("[ObsidianIngots] Successfully set up SoundHandler.");
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
	public Sound getSound(String name, String path, World world, int x, int y, int z)
	{
		return new Sound(soundSystem, getSoundName(name), path, world, x, y, z);
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
