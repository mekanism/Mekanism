package mekanism.client;

import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPI.BoxBlacklistEvent;
import mekanism.api.MekanismConfig.general;
import mekanism.client.sound.SoundHandler;
import mekanism.client.voice.VoiceClient;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketKey.KeyMessage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;

public class MekanismClient extends Mekanism
{
	public static VoiceClient voiceClient;

	public static long ticksPassed = 0;

	public static void updateKey(KeyBinding key, int type)
	{
		boolean down = Minecraft.getMinecraft().currentScreen == null ? key.getIsKeyPressed() : false;

		if(down != keyMap.has(Minecraft.getMinecraft().thePlayer, type))
		{
			Mekanism.packetHandler.sendToServer(new KeyMessage(type, down));
			keyMap.update(Minecraft.getMinecraft().thePlayer, type, down);
		}
	}

	public static void reset()
	{
		if(general.voiceServerEnabled)
		{
			if(MekanismClient.voiceClient != null)
			{
				MekanismClient.voiceClient.disconnect();
				MekanismClient.voiceClient = null;
			}
		}

		ClientTickHandler.tickingSet.clear();

		MekanismAPI.getBoxIgnore().clear();
		MinecraftForge.EVENT_BUS.post(new BoxBlacklistEvent());

		Mekanism.jetpackOn.clear();
		Mekanism.gasmaskOn.clear();
		Mekanism.flamethrowerActive.clear();
		Mekanism.activeVibrators.clear();

		SoundHandler.soundMaps.clear();

		Mekanism.proxy.loadConfiguration();

		Mekanism.logger.info("Reloaded config.");
	}
}
