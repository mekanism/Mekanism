package mekanism.client;

import java.net.URL;

import mekanism.common.Mekanism;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.client.FMLClientHandler;
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
			
			URL url = getClass().getClassLoader().getResource("mods/mekanism/sound/" + sound);
			if(url == null)
			{
				System.out.println("[Mekanism] Invalid sound file: " + sound);
			}
			
			if(Mekanism.audioHandler.soundSystem != null)
			{
				Mekanism.audioHandler.soundSystem.newSource(false, id, url, sound, true, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0, 16F);
				updateVolume(FMLClientHandler.instance().getClient().thePlayer);
				Mekanism.audioHandler.soundSystem.activate(id);
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
			
			Mekanism.audioHandler.sounds.remove(tileEntity);
			
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
			if(entityplayer != null && tileEntity != null && entityplayer.worldObj == tileEntity.worldObj)
			{
		    	float volume = 0;
		    	
		        double distanceVolume = (entityplayer.getDistanceSq(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord)*0.008);
		        volume = (float)(Math.max(Mekanism.audioHandler.masterVolume-distanceVolume, 0))*0.05F;
		
		        if(Mekanism.audioHandler.soundSystem != null)
		        {
		        	Mekanism.audioHandler.soundSystem.setVolume(identifier, volume);
		        }
			}
		}
    }
}
