package mekanism.client;

import java.net.URL;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Sound -- an object that is created in SoundHandler. A 'Sound' object runs off of
 * PaulsCode's SoundSystem. It has several methods; play(), for looping the clip,
 * stop(), for stopping the loop, remove(), for removing the sound from PaulsCode,
 * and updateVolume() for updating the volume based on where the player is.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
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
	
	/** The dimension ID this sound is playing in */
	public int dimensionId;
	
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
	public Sound(String id, String sound, World world, int x, int y, int z, int dim)
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			soundPath = sound;
			identifier = id;
			xCoord = x;
			yCoord = y;
			zCoord = z;
			dimensionId = dim;
			
			URL url = getClass().getClassLoader().getResource("mods/mekanism/sound/" + sound);
			if(url == null)
			{
				System.out.println("[Mekanism] Invalid sound file: " + sound);
			}
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.newSource(false, id, url, sound, true, x, y, z, 0, 16F);
				updateVolume(FMLClientHandler.instance().getClient().thePlayer);
				Mekanism.audioHandler.soundSystem.activate(id);
			}
			
			Mekanism.audioHandler.sounds.add(this);
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
				updateVolume(FMLClientHandler.instance().getClient().thePlayer);
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
				updateVolume(FMLClientHandler.instance().getClient().thePlayer);
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
				updateVolume(FMLClientHandler.instance().getClient().thePlayer);
				Mekanism.audioHandler.soundSystem.removeSource(identifier);
			}
		}
	}
	
	/**
	 * Updates the volume based on how far away the player is from the machine.
	 * @param entityplayer - player who is near the machine, always Minecraft.thePlayer
	 */
    public void updateVolume(EntityPlayer entityplayer)
    {
		synchronized(Mekanism.audioHandler.sounds)
		{
			if(entityplayer.worldObj.provider.dimensionId == dimensionId)
			{
		    	float volume = 0;
		    	
		        double distanceVolume = (entityplayer.getDistanceSq(xCoord, yCoord, zCoord)*0.008);
		        volume = (float)(Math.max(Mekanism.audioHandler.masterVolume-distanceVolume, 0))*0.05F;
		
		        if(Mekanism.audioHandler.soundSystem != null)
		        {
		        	Mekanism.audioHandler.soundSystem.setVolume(identifier, volume);
		        }
			}
		}
    }
}
