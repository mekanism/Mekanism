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

import mekanism.api.Object3D;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
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
	public Map<Object, Sound> sounds = Collections.synchronizedMap(new HashMap<Object, Sound>());
	
	/** The current base volume Minecraft is using. */
	public float masterVolume = 0;
	
	public Minecraft mc = Minecraft.getMinecraft();
	
	/** 
	 * SoundHandler -- a class that handles all Sounds used by Mekanism.
	 */
	public SoundHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		System.out.println("[Mekanism] Successfully set up SoundHandler.");
	}
	
	public void preloadSounds()
	{
		CodeSource src = getClass().getProtectionDomain().getCodeSource();
		String corePath = src.getLocation().getFile().split("/mekanism/client")[0];
		List<String> listings = listFiles(corePath.replace("%20", " ").replace(".jar!", ".jar").replace("file:", ""), "assets/mekanism/sound");
		
		for(String s : listings)
		{
			if(s.contains("etc"))
			{
				continue;
			}
			
			if(s.contains("/mekanism/sound/"))
			{
				s = s.split("/mekanism/sound/")[1];
			}
			
			preloadSound(s);
		}
		
		System.out.println("[Mekanism] Preloaded " + listings.size() + " object sounds.");
		
		listings = listFiles(corePath.replace("%20", " ").replace(".jar!", ".jar").replace("file:", ""), "assets/mekanism/sound/etc");
		
		for(String s : listings)
		{
			if(s.contains("/mekanism/sound/etc/"))
			{
				s = s.split("/mekanism/sound/etc/")[1];
			}
			
			mc.sndManager.addSound("mekanism:etc/" + s);
		}
		
		System.out.println("[Mekanism] Initialized " + listings.size() + " sound effects.");
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
		URL url = getClass().getClassLoader().getResource("assets/mekanism/sound/" + sound);

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
							if(!sound.update(world))
							{
								soundsToRemove.add(sound);
								continue;
							}
						}
					}
					
					for(Sound sound : soundsToRemove)
					{
						sound.remove();
					}
					
					for(Sound sound : sounds.values())
					{
						if(sound.isPlaying)
						{
							sound.updateVolume(FMLClientHandler.instance().getClient().thePlayer);
						}
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
	public Sound getFrom(Object obj)
	{
		synchronized(sounds)
		{
			return sounds.get(obj);
		}
	}
	
	/**
	 * Create and return an instance of a Sound.
	 * @param tileEntity - the holder of this sound.
	 * @return Sound instance
	 */
	public void register(Object obj)
	{
		if(obj instanceof TileEntity && !(obj instanceof IHasSound))
		{
			return;
		}
		
		synchronized(sounds)
		{
			if(getFrom(obj) == null)
			{
				if(obj instanceof TileEntity)
				{
					new TileSound(getIdentifier(), ((IHasSound)obj).getSoundPath(), (TileEntity)obj);
				}
				else if(obj instanceof EntityPlayer)
				{
					new JetpackSound(getIdentifier(), "Jetpack.ogg", (EntityPlayer)obj);
				}
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
