package mekanism.client;

import java.net.URL;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
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
	public Sound(String id, String sound, World world, int x, int y, int z)
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			soundPath = sound;
			identifier = id;
			xCoord = x;
			yCoord = y;
			zCoord = z;
			
			URL url = getClass().getClassLoader().getResource("resources/mekanism/sound/" + sound);
			if(url == null)
			{
				System.out.println("[Mekanism] Invalid sound file: " + sound);
			}
			
			Mekanism.audioHandler.sounds.add(this);
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.newSource(false, id, url, sound, true, x, y, z, 0, 16F);
				Mekanism.audioHandler.soundSystem.activate(id);
			}
		}
	}
	
	/** 
	 * Start looping the sound effect 
	 */
	public void play()
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(isPlaying)
			{
				return;
			}
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.play(identifier);
			}
			isPlaying = true;
		}
	}
	
	/** 
	 * Stop looping the sound effect 
	 */
	public void stopLoop()
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(!isPlaying)
			{
				return;
			}
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.stop(identifier);
			}
			isPlaying = false;
		}
	}
	
	/** 
	 * Remove the sound effect from the PaulsCode SoundSystem 
	 */
	public void remove()
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(isPlaying)
			{
				stopLoop();
			}
			
			Mekanism.audioHandler.sounds.remove(this);
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.removeSource(identifier);
			}
		}
	}
	
	/**
	 * Updates the volume based on how far away the player is from the machine.
	 * @param entityplayer - player who is near the machine, always Minecraft.thePlayer
	 */
    public void distanceUpdate(EntityPlayer entityplayer)
    {
		synchronized(Mekanism.audioHandler.sounds)
		{
	    	float volume = 0;
	    	
	        if (!isPlaying)
	        {
	            return;
	        }
	        
	        double distanceVolume = entityplayer.getDistanceSq(xCoord, yCoord, zCoord)*0.01;
	        volume = (float)Math.max(Mekanism.audioHandler.masterVolume-distanceVolume, 0);
	
	        if(Mekanism.audioHandler.soundSystem != null)
	        {
	        	Mekanism.audioHandler.soundSystem.setVolume(identifier, volume);
	        }
		}
    }
}
