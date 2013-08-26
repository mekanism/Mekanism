package mekanism.client.sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import mekanism.api.Object3D;
import mekanism.common.IActiveState;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import paulscode.sound.SoundSystem;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * SoundHandler - a class that handles all Sounds used by Mekanism.
 * Runs off of PaulsCode's SoundSystem.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class SoundHandler 
{
	/** All the sound references in the Minecraft game. */
	public Map<TileEntity, Sound> sounds = Collections.synchronizedMap(new HashMap<TileEntity, Sound>());
	
	/** The current base volume Minecraft is using. */
	public float masterVolume = 0;
	
	/** 
	 * SoundHandler -- a class that handles all Sounds used by Mekanism.
	 */
	public SoundHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
		System.out.println("[Mekanism] Successfully set up SoundHandler.");
	}
	
	/**
	 * Ticks the sound handler.  Should be called every Minecraft tick, or 20 times per second.
	 */
	public void onTick()
	{
		synchronized(sounds)
		{
			if(getSoundSystem() != null)
			{
				if(!Mekanism.proxy.isPaused())
				{
					ArrayList<Sound> soundsToRemove = new ArrayList<Sound>();
					World world = FMLClientHandler.instance().getClient().theWorld;
					for(Sound sound : sounds.values())
					{
						if(FMLClientHandler.instance().getClient().thePlayer != null && world != null)
						{
							if(!(sound.tileEntity instanceof IHasSound))
							{
								soundsToRemove.add(sound);
								continue;
							}
							else if(world.getBlockTileEntity(sound.tileEntity.xCoord, sound.tileEntity.yCoord, sound.tileEntity.zCoord) != sound.tileEntity)
							{
								soundsToRemove.add(sound);
								continue;
							}
							else if(!((IHasSound)sound.tileEntity).getSoundPath().equals(sound.soundPath))
							{
								soundsToRemove.add(sound);
								continue;
							}
							else if(sound.tileEntity instanceof IActiveState)
							{
								if(((IActiveState)sound.tileEntity).getActive() != sound.isPlaying)
								{
									if(((IActiveState)sound.tileEntity).getActive())
									{
										sound.play();
									}
									else {
										sound.stopLoop();
									}
								}
							}
							
							if(sound.isPlaying)
							{
								sound.updateVolume(FMLClientHandler.instance().getClient().thePlayer);
							}
						}
					}
					
					for(Sound sound : soundsToRemove)
					{
						sound.remove();
					}
					
					masterVolume = FMLClientHandler.instance().getClient().gameSettings.soundVolume;
				}
				else {
					for(Sound sound : sounds.values())
					{
						if(sound.isPlaying)
						{
							sound.stopLoop();
						}
					}
				}
			}
			else {
				Mekanism.proxy.unloadSoundHandler();
			}
		}
	}
	
	/**
	 * Gets a sound object from a specific TileEntity, null if there is none.
	 * @param tileEntity - the holder of the sound
	 * @return Sound instance
	 */
	public Sound getFrom(TileEntity tileEntity)
	{
		synchronized(sounds)
		{
			return sounds.get(tileEntity);
		}
	}
	
	/**
	 * Create and return an instance of a Sound.
	 * @param tileEntity - the holder of this sound.
	 * @return Sound instance
	 */
	public void register(TileEntity tileEntity)
	{
		if(!(tileEntity instanceof IHasSound))
		{
			return;
		}
		
		synchronized(sounds)
		{
			if(getFrom(tileEntity) == null)
			{
				new Sound(getIdentifier(), ((IHasSound)tileEntity).getSoundPath(), tileEntity);
			}
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
			String toReturn = "Mekanism_" + sounds.size() + "_" + new Random().nextInt(10000);
			
			for(Sound sound : sounds.values())
			{
				if(sound.identifier.equals(toReturn))
				{
					return getIdentifier();
				}
			}
			
			return toReturn;
		}
	}
	
	/**
	 * Plays a sound in a specific location.
	 * @param soundPath - sound path to play
	 * @param world - world to play in
	 * @param object - location to play
	 */
	public void quickPlay(String soundPath, World world, Object3D object)
	{
		URL url = getClass().getClassLoader().getResource("assets/mekanism/sound/" + soundPath);
		
		if(url == null)
		{
			System.out.println("[Mekanism] Invalid sound file: " + soundPath);
		}
		
		String s = getSoundSystem().quickPlay(false, url, soundPath, false, object.xCoord, object.yCoord, object.zCoord, 0, 16F);
		getSoundSystem().setVolume(s, masterVolume);
	}
	
	public static SoundSystem getSoundSystem()
	{
		return Minecraft.getMinecraft().sndManager.sndSystem;
	}
	
	@ForgeSubscribe
	public void onChunkUnload(ChunkEvent.Unload event)
	{
		if(event.getChunk() != null)
		{
			for(Object obj : event.getChunk().chunkTileEntityMap.values())
			{
				if(obj instanceof TileEntity)
				{
					TileEntity tileEntity = (TileEntity)obj;
					
					if(tileEntity instanceof IHasSound)
					{
						if(getFrom(tileEntity) != null)
						{
							if(sounds.containsKey(tileEntity))
							{
								getFrom(tileEntity).remove();
							}
						}
					}
				}
			}
		}
	}
}
