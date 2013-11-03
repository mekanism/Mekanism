package mekanism.client;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.ClientPlayerTickHandler.UpdateQueueData;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MekanismClient extends Mekanism
{
	@SideOnly(Side.CLIENT)
	/** The main SoundHandler instance that is used by all audio sources */
	public static SoundHandler audioHandler;
	
	public static List<UpdateQueueData> cableUpdateQueue = new ArrayList<UpdateQueueData>();
	
	//General Configuration
	public static boolean enableSounds = true;
	public static boolean fancyUniversalCableRender = true;
	
	public static long ticksPassed = 0;
}
