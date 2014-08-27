package mekanism.client.sound;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.client;
import mekanism.client.HolidayManager;
import mekanism.common.Mekanism;
import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import paulscode.sound.SoundSystem;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
	public Map<Object, SoundMap> soundMaps = Collections.synchronizedMap(new HashMap<Object, SoundMap>());

	public static Minecraft mc = Minecraft.getMinecraft();
	
	public static final String CHANNEL_TILE_DEFAULT = "tile";
	public static final String CHANNEL_JETPACK = "jetpack";
	public static final String CHANNEL_GASMASK = "gasMask";
	public static final String CHANNEL_FLAMETHROWER = "flamethrower";

	/**
	 * SoundHandler -- a class that handles all Sounds used by Mekanism.
	 */
	public SoundHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);

		Mekanism.logger.info("Successfully set up SoundHandler.");
	}

	public void preloadSounds()
	{
		CodeSource src = getClass().getProtectionDomain().getCodeSource();
		String corePath = src.getLocation().getFile().split("/mekanism/client")[0];
		List<String> listings = listFiles(corePath.replace("%20", " ").replace(".jar!", ".jar").replace("file:", ""), "assets/mekanism/sounds");

		for(String s : listings)
		{
			if(s.contains("etc") || s.contains("holiday"))
			{
				continue;
			}

			if(s.contains("/mekanism/sounds/"))
			{
				s = s.split("/mekanism/sounds/")[1];
			}

			preloadSound(s);
		}

		Mekanism.logger.info("Preloaded " + listings.size() + " object sounds.");

		if(client.holidays)
		{
			listings = listFiles(corePath.replace("%20", " ").replace(".jar!", ".jar").replace("file:", ""), "assets/mekanism/sounds/holiday");

			for(String s : listings)
			{
				if(s.contains("/mekanism/sounds/"))
				{
					s = s.split("/mekanism/sounds/")[1];
				}

				if(!s.contains("holiday"))
				{
					s = "holiday/" + s;
				}

				preloadSound(s);
			}
		}
	}

	private List<String> listFiles(String path, String s)
	{
		List<String> names = new ArrayList<String>();

		File f = new File(path);

		if(!f.exists())
		{
			return names;
		}

		if(!f.isDirectory())
		{
			try {
				ZipInputStream zip = new ZipInputStream(new FileInputStream(path));

				while(true)
				{
					ZipEntry e = zip.getNextEntry();

					if(e == null)
					{
						break;
					}

					String name = e.getName();

					if(name.contains(s) && name.endsWith(".ogg"))
					{
						names.add(name);
					}
				}

				zip.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			f = new File(path + "/" + s);

			for(File file : f.listFiles())
			{
				if(file.getPath().contains(s) && file.getName().endsWith(".ogg"))
				{
					names.add(file.getName());
				}
			}
		}

		return names;
	}

	private void preloadSound(String sound)
	{
		String id = "pre_" + sound;
		URL url = getClass().getClassLoader().getResource("assets/mekanism/sounds/" + sound);

		if(getSoundSystem() != null)
		{
			getSoundSystem().newSource(false, id, url, sound, true, 0, 0, 0, 0, 16F);
			getSoundSystem().activate(id);
			getSoundSystem().removeSource(id);
		}
	}

	/**
	 * Ticks the sound handler.  Should be called every Minecraft tick, or 20 times per second.
	 */
	public void onTick()
	{
		synchronized(soundMaps)
		{
			if(getSoundSystem() != null)
			{
				if(!Mekanism.proxy.isPaused())
				{
					ArrayList<Sound> soundsToRemove = new ArrayList<Sound>();
					World world = FMLClientHandler.instance().getClient().theWorld;
					
					if(FMLClientHandler.instance().getClient().thePlayer != null && world != null)
					{
						for(SoundMap map : soundMaps.values())
						{
							for(Sound sound : map)
							{
								if(!sound.update(world))
								{
									soundsToRemove.add(sound);
									continue;
								}
								
								if(sound.isPlaying)
								{
									sound.updateVolume();
								}
							}
						}
	
						for(Sound sound : soundsToRemove)
						{
							sound.remove();
						}
					}
				}
				else {
					for(SoundMap map : soundMaps.values())
					{
						map.stopLoops();
					}
				}
			}
			else {
				Mekanism.proxy.unloadSoundHandler();
			}
		}
	}
	
	public void removeSound(Object ref, String channel)
	{
		if(soundMaps.get(ref) == null)
		{
			return;
		}
		
		soundMaps.get(ref).remove(channel);
		
		if(soundMaps.get(ref).isEmpty())
		{
			soundMaps.remove(ref);
		}
	}
	
	public void registerSound(Object ref, String channel, Sound sound)
	{
		if(soundMaps.get(ref) == null)
		{
			soundMaps.put(ref, new SoundMap(ref, channel, sound));
			return;
		}
		
		soundMaps.get(ref).add(channel, sound);
	}

	/**
	 * Gets a sound object from a specific TileEntity, null if there is none.
	 * @param tileEntity - the holder of the sound
	 * @return Sound instance
	 */
	public SoundMap getMap(Object ref)
	{
		synchronized(soundMaps)
		{
			return soundMaps.get(ref);
		}
	}
	
	public Sound getSound(Object ref, String channel)
	{
		if(soundMaps.get(ref) == null)
		{
			return null;
		}
		
		return soundMaps.get(ref).getSound(channel);
	}

	/**
	 * Get a unique identifier for a sound effect instance by combining the mod's name,
	 * Mekanism, the new sound's unique position on the 'sounds' ArrayList, and a random
	 * number between 0 and 10,000. Example: "Mekanism_6_6123"
	 * @return unique identifier
	 */
	public String getIdentifier(Object obj)
	{
		synchronized(soundMaps)
		{
			String toReturn = "Mekanism_" + getActiveSize() + "_" + new Random().nextInt(10000);

			return toReturn;
		}
	}
	
	public int getActiveSize()
	{
		int count = 0;
		
		for(SoundMap map : soundMaps.values())
		{
			count += map.size();
		}
		
		return count;
	}

	/**
	 * Plays a sound in a specific location.
	 * @param soundPath - sound path to play
	 * @param world - world to play in
	 * @param object - location to play
	 */
	public void quickPlay(String soundPath, World world, Coord4D object)
	{
		URL url = getClass().getClassLoader().getResource("assets/mekanism/sounds/" + soundPath);

		if(url == null)
		{
			Mekanism.logger.info("Invalid sound file: " + soundPath);
		}

		String s = getSoundSystem().quickPlay(false, url, soundPath, false, object.xCoord, object.yCoord, object.zCoord, 0, 16F);
		getSoundSystem().setVolume(s, getMasterVolume());
	}
	
	public float getMasterVolume()
	{
		return FMLClientHandler.instance().getClient().gameSettings.getSoundLevel(SoundCategory.MASTER);
	}

	public static SoundSystem getSoundSystem()
	{
		try {
			return (SoundSystem)MekanismUtils.getPrivateValue(getSoundManager(), SoundManager.class, ObfuscatedNames.SoundManager_sndSystem);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static SoundManager getSoundManager()
	{
		try {
			return (SoundManager)MekanismUtils.getPrivateValue(mc.getSoundHandler(), net.minecraft.client.audio.SoundHandler.class, ObfuscatedNames.SoundHandler_sndManager);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static boolean isSystemLoaded()
	{
		try {
			return (Boolean)MekanismUtils.getPrivateValue(getSoundManager(), net.minecraft.client.audio.SoundManager.class, new String[] {"loaded"});
		} catch(Exception e) {
			return false;
		}
	}
	
	public static void playSound(String sound)
	{
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(sound), 1.0F));
	}

	@SubscribeEvent
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
						if(getMap(tileEntity) != null)
						{
							if(soundMaps.containsKey(tileEntity))
							{
								getMap(tileEntity).kill();
							}
						}
					}
				}
			}
		}
	}
}
