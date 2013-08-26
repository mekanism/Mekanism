package mekanism.client.sound;

import java.net.URL;

import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	
	/** The TileEntity this sound is associated with. */
	public TileEntity tileEntity;
	
	/** Whether or not this sound is playing */
	public boolean isPlaying = false;
	
	/**
	 * A sound that runs off of the PaulsCode sound system.
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param tileentity - the tile this sound is playing from.
	 */
	public Sound(String id, String sound, TileEntity tileentity)
	{
		synchronized(Mekanism.audioHandler.sounds)
		{
			soundPath = sound;
			identifier = id;
			tileEntity = tileentity;
			
			URL url = getClass().getClassLoader().getResource("assets/mekanism/sound/" + sound);
			
			if(url == null)
			{
				System.out.println("[Mekanism] Invalid sound file: " + sound);
			}
			
			if(SoundHandler.getSoundSystem() != null)
			{
				SoundHandler.getSoundSystem().newSource(false, id, url, sound, true, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0, 16F);
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().activate(id);
			}
			
			Mekanism.audioHandler.sounds.put(tileEntity, this);
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
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().play(identifier);
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
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().stop(identifier);
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
			
			Mekanism.audioHandler.sounds.remove(tileEntity);
			
			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume(Minecraft.getMinecraft().thePlayer);
				SoundHandler.getSoundSystem().removeSource(identifier);
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
			if(entityplayer != null && tileEntity != null && entityplayer.worldObj == tileEntity.worldObj)
			{
				float multiplier = ((IHasSound)tileEntity).getVolumeMultiplier();
		    	float volume = 0;
		    	float masterVolume = Mekanism.audioHandler.masterVolume;
		    	
		        double distance = entityplayer.getDistance(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
		        volume = (float)Math.min(Math.max(masterVolume-((distance*.08F)*masterVolume), 0)*multiplier, 1);
		
		        if(SoundHandler.getSoundSystem() != null)
		        {
		        	SoundHandler.getSoundSystem().setVolume(identifier, volume);
		        }
			}
		}
    }
}
