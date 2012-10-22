package net.uberkat.obsidian.client;

import java.io.File;
import java.net.URL;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;

import paulscode.sound.SoundSystem;

/**
 * Sound -- an object that is created in SoundHandler. A 'Sound' object runs off of
 * PaulsCode's SoundSystem. It has several methods; play(), for looping the clip,
 * stop(), for stopping the loop, remove(), for removing the sound from PaulsCode,
 * and updateVolume() for updating the volume based on where the player is.
 * @author AidanBrady
 *
 */
public class Sound 
{
	/** The PaulsCode SoundSystem */
	public SoundSystem soundSystem;
	
	/** The bundled path where the sound is */
	public String soundPath;
	/** A unique identifier for this sound */
	public String identifier;
	
	/** X coordinate of this sound effect */
	public int xCoord;
	/** Y coordinate of this sound effect */
	public int yCoord;
	/** Z coordinate of this sound effect */
	public int zCoord;
	
	/** The world in which this sound is playing */
	public World worldObj;
	
	/** Whether or not this sound is playing */
	public boolean isPlaying = false;
	
	/** A sound, an object that runs off of the PaulsCode sound system.
	 * 
	 * @param system - PaulsCode SoundSystem
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param world - world the sound is playing
	 * @param x - x coord
	 * @param y - y coord
	 * @param z - z coord
	 */
	public Sound(SoundSystem system, String id, String sound, World world, int x, int y, int z)
	{
		soundSystem = system;
		soundPath = sound;
		identifier = id;
		worldObj = world;
		xCoord = x;
		yCoord = y;
		zCoord = z;
		
		URL url = getClass().getClassLoader().getResource("sounds/" + sound);
		if(url == null)
		{
			System.out.println("[ObsidianIngots] Invalid sound file: " + sound);
		}
		
		soundSystem.newSource(false, id, url, sound, true, x, y, z, 0, 16F);
		soundSystem.activate(id);
	}
	
	/** Start looping the sound effect */
	public void play()
	{
		if(isPlaying)
		{
			return;
		}
		
		soundSystem.play(identifier);
		isPlaying = true;
	}
	
	/** Stop looping the sound effect */
	public void stop()
	{
		if(!isPlaying)
		{
			return;
		}
		
		soundSystem.stop(identifier);
		isPlaying = false;
	}
	
	/** Remove the sound effect from the PaulsCode SoundSystem */
	public void remove()
	{
		if(isPlaying)
		{
			stop();
		}
		soundSystem.removeSource(identifier);
	}
	
	/** Updates the volume based on how far away the player is from the machine
	 * 
	 * @param entityplayer - player who is near the machine, usually Minecraft.thePlayer
	 */
    public void updateVolume(EntityPlayer entityplayer)
    {
    	float volume = 0;
        if (!isPlaying)
        {
            volume = 0.0F;
            return;
        }
        
        double playerDistance = entityplayer.posX + entityplayer.posY + entityplayer.posZ;
        double machineDistance = xCoord + yCoord + zCoord;
        double distanceVolume = Math.abs((playerDistance-machineDistance)*0.1);
        
        volume = (float)Math.max(1.0F-distanceVolume, 0);

        soundSystem.setVolume(identifier, volume);
    }
}
