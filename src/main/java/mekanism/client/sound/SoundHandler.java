package mekanism.client.sound;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.ObfuscatedNames;
import mekanism.common.util.MekanismUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * SoundHandler - a class that handles all Sounds used by Mekanism.
 * Runs off of PaulsCode's SoundSystem through Minecraft.
 * @author AidanBrady rewritten by unpairedbracket
 *
 */
@SideOnly(Side.CLIENT)
public class SoundHandler
{
	public static Map<String, Map<String, IResettableSound>> soundMaps = new HashMap<String, Map<String, IResettableSound>>();

	public static Map<ISound, String> invPlayingSounds;

	public static Minecraft mc = Minecraft.getMinecraft();

	public static enum Channel
	{
		JETPACK("jetpack", JetpackSound.class),
		GASMASK("gasMask", GasMaskSound.class),
		FLAMETHROWER("flamethrower", FlamethrowerSound.class);

		String channelName;
		Class<? extends PlayerSound> soundClass;

		private Channel(String name, Class<? extends PlayerSound> clazz)
		{
			channelName = name;
			soundClass = clazz;
		}

		public String getName()
		{
			return channelName;
		}

		public PlayerSound getNewSound(EntityPlayer player)
		{
			try {
				return soundClass.getDeclaredConstructor(EntityPlayer.class).newInstance(player);
			} catch(Exception e) {
				return null;
			}
		}
	}

	public static boolean soundPlaying(EntityPlayer player, Channel channel)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());

		return !(sound == null || sound.isDonePlaying());
	}

	public static void addSound(EntityPlayer player, Channel channel, boolean replace)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());
		
		if(sound == null || replace)
		{
			PlayerSound newSound = channel.getNewSound(player);
			map.put(channel.getName(), newSound);
		}
	}

	public static boolean playSound(EntityPlayer player, Channel channel)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());
		
		if(sound != null)
		{
			if(canRestartSound(sound))
			{
				sound.reset();
				playSound(sound);
			}
			
			return true;
		}
		
		return false;
	}

	public static Map<String, IResettableSound> getMap(String name)
	{
		Map<String, IResettableSound> map = soundMaps.get(name);
		
		if(map == null)
		{
			map = new HashMap<String, IResettableSound>();
			soundMaps.put(name, map);
		}

		return map;
	}

	public static SoundManager getSoundManager()
	{
		try {
			return (SoundManager)MekanismUtils.getPrivateValue(mc.getSoundHandler(), net.minecraft.client.audio.SoundHandler.class, ObfuscatedNames.SoundHandler_sndManager);
		} catch(Exception e) {
			return null;
		}
	}

	//Fudge required because sound thread gets behind and the biMap crashes when rapidly toggling sounds.
	public static Map<ISound, String> getSoundMap()
	{
		if(invPlayingSounds == null)
		{
			try {
				invPlayingSounds = (Map<ISound, String>)MekanismUtils.getPrivateValue(getSoundManager(), net.minecraft.client.audio.SoundManager.class, ObfuscatedNames.SoundManager_invPlayingSounds);
			} catch(Exception e) {
				invPlayingSounds = null;
			}
		}
		
		return invPlayingSounds;
	}

	public static boolean canRestartSound(ITickableSound sound)
	{
		return sound.isDonePlaying() && !getSoundMap().containsKey(sound);
	}
	
	public static void playSound(String sound)
	{
        playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(sound), 1.0F));
	}

	public static void playSound(ISound sound)
	{
		mc.getSoundHandler().playSound(sound);
	}
}
