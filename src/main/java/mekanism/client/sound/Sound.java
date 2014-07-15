package mekanism.client.sound;

import java.net.URL;

import mekanism.api.Pos3D;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public abstract class Sound
{
	/** The bundled path where the sound is */
	public String prevSoundPath;

	/** A unique identifier for this sound */
	public String identifier;

	/** Whether or not this sound is playing */
	public boolean isPlaying = false;

	public int ticksSincePlay = 0;

	private Object objRef;

	protected Minecraft mc = Minecraft.getMinecraft();

	/**
	 * A sound that runs off of the PaulsCode sound system.
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param tileentity - the tile this sound is playing from.
	 */
	public Sound(String id, String sound, Object obj, Pos3D loc)
	{
		if(MekanismClient.audioHandler.getFrom(obj) != null)
		{
			return;
		}

		synchronized(MekanismClient.audioHandler.sounds)
		{
			prevSoundPath = sound;
			identifier = id;
			objRef = obj;

			URL url = getClass().getClassLoader().getResource("assets/mekanism/sounds/" + sound);

			if(url == null)
			{
				Mekanism.logger.error("Invalid sound file: " + sound);
			}

			if(SoundHandler.getSoundSystem() != null)
			{
				SoundHandler.getSoundSystem().newSource(false, id, url, sound, true, (float)loc.xPos, (float)loc.yPos, (float)loc.zPos, 0, 16F);
				updateVolume();
				SoundHandler.getSoundSystem().activate(id);
			}

			MekanismClient.audioHandler.sounds.put(obj, this);
		}
	}

	/**
	 * Start looping the sound effect
	 */
	public void play()
	{
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(isPlaying)
			{
				return;
			}

			ticksSincePlay = 0;

			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume();
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
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(!isPlaying)
			{
				return;
			}

			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume();
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
		synchronized(MekanismClient.audioHandler.sounds)
		{
			if(isPlaying)
			{
				stopLoop();
			}

			MekanismClient.audioHandler.sounds.remove(objRef);

			if(SoundHandler.getSoundSystem() != null)
			{
				updateVolume();
				SoundHandler.getSoundSystem().removeSource(identifier);
			}
		}
	}
	
	public String getSoundPath()
	{
		return prevSoundPath;
	}

	public boolean update(World world)
	{
		if(!getSoundPath().equals(prevSoundPath))
		{
			return false;
		}
		
		return true;
	}

	public abstract Pos3D getLocation();

	public float getMultiplier()
	{
		return Math.min(1, ((float)ticksSincePlay/30F));
	}

	/**
	 * Updates the volume based on how far away the player is from the machine.
	 * @param entityplayer - player who is near the machine, always Minecraft.thePlayer
	 */
	public void updateVolume()
	{
		synchronized(MekanismClient.audioHandler.sounds)
		{
			try {
				float multiplier = getMultiplier();
				float volume = 0;
				float masterVolume = MekanismClient.audioHandler.getMasterVolume();
				double distance = mc.thePlayer.getDistance(getLocation().xPos, getLocation().yPos, getLocation().zPos);
				volume = (float)Math.min(Math.max(masterVolume-((distance*.08F)*masterVolume), 0)*multiplier, 1);
				volume *= Math.max(0, Math.min(1, MekanismClient.baseSoundVolume));

				if(SoundHandler.getSoundSystem() != null)
				{
					SoundHandler.getSoundSystem().setVolume(identifier, volume);
				}
			} catch(Exception e) {}
		}
	}
}
