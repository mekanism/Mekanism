package mekanism.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import mekanism.common.IActiveState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
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
	
	/** All the sound references in the Minecraft game. */
	public List<Sound> sounds = Collections.synchronizedList(new ArrayList<Sound>());
	
	/** The current base volume Minecraft is using. */
	public float masterVolume = 0;
	
	/** SoundHandler -- a class that handles all Sounds used by Mekanism. */
	public SoundHandler()
	{
		if(soundSystem == null)
		{
			soundSystem = FMLClientHandler.instance().instance().getClient().sndManager.sndSystem;
			MinecraftForge.EVENT_BUS.register(this);
			System.out.println("[Mekanism] Successfully set up SoundHandler.");
		}
	}
	
	/**
	 * Ticks the sound handler.  Should be called every Minecraft tick, or 20 times per second.
	 */
	public void onTick()
	{
		synchronized(sounds)
		{
			ArrayList<Sound> soundsToRemove = new ArrayList<Sound>();
			for(Sound sound : sounds)
			{
				if(FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().theWorld != null)
				{
					if(sound.isPlaying)
					{
						sound.updateVolume(FMLClientHandler.instance().getClient().thePlayer);
					}
					
					TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(sound.xCoord, sound.yCoord, sound.zCoord);
					
					if(tileEntity instanceof IActiveState && tileEntity instanceof IHasSound)
					{
						if(((IHasSound)tileEntity).getSound().soundPath != sound.soundPath)
						{
							soundsToRemove.add(sound);
							continue;
						}
						if(((IActiveState)tileEntity).getActive() != sound.isPlaying)
						{
							if(((IActiveState)tileEntity).getActive())
							{
								sound.play();
							}
							else {
								sound.stopLoop();
							}
						}
					}
					else if(tileEntity == null)
					{
						soundsToRemove.add(sound);
					}
				}
			}
			
			for(Sound sound : soundsToRemove)
			{
				sound.remove();
				
				TileEntity tileEntity = FMLClientHandler.instance().getClient().theWorld.getBlockTileEntity(sound.xCoord, sound.yCoord, sound.zCoord);
				
				if(tileEntity instanceof IHasSound)
				{
					((IHasSound)tileEntity).removeSound();
				}
			}
			
			masterVolume = FMLClientHandler.instance().getClient().gameSettings.soundVolume;
		}
	}
	
	/**
	 * Create and return an instance of a Sound.
	 * @param name - unique identifier for this sound
	 * @param path - bundled path to the sound effect
	 * @param world - world to play sound in
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 * @return Sound instance
	 */
	public Sound getSound(String path, World world, int x, int y, int z)
	{
		synchronized(sounds)
		{
			return new Sound(getIdentifier(), path, world, x, y, z);
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
			return "Mekanism_" + sounds.size() + "_" + new Random().nextInt(10000);
		}
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
						if(sounds.contains(((IHasSound)tileEntity).getSound()))
						{
							sounds.remove(((IHasSound)tileEntity).getSound());
						}
						
						((IHasSound)tileEntity).removeSound();
					}
				}
			}
		}
	}
}
