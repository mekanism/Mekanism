package mekanism.client.sound;

import java.net.URL;

import mekanism.api.MekanismConfig.client;
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
	
	public String channel;

	protected Minecraft mc = Minecraft.getMinecraft();

	/**
	 * A sound that runs off of the PaulsCode sound system.
	 * @param id - unique identifier
	 * @param sound - bundled path to the sound
	 * @param tileentity - the tile this sound is playing from.
	 */
	public Sound(String id, String sound, String chan, Object obj, Pos3D loc)
	{
		if(MekanismClient.audioHandler.getSound(obj, chan) != null)
		{
			return;
		}

		synchronized(MekanismClient.audioHandler.soundMaps)
		{
			prevSoundPath = sound;
			identifier = id;
			objRef = obj;
			channel = chan;

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

			MekanismClient.audioHandler.registerSound(objRef, channel, this);
		}
	}

	/**
	 * Start looping the sound effect
	 */
	public void play()
	{
		synchronized(MekanismClient.audioHandler.soundMaps)
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
		synchronized(MekanismClient.audioHandler.soundMaps)
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
	 * Remove the sound effect from the PaulsCode SoundSystem and the Mekanism SoundHandler
	 */
	public void remove()
	{
		synchronized(MekanismClient.audioHandler.soundMaps)
		{
			if(isPlaying)
			{
				stopLoop();
			}

			MekanismClient.audioHandler.removeSound(objRef, channel);

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
		return doGradualEffect() ? Math.min(1, ((float)ticksSincePlay/30F)) : 1;
	}
	
	public boolean doGradualEffect()
	{
		return true;
	}

	/**
	 * Updates the volume based on how far away the player is from the machine.
	 * @param entityplayer - player who is near the machine, always Minecraft.thePlayer
	 */
	public void updateVolume()
	{
		synchronized(MekanismClient.audioHandler.soundMaps)
		{
			try {
				float multiplier = getMultiplier();
				float volume = 0;
				float masterVolume = MekanismClient.audioHandler.getMasterVolume();
				double distance = mc.thePlayer.getDistance(getLocation().xPos, getLocation().yPos, getLocation().zPos);
				volume = (float)Math.min(Math.max(masterVolume-((distance*.08F)*masterVolume), 0)*multiplier, 1);
				volume *= Math.max(0, Math.min(1, client.baseSoundVolume));

				if(SoundHandler.getSoundSystem() != null)
				{
					SoundHandler.getSoundSystem().setVolume(identifier, volume);
				}
			} catch(Exception e) {}
		}
	}
}
