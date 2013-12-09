package mekanism.client;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketKey;
import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MekanismClient extends Mekanism
{
	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	//General Configuration
	public static boolean enableSounds = true;
	public static boolean fancyUniversalCableRender = true;
	public static double baseSoundVolume = 1;
	
	public static long ticksPassed = 0;
	
	public static void updateKey(int key)
	{
		boolean down = Minecraft.getMinecraft().currentScreen == null ? Keyboard.isKeyDown(key) : false;
		
		if(down != keyMap.has(Minecraft.getMinecraft().thePlayer, key))
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketKey().setParams(key, down));
			keyMap.update(Minecraft.getMinecraft().thePlayer, key, down);
		}
	}
}
