package mekanism.client.sound;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.Mekanism;
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
 * Runs off of PaulsCode's SoundSystem.
 * @author AidanBrady
 *
 */
@SideOnly(Side.CLIENT)
public class SoundHandler
{
	public Map<String, Map<String, IResettableSound>> soundMaps = new HashMap<String, Map<String, IResettableSound>>();

	public static Map<ISound, String> invPlayingSounds;

	public static Minecraft mc = Minecraft.getMinecraft();

	public enum Channel
	{
		JETPACK("jetpack"),
		GASMASK("gasMask"),
		FLAMETHROWER("flamethrower");

		String channelName;

		private Channel(String name)
		{
			channelName = name;
		}

		public String getName()
		{
			return channelName;
		}
	}

	public boolean hasSound(EntityPlayer player, Channel channel)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());

		return sound != null;
	}

	public void addSound(EntityPlayer player, Channel channel, IResettableSound newSound, boolean replace)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());
		if(sound == null || replace)
		{
			map.put(channel.getName(), newSound);
		}
	}

	public boolean playSound(EntityPlayer player, Channel channel)
	{
		String name = player.getCommandSenderName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());
		if(sound != null)
		{
			if(sound.isDonePlaying() && !getSoundMap().containsKey(sound))
			{
				sound.reset();
				Mekanism.logger.info("Playing sound " + sound);
				playSound(sound);
			}
			return true;
		}
		return false;
	}

	public Map<String, IResettableSound> getMap(String name)
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
		try {
			invPlayingSounds = (Map<ISound, String>)MekanismUtils.getPrivateValue(getSoundManager(), net.minecraft.client.audio.SoundManager.class, ObfuscatedNames.SoundManager_invPlayingSounds);
		} catch(Exception e) {
			invPlayingSounds = null;
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
