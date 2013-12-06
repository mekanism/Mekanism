package mekanism.client;

import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketKey;
import net.minecraft.entity.player.EntityPlayer;

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
	
	public static long ticksPassed = 0;
	
	public static void updateKey(EntityPlayer player, int key)
	{
		if(Keyboard.isKeyDown(key) != keyMap.has(player, key))
		{
			PacketHandler.sendPacket(Transmission.SERVER, new PacketKey().setParams(key, Keyboard.isKeyDown(key)));
			keyMap.update(player, key, Keyboard.isKeyDown(key));
		}
	}
}
