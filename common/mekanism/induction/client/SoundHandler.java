/**
 * 
 */
package mekanism.induction.client;

import mekanism.induction.common.MekanismInduction;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Calclavia
 * 
 */
@SideOnly(Side.CLIENT)
public class SoundHandler
{
	public static final SoundHandler INSTANCE = new SoundHandler();

	public static final String[] SOUND_FILES = { "Shock1.ogg", "Shock2.ogg", "Shock3.ogg", "Shock4.ogg", "Shock5.ogg", "Shock6.ogg", "Shock7.ogg" };

	@ForgeSubscribe
	public void loadSoundEvents(SoundLoadEvent event)
	{
		for (int i = 0; i < SOUND_FILES.length; i++)
		{
			event.manager.addSound(MekanismInduction.PREFIX + SOUND_FILES[i]);
		}

		MekanismInduction.LOGGER.fine("Loaded sound fxs");
	}
}
