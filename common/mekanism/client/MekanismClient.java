package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;

public class MekanismClient extends Mekanism
{
	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	//General Configuration
	public static boolean enableSounds = true;
	public static boolean fancyUniversalCableRender = true;
}
