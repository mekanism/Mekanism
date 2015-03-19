package mekanism.client.sound;

import net.minecraft.client.audio.ITickableSound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IResettableSound extends ITickableSound
{
	public void reset();
}
