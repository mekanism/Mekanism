package mekanism.client.sound;

import java.util.HashMap;
import java.util.Map;

import mekanism.common.config.MekanismConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * SoundHandler - a class that handles all Sounds used by Mekanism.
 * Runs off of PaulsCode's SoundSystem through Minecraft.
 * @author AidanBrady rewritten by unpairedbracket
 *
 */
@SideOnly(Side.CLIENT)
public class SoundHandler
{
	public static Map<String, Map<String, IResettableSound>> soundMaps = new HashMap<>();

	public static Map<ISound, String> invPlayingSounds;

	public static Minecraft mc = Minecraft.getMinecraft();

	public enum Channel
	{
		JETPACK("jetpack", JetpackSound.class),
		GASMASK("gasMask", GasMaskSound.class),
		FLAMETHROWER("flamethrower", FlamethrowerSound.class);

		String channelName;
		Class<? extends PlayerSound> soundClass;

		Channel(String name, Class<? extends PlayerSound> clazz)
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
		String name = player.getName();
		Map<String, IResettableSound> map = getMap(name);
		IResettableSound sound = map.get(channel.getName());

		return !(sound == null || sound.isDonePlaying());
	}

	public static void addSound(EntityPlayer player, Channel channel, boolean replace)
	{
		String name = player.getName();
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
		String name = player.getName();
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
		Map<String, IResettableSound> map = soundMaps.computeIfAbsent(name, k -> new HashMap<>());

		return map;
	}

	public static boolean canRestartSound(ITickableSound sound)
	{
		return !mc.getSoundHandler().sndManager.invPlayingSounds.containsKey(sound);
	}
	
	public static void playSound(SoundEvent sound)
	{
        playSound(PositionedSoundRecord.getRecord(sound, 1.0F, (float) MekanismConfig.current().client.baseSoundVolume.val()));
	}

	public static void playSound(ISound sound)
	{
		mc.getSoundHandler().playSound(sound);
	}
}
